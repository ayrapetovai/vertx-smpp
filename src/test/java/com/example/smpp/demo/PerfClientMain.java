package com.example.smpp.demo;

import com.cloudhopper.commons.charset.BaseCharset;
import com.cloudhopper.commons.charset.CharsetUtil;
import com.example.smpp.Smpp;
import com.example.smpp.client.SmppClientOptions;
import com.example.smpp.model.SmppBindType;
import com.example.smpp.pdu.DeliverSm;
import com.example.smpp.pdu.SubmitSm;
import com.example.smpp.types.Address;
import com.example.smpp.types.SmppInvalidArgumentException;
import com.example.smpp.util.charset.GSM8BitCharset;
import com.example.smpp.util.charset.Gsm7BitCharset;
import com.example.smpp.util.core.CountDownLatch;
import com.example.smpp.util.core.FlowControl;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.smpp.demo.PerfClientMain.Encoder.NONE;

public class PerfClientMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(PerfClientMain.class);

  private static class Counters {
    double start;
    double submitEnd;
    double submitSmLatencySumNano;
    double submitSmRespCount;
    long submitSmCount;
    double deliverEnd;
    double deliverSmRespCount;
    long deliverSmCount;
    double deliverSmRespLatencySumNano;
  }

  private static final String  SYSTEM_ID = "vertx-smpp-client";
  private static final int     SESSIONS = 1;
  private static final int     THREADS = 1;
  private static final boolean SSL = false;
  private static final int     WINDOW = 600;
  private static final Encoder ENCODE = NONE;
//  private static final Encoder ENCODE = Encoder.CLOUDHOPPER_GSM;
//  private static final Encoder ENCODE = Encoder.CLOUDHOPPER_GSM7;
//  private static final Encoder ENCODE = Encoder.CLOUDHOPPER_UCS_2;
//  private static final Encoder ENCODE = Encoder.CUSTOM_GSM8;
//  private static final Encoder ENCODE = Encoder.CUSTOM_GSM7;
//  private static final Encoder ENCODE = Encoder.PLAIN_UTF8;
  private static final int     SUBMIT_SM_NUMBER = 1_000_000;

  private final Random rng = new Random();

  @Override
  public void start(Promise<Void> startPromise) {

    var submitSmLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER);
    var deliverSmRespLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER);

    var options = new SmppClientOptions();
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
          cfg.onCreated(sess -> log.info("user code: session#{} created, bound to {}", sess.getId(), sess.getBoundToSystemId()));
          cfg.onUnexpectedResponse(respCtx -> log.warn("user code: unexpected response received {}", respCtx.getResponse()));
          cfg.onForbiddenRequest(reqCtx -> log.info("user code: reacts to forbidden request pdu {}", reqCtx.getRequest()));
          cfg.onClose(sess -> {}); // TODO удалить? Вместо этого используется промис для close?
        })
        .bind("localhost", SSL? 2777: 2776)
        .onRefuse(e -> {
          log.error("user code: server refused to bind", e);
          startPromise.fail(e);
        })
        .onSuccess(sess -> {
          log.info("user code: client bound");
          var counters = new Counters();
          sess.setReferenceObject(counters);
          counters.start = System.currentTimeMillis();
          FlowControl
              .forLoopInt(vertx, 0, SUBMIT_SM_NUMBER, i -> {
                counters.submitSmCount++;
                var ssm = new SubmitSm();
                setSourceAndDestAddress(ssm);
                if (ENCODE != NONE) {
                  addShortMessage(ssm);
                }
                var sendSubmitSmStart = new long[]{System.nanoTime()};
                sess.send(ssm)
                    .onSuccess(submitSmResp -> {
                      counters.submitSmLatencySumNano += (System.nanoTime() - sendSubmitSmStart[0]);
                      counters.submitSmRespCount++;
                      submitSmLatch.countDown(1);
                    })
                    .onFailure(e -> log.error("user code: cannot send", e))
                    .onWindowTimeout(e -> log.error("user code: window timeout", e));
              })
              .compose(v -> submitSmLatch.await(20, TimeUnit.SECONDS))
              .compose(v -> {
                counters.submitEnd = System.currentTimeMillis();
                return deliverSmRespLatch.await(20, TimeUnit.SECONDS);
              })
              .compose(v -> {
                counters.deliverEnd = System.currentTimeMillis();
                var closePromise = Promise.<Void>promise();
                sess.close(closePromise);
                return closePromise.future();
              })
              .onComplete(ar -> {
                var c = sess.getReferenceObject(Counters.class);
                log.info(
                    "done: threads={}, sessions={}, window={}, text({}), this={}, that={}, ssl={}",
                    THREADS, SESSIONS, WINDOW, ENCODE.name(), SYSTEM_ID, sess.getBoundToSystemId(), SSL? "on": "off"
                );
                var submitSmThroughput = (c.submitSmRespCount/((c.submitEnd - c.start)/1000.0));
                log.info("submitSm=" + c.submitSmCount + ", submitSmResp=" + c.submitSmRespCount + ", throughput=" + submitSmThroughput);
                log.info("submitSm latency=" + (0.000_001 * c.submitSmLatencySumNano/c.submitSmRespCount));
                log.info("submit_sm time=" + (c.submitEnd - c.start) + "ms");

                var deliverSmThroughput = (c.deliverSmRespCount/((c.deliverEnd - c.start)/1000.0));
                log.info("deliverSm=" + c.deliverSmCount + ", deliverSmResp=" + c.deliverSmRespCount + ", throughput=" + deliverSmThroughput);
                log.info("deliverSmResp latency=" + (0.000_001 * c.deliverSmRespLatencySumNano/c.deliverSmRespCount));
                log.info("deliver_sm time=" + positiveOrNaN(c.deliverEnd - c.start) + "ms");

                log.info("Overall throughput=" + (submitSmThroughput + deliverSmThroughput));
                startPromise.complete();
              });
        })
        .onFailure(e -> log.error("bind failed", e));


    // make second session
