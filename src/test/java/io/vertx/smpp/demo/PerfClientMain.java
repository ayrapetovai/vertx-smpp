package io.vertx.smpp.demo;

import com.cloudhopper.commons.charset.BaseCharset;
import com.cloudhopper.commons.charset.CharsetUtil;
import io.vertx.smpp.Smpp;
import io.vertx.smpp.client.SmppClientOptions;
import io.vertx.smpp.model.SmppBindType;
import io.vertx.smpp.pdu.DeliverSm;
import io.vertx.smpp.pdu.SubmitSm;
import io.vertx.smpp.session.SmppSession;
import io.vertx.smpp.types.Address;
import io.vertx.smpp.types.SmppInvalidArgumentException;
import io.vertx.smpp.util.charset.GSM8BitCharset;
import io.vertx.smpp.util.charset.Gsm7BitCharset;
import io.vertx.smpp.util.core.CountDownLatch;
import io.vertx.smpp.util.core.FlowControl;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.vertx.smpp.demo.PerfClientMain.Encoder.NONE;

//|> threads=1, sessions=1, window=600(mean 38, max 365), text(CUSTOM_GSM8), this=vertx-smpp-client, that=vertx-smpp-server, ssl=off
//        |   requests | responses | throughput | latency,ms |  time,ms | failures | rTimeout |  discard | onClosed |  wrongOp | oTimeout | overflowed
// submit |    5000000 |   5000000 |     132597 |      0,271 | 37708,00 |        0 |        0 |        0 |        0 |        0 |        0 |          0
//deliver |    5000000 |   5000000 |     132597 |      0,067 | 37708,00 |       -1 |       -1 |       -1 |       -1 |       -1 |       -1 |         -1

public class PerfClientMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(PerfClientMain.class);

  private static class Counters {
    double start;
    double submitEnd;
    double submitSmLatencySumNano;
    long submitSmRespCount;
    long submitSmOnChannelClosed;
    long submitSmDiscarded;
    long submitSmWrongOperation;
    long submitSmOfferTimeout;
    long submitSmWriteOverflow;
    long submitSmAllFailures;
    long submitSmTimeout;
    long submitSmCount;
    double deliverEnd;
    long deliverSmRespCount;
    long deliverSmCount;
    double deliverSmRespLatencySumNano;
    double meanWindowSize;
    int maxWindowSize;
  }

  private static final String  SYSTEM_ID = "vertx-smpp-client";
  private static final int     SESSIONS = 1;
  private static final int     THREADS = 1;
  private static final boolean SSL = false;
  private static final boolean AWAIT_DELIVERY = true;
  private static final int     WINDOW = 600;
//  private static final Encoder ENCODE = NONE;
//  private static final Encoder ENCODE = Encoder.CLOUDHOPPER_GSM;
//  private static final Encoder ENCODE = Encoder.CLOUDHOPPER_GSM7;
//  private static final Encoder ENCODE = Encoder.CLOUDHOPPER_UCS_2;
  private static final Encoder ENCODE = Encoder.CUSTOM_GSM8;