//    client
//        .bind("localhost", 2776)
//        .onSuccess(sess -> {
//        });
  }

  private void setSourceAndDestAddress(SubmitSm ssm) {
    var from = "8916" + (rng.nextInt(9000000) + 1000000);
    var to = "8916" + (rng.nextInt(9000000) + 1000000);
    ssm.setSourceAddress(new Address((byte) 2, (byte) 2, from));
    ssm.setDestAddress(new Address((byte) 2, (byte) 2, to));
  }

  private double positiveOrNaN(double l) {
    return l < 0? Double.NaN: l;
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
//==================================
//23:33:35.789 - done: threads=4, sessions=4, window=600, text(none), this=vertx-smpp-client, that=vertx-smpp-server, ssl=off
//23:33:35.789 - submitSm=2000000, submitSmResp=2000000, throughput=94643.1951542684
//23:33:35.789 - submitSm latency=0.7106796106344999
//23:33:35.789 - submit_sm time=21132ms
//23:33:35.789 - deliverSm=2000000, deliverSmResp=2000000, throughput=94643.1951542684
//23:33:35.789 - deliverSmResp latency=0.0924942272955
//23:33:35.789 - deliver_sm time=21132.0ms
//23:33:35.789 - Overall throughput=189286.3903085368

//==================================

//23:17:30.585 - done: threads=1, sessions=1, window=600, text(none), this=vertx-smpp-client, that=vertx-smpp-server, ssl=off
//23:17:30.589 - submitSm=2000000, submitSmResp=2000000, throughput=164460.15952635475
//23:17:30.590 - submitSm latency=0.22241932045549997
//23:17:30.591 - submit_sm time=12161ms
//23:17:30.591 - deliverSm=2000000, deliverSmResp=2000000, throughput=164460.15952635475
//23:17:30.591 - deliverSmResp latency=0.05109675399349999
//23:17:30.591 - deliver_sm time=12161.0ms
//23:17:30.591 - Overall throughput=328920.3190527095

//==================================

// vertx-smpp(4), no text, each
//15:47:54.270 - submitSm=1000000, submitSmResp=1000000, throughput=87260.03490401396
//15:47:54.270 - submitSm latency=1.3886247809849999
//15:47:54.270 - deliverSm=1000001, deliverSmResp=1000001, throughput=87260.12216404887
//15:47:54.270 - deliverSmResp latency=0.16470121744778254
//15:47:54.270 - Overall throughput=174520.1570680628
//15:47:54.270 - Time=11460ms
//throughput 700000

// cloudhopper(4), no text, sum
//16:03:23.434 - submitSm=1000000, submitSmResp=1000000, throughput=18183.80186929483
//16:03:23.434 - submitSm latency=35.967412587631
//16:03:23.434 - deliverSm=548472, deliverSmResp=548472, throughput=9971.492982328558
//16:03:23.434 - deliverSmResp latency=0.15386301283019005
//16:03:23.434 - Overall throughput=28155.29485162339
//16:03:23.434 - Time=54994ms

//==================================

// vertx-smpp(4), text(cloudhopper.CHARSET_GSM), each, window(600)
//18:56:07.903 - submitSm=2000000, submitSmResp=2000000, throughput=32591.337222566242
//18:56:07.903 - submitSm latency=1.293296398087
//18:56:07.903 - submit_sm time=61366ms
//18:56:07.903 - deliverSm=2000000, deliverSmResp=2000000, throughput=32591.337222566242
//18:56:07.903 - deliverSmResp latency=0.093746191569
//18:56:07.903 - deliver_sm time=61366ms
//18:56:07.903 - Overall throughput=65182.674445132485
//throughput 260000

// vertx-smpp(4), text(Gsm7BitCharset), each, window(600)
//19:00:31.635 - submitSm=2000000, submitSmResp=2000000, throughput=71813.28545780969
//19:00:31.635 - submitSm latency=1.0924684289770001
//19:00:31.635 - submit_sm time=27850ms
//19:00:31.635 - deliverSm=2000000, deliverSmResp=2000000, throughput=71810.70697641019
//19:00:31.635 - deliverSmResp latency=0.109346238805
//19:00:31.635 - deliver_sm time=27851ms
//19:00:31.635 - Overall throughput=143623.99243421986
//throughput 547000

//==================================

// vertx-smpp(1), text(cloudhopper.CHARSET_GSM)
//submitSm=1000000, submitSmResp=1000000, throughput=47947.83275795934
//submitSm latency=0.7100937028389999
//deliverSm=1000001, deliverSmResp=1000001, throughput=47945.58181905356
//deliverSmResp latency=0.05510074804025196
//Overall throughput=95893.4145770129
//Time=20856ms

// cloudhopper(1), text(cloudhopper.CHARSET_GSM)
//submitSm=1000000, submitSmResp=1000000, throughput=47959.330487746396
//submitSm latency=0.692746395918
//deliverSm=788751, deliverSmResp=788751, throughput=37822.528052172245
//deliverSmResp latency=0.04484290301628777
//Overall throughput=85781.85853991864
//Time=20851ms

//==================================

// vertx-smpp(1), no text
//22:48:24.476 - submitSm=2000000, submitSmResp=2000000, throughput=136407.0386031919
//22:48:24.477 - submitSm latency=0.2502508931195
//22:48:24.478 - submit_sm time=14662ms
//22:48:24.478 - deliverSm=2000000, deliverSmResp=2000000, throughput=136407.0386031919
//22:48:24.478 - deliverSmResp latency=0.05146083862
//22:48:24.478 - deliver_sm time=14662ms
//22:48:24.478 - Overall throughput=272814.0772063838

// cloudhopper(1), no text
//22:52:08.558 - submitSm=2000000, submitSmResp=2000000, throughput=147775.97162701344
//22:52:08.559 - submitSm latency=0.30968050239899997
//22:52:08.560 - submit_sm time=13534ms
//22:52:08.560 - deliverSm=234471, deliverSmResp=234471, throughput=-1.4238244491955677E-4
//22:52:08.560 - deliverSmResp latency=0.02519616518887197
//22:52:08.561 - deliver_sm time=-1646769025019ms
//22:52:08.561 - Overall throughput=147775.97148463098

//==================================

//20:18:01.227 - done: threads=1, sessions=1, window=600, text(NONE), this=vertx-smpp-client, that=vertx-smpp-server, ssl=off
//20:18:01.230 - submitSm=100000000, submitSmResp=1.0E8, throughput=158588.1845458986
//20:18:01.231 - submitSm latency=0.22691762997125
//20:18:01.232 - submit_sm time=630564.0ms
//20:18:01.232 - deliverSm=100000000, deliverSmResp=1.0E8, throughput=158588.1845458986
//20:18:01.232 - deliverSmResp latency=0.05354155271269
//20:18:01.232 - deliver_sm time=630564.0ms
//20:18:01.232 - Overall throughput=317176.3690917972
//20:18:01.233 - cosing vertx 06d5e10c-c5c2-466e-a192-91d8551368f1

  public static void main(String[] args) {
    var vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(THREADS));
    vertx.deployVerticle(PerfClientMain.class.getCanonicalName(), new DeploymentOptions().setInstances(SESSIONS))
        .onComplete(arId -> {
          log.info("closing vertx {}", arId.result());
          vertx.close();
        });
  }
}