//  private static final Encoder ENCODE = Encoder.CUSTOM_GSM7;
//  private static final Encoder ENCODE = Encoder.PLAIN_UTF8;
  private static final int     SUBMIT_SM_NUMBER = 1_000_000;
  private static final Set<SmppSession> sessions = new HashSet<>();

  private final Random rng = new Random();

  @Override
  public void start(Promise<Void> startPromise) {

    var submitSmLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER);
    var deliverSmRespLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER);

    var forLoop = FlowControl.forLoopInt(vertx.getOrCreateContext(), 0, SUBMIT_SM_NUMBER);

    var options = new SmppClientOptions()
      .setConnectTimeout(2000);
    if (SSL) {
      options
          .setSsl(true)
          .setTrustAll(true);
    }

    Smpp.client(vertx, options)
        .configure(cfg -> {
          log.info("user code: configuring new session");
          cfg.setSystemId(SYSTEM_ID);
          cfg.setPassword("test");
          cfg.setBindType(SmppBindType.TRANSCEIVER);
          cfg.setWindowSize(WINDOW);
          cfg.setUnbindTimeout(1000);
          cfg.setBindTimeout(5000);
          cfg.setWriteTimeout(5000);
          cfg.setRequestExpiryTimeout(5000);
          cfg.setWindowWaitTimeout(5000);
          cfg.onRequest(reqCtx -> {
            var counters = reqCtx.getSession().getReferenceObject(Counters.class);
            if (reqCtx.getRequest() instanceof DeliverSm) {
              counters.deliverSmCount++;
              var sendDeliverSmRespStart = new long[]{System.nanoTime()};
              var resp = reqCtx.getRequest().createResponse();
              reqCtx.getSession()
                  .reply(resp)
                  .onSuccess(nothing -> {
                    counters.deliverSmRespLatencySumNano += (System.nanoTime() - sendDeliverSmRespStart[0]);
                    counters.deliverSmRespCount++;
                    deliverSmRespLatch.countDown(1);
                  })
                  .onFailure(e -> log.trace("user code: could no reply with {}", resp.getName(), e));
            }
          });
          // TODO remove onCreated from ClientSessionConfigurator? Let user use BindFuture.onSuccess(session)?
          cfg.onCreated(sess -> log.info("user code: session#{} created, bound to {}", sess.getId(), sess.getBoundToSystemId()));
          cfg.onUnexpectedResponse(respCtx -> log.warn("user code: unexpected response received {}", respCtx.getResponse()));
          cfg.onForbiddenRequest(reqCtx -> log.info("user code: reacts to forbidden request pdu {}", reqCtx.getRequest()));
          cfg.onOverflowed(v -> forLoop.pause());
          cfg.onDrained(v -> forLoop.resume());
          cfg.onClosed(sess -> {
            log.info("user code: onClosed, stopping for loop and completing startPromise");
            forLoop.stop();
            startPromise.tryComplete();
          });
        })
        .bind("localhost", SSL? 2777: 2776)
        .onBindRefused(e -> log.error("user code: server refused to bind"))
        .onSuccess(sess -> {
          sessions.add(sess);
          log.info("user code: client bound");
          var counters = new Counters();
          sess.setReferenceObject(counters);
          counters.start = System.currentTimeMillis();
          forLoop.start(i -> {
            counters.submitSmCount++;
            var ssm = new SubmitSm();
            setSourceAndDestAddress(ssm);
            if (ENCODE != NONE) {
              addShortMessage(ssm);
            }
            var sendSubmitSmStart = new long[]{System.nanoTime()};
            sess.send(ssm)
                .onComplete(resp -> counters.submitSmLatencySumNano += (System.nanoTime() - sendSubmitSmStart[0]))
                .onSuccess(submitSmResp -> {
                  counters.submitSmRespCount++;
                  submitSmLatch.countDown(1);
                })
                .onWrongOperation(e -> counters.submitSmWrongOperation++)
                .onChannelClosed(e -> counters.submitSmOnChannelClosed++ )
                .onTimeout(e -> counters.submitSmTimeout++)
                .onDiscarded(e -> counters.submitSmDiscarded++)
                .onWindowTimeout(e -> counters.submitSmOfferTimeout++)
                .onOverflowed(e -> {
//                  forLoop.pause();
                  counters.submitSmWriteOverflow++;
                })
                .onFailure(e -> counters.submitSmAllFailures++)
                ;
            var windowSize = sess.getWindowSize();
            if (windowSize > counters.maxWindowSize) {
              counters.maxWindowSize = windowSize;
            }
            counters.meanWindowSize += (double) windowSize/SUBMIT_SM_NUMBER;
          })
          .compose(v -> submitSmLatch.await(5, TimeUnit.SECONDS))
          .compose(v -> {
            counters.submitEnd = System.currentTimeMillis();
            if (AWAIT_DELIVERY) {
              return deliverSmRespLatch.await(5, TimeUnit.SECONDS);
            } else {
              return Future.succeededFuture();
            }
          })
          .compose(v -> {
            counters.deliverEnd = System.currentTimeMillis();
            var closePromise = Promise.<Void>promise();
            sess.close(closePromise);
            return closePromise.future();
          })
          .onComplete(ar -> {
            printCounters(sess);
            startPromise.tryComplete();
          });
        })
        .onFailure(e -> {
          log.error("bind failed", e);
          startPromise.tryFail("bind failed");
        });
  }

  private static void printCounters(SmppSession sess) {
    var c = sess.getReferenceObject(Counters.class);

    if (c.submitEnd == 0) {
      c.submitEnd = System.currentTimeMillis();
    }
    if (c.deliverEnd == 0) {
      c.deliverEnd = System.currentTimeMillis();
    }
    log.info(
        "done: threads={}, sessions={}, window={}, text({}), this={}, that={}, ssl={}",
        THREADS, SESSIONS, WINDOW, ENCODE.name(), SYSTEM_ID, sess.getBoundToSystemId(), SSL? "on": "off"
    );
    var submitSmThroughput = ((double) c.submitSmRespCount/((c.submitEnd - c.start)/1000.0));
    var deliverSmThroughput = ((double) c.deliverSmRespCount/((c.deliverEnd - c.start)/1000.0));

    var table = new StringBuilder();
    table.append(String.format(
        "|> threads=%d, sessions=%d, window=%d(mean %d, max %d), text(%s), this=%s, that=%s, ssl=%s",
        THREADS, SESSIONS, WINDOW, (int) c.meanWindowSize, c.maxWindowSize, ENCODE.name(), SYSTEM_ID, sess.getBoundToSystemId(), SSL? "on": "off"
        )).append('\n');
    table.append(
        "        |   requests | responses | throughput | latency,ms |  time,ms | failures | rTimeout |  discard | onClosed |  wrongOp | oTimeout | overflowed ").append('\n');
    table.append(" submit | ")
        .append(String.format("%10d |", c.submitSmCount)) // requests
        .append(String.format("%10d |", c.submitSmRespCount)) // responses
        .append(String.format(" %10d |", (int) submitSmThroughput)) // throughput
        .append(String.format("%11.3f |", (0.000_001 * c.submitSmLatencySumNano/c.submitSmRespCount))) // latency,ms
        .append(String.format("%9.2f |", c.submitEnd - c.start)) // time,ms
        .append(String.format("%9d |", c.submitSmAllFailures)) // failures
        .append(String.format("%9d |", c.submitSmTimeout)) // rTimeout
        .append(String.format("%9d |", c.submitSmDiscarded)) // discard
        .append(String.format("%9d |", c.submitSmOnChannelClosed)) // onClosed
        .append(String.format("%9d |", c.submitSmWrongOperation)) // wrongOp
        .append(String.format("%9d |", c.submitSmOfferTimeout)) // oTimeout
        .append(String.format("%11d",  c.submitSmWriteOverflow)) // overflowed
        .append('\n');
    table.append("deliver |")
        .append(String.format("%11d |", c.deliverSmCount)) // requests
        .append(String.format("%10d |", c.deliverSmRespCount)) // responses
        .append(String.format(" %10d |", (int) deliverSmThroughput)) // throughput
        .append(String.format("%11.3f |", (0.000_001 * c.deliverSmRespLatencySumNano/c.deliverSmRespCount))) // latency,ms
        .append(String.format("%9.2f |", positiveOrNaN(c.deliverEnd - c.start))) // time,ms
        .append(String.format("%9d |", -1)) // failures
        .append(String.format("%9d |", -1)) // rTimeout
        .append(String.format("%9d |", -1)) // discard
        .append(String.format("%9d |", -1)) // onClosed
        .append(String.format("%9d |", -1)) // wrongOp
        .append(String.format("%9d |", -1)) // oTimeout
        .append(String.format("%11d",  -1)) // overflowed
        .append('\n');
    log.info("Statistics:\n{}", table);
  }

  private static double positiveOrNaN(double l) {
    return l < 0? Double.NaN: l;
  }

  private void setSourceAndDestAddress(SubmitSm ssm) {
    var from = "8916" + (rng.nextInt(9000000) + 1000000);
    var to = "8916" + (rng.nextInt(9000000) + 1000000);
    ssm.setSourceAddress(new Address((byte) 2, (byte) 2, from));
    ssm.setDestAddress(new Address((byte) 2, (byte) 2, to));
  }

  enum Encoder {
    NONE {
      @Override
      public byte[] encode(String text) {
        return null;
      }
    },
    CLOUDHOPPER_GSM {
      @Override
      public byte[] encode(String text) {
        return CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM);
      }
    },
    CLOUDHOPPER_GSM7 {
      @Override
      public byte[] encode(String text) {
        return CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM7);
      }
    },
    CLOUDHOPPER_UCS_2 {
      @Override
      public byte[] encode(String text) {
        return CharsetUtil.encode(text.substring(0, 255/2), CharsetUtil.CHARSET_UCS_2);
      }
    },
    PLAIN_UTF8 {
      @Override
      public byte[] encode(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
      }
    },
    CUSTOM_GSM8 {
      @Override
      public byte[] encode(String text) {
        return CharsetUtil.encode(text, gsm8BitCharset);
      }
    },
    CUSTOM_GSM7 {
      @Override
      public byte[] encode(String text) {
        try {
          return gsm7BitCharsetEncoder.encode(CharBuffer.wrap(text)).array();
        } catch (CharacterCodingException e) {
          e.printStackTrace();
        }
        return null;
      }
    };
    public abstract byte[] encode(String text);
    private static final BaseCharset gsm8BitCharset = new GSM8BitCharset();
    private static final CharsetEncoder gsm7BitCharsetEncoder = new Gsm7BitCharset("UTF-8", new String[]{"gsm"}).newEncoder();
  }

  private void addShortMessage(SubmitSm ssm) {
    String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
//    String text160 = "txId:" + java.util.UUID.randomUUID() + ";";
    var textBytes = ENCODE.encode(text160);
    try {
      ssm.setShortMessage(textBytes);
    } catch (SmppInvalidArgumentException e) {
      e.printStackTrace();
    }
  }

  private static Thread shutdownHook = null;
  private static void registerShutdownHook(Vertx vertx) {
    if (shutdownHook == null) {
      shutdownHook = new Thread(() -> shutdownRoutine(vertx));
      Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
  }

  // FIXME this shutdown hook work when several sessions are closing onSuccess, must not.
  private static synchronized boolean unregisterShutdownHook() {
    if (shutdownHook != null) {
      try {
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
      } catch (IllegalStateException e) {
        return false;
      }
      shutdownHook = null;
      return true;
    }
    return false;
  }

  private static void shutdownRoutine(Vertx vertx) {
    log.info("shutdown hook: sessions {}", sessions);
    var openedSessions = sessions.stream()
        .filter(s -> s.isOpened() || s.isBound()).collect(Collectors.toList());
    var closeLatch = new java.util.concurrent.CountDownLatch(openedSessions.size());
    openedSessions.forEach(sess -> {
      vertx.runOnContext(__ -> {
        var closePromise = Promise.<Void>promise();
        closePromise.future()
            .onComplete(v -> {
              printCounters(sess);
              closeLatch.countDown();
            });
        sess.close(closePromise);
      });
    });
    try {
      if (!closeLatch.await(10, TimeUnit.SECONDS)) {
        log.warn("session close timed out");
      }
    } catch (InterruptedException ignore) {}
    log.info("closing vertx");
    vertx.close();
  }

  public static void main(String[] args) {
    var vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(THREADS));
    registerShutdownHook(vertx);
    vertx.deployVerticle(
        PerfClientMain.class.getCanonicalName(),
        new DeploymentOptions().setInstances(SESSIONS)
    )
        .onComplete(id -> {
          if (unregisterShutdownHook()) {
            shutdownRoutine(vertx);
          }
        });
  }
}
