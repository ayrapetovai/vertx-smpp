package com.example.smpp.demo;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.example.smpp.Smpp;
import com.example.smpp.client.SmppClient;
import com.example.smpp.client.SmppClientOptions;
import com.example.smpp.model.SmppBindType;
import com.example.smpp.util.vertx.CountDownLatch;
import com.example.smpp.util.vertx.Loop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SmppClientMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(SmppClientMain.class);

  private static final String SYSTEM_ID = "vertx-smpp-client";
  private static final int SESSIONS = 1;
  private static final int THREADS = 1;
  private static final boolean LOADED = false;
  private static final boolean SSL = false;
//  private static final int SUBMIT_SM_NUMBER = 50_000_000;
//  private static final int SUBMIT_SM_NUMBER = 10_000_000;
//  private static final int SUBMIT_SM_NUMBER = 5_000_000;
//  private static final int SUBMIT_SM_NUMBER = 2_000_000;
//  private static final int SUBMIT_SM_NUMBER = 1_000_000;
//  private static final int SUBMIT_SM_NUMBER = 100_000;
//  private static final int SUBMIT_SM_NUMBER = 20_000;
//  private static final int SUBMIT_SM_NUMBER = 10_000;
//  private static final int SUBMIT_SM_NUMBER = 7_000;
//  private static final int SUBMIT_SM_NUMBER = 1_000;
  private static final int SUBMIT_SM_NUMBER = 10;
//  private static final int SUBMIT_SM_NUMBER = 4;
//  private static final int SUBMIT_SM_NUMBER = 1;
  SmppClient client;

  @Override
  public void start(Promise<Void> startPromise) {

    var start = new long[]{0};
    var submitEnd = new long[]{0};
    var submitSmLatencySumNano = new long[]{0};
    var submitSmRespCount = new long[]{0};
    var submitSmCount = new long[]{0};
    var deliverEnd = new long[]{0};
    var deliverSmRespCount = new long[]{0};
    var deliverSmCount = new long[]{0};
    var deliverSmRespLatencySumNano = new long[]{0};

    var submitSmLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER);
    var deliverSmRespLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER);

    var options = new SmppClientOptions();
    if (SSL) {
      options.setSsl(true);
      options.setTrustAll(true);
    }

//    client = Smpp.client(vertx);
    client = Smpp.client(vertx, options);
    client
        .configure(cfg -> {
          log.info("user code: configuring new session#{}", cfg.getId());
          cfg.setSystemId(SYSTEM_ID);
          cfg.setPassword("test");
          cfg.setBindType(SmppBindType.TRANSCEIVER); // TODO так лучше, чем в bind?
          cfg.setWindowSize(600);
          cfg.setBindTimeout(1000);
//          cfg.setWriteTimeout(1000);
          cfg.setWindowWaitTimeout(1000);
          cfg.onRequest(reqCtx -> {
            if (reqCtx.getRequest() instanceof DeliverSm) {
              deliverSmCount[0]++;
              var sendDeliverSmRespStart = new long[]{System.nanoTime()};
              var resp = reqCtx.getRequest().createResponse();
              reqCtx.getSession()
                  .reply(resp)
                  .onSuccess(nothing -> {
                    deliverSmRespLatencySumNano[0] += (System.nanoTime() - sendDeliverSmRespStart[0]);
                    deliverSmRespCount[0]++;
                    deliverSmRespLatch.countDown(1);
                  })
                  .onFailure(e -> {
                    log.trace("user code: could no reply with {}", resp.getName(), e);
                  });
            }
          });
          cfg.onUnexpectedResponse(respCtx -> {
            log.warn("user code: unexpected response received {}", respCtx.getResponse());
          });
          cfg.onCreated(sess -> {
            log.info("user code: session#{} created, bound to {}", sess.getId(), sess.getBoundToSystemId());
          });
          cfg.onForbiddenRequest(reqCtx -> {
            log.info("user code: reacts to forbidden request pdu {}", reqCtx.getRequest());
          });
        })
        .bind("localhost", SSL? 2777: 2776)
        .onSuccess(sess -> {
          start[0] = System.currentTimeMillis();
          log.info("user code: client bound");
          new Loop(vertx)
              .forLoopInt(0, SUBMIT_SM_NUMBER, i -> {
                var throttled = Promise.<Boolean>promise();
                submitSmCount[0]++;
                var ssm = new SubmitSm();
//                setSourceAndDestAddress(ssm);
                if (LOADED) {
                  addShortMessage(ssm);
                }
                var sendSubmitSmStart = new long[]{System.nanoTime()};
                sess.send(ssm)
                    .onSuccess(submitSmResp -> {
                      submitSmLatencySumNano[0] += (System.nanoTime() - sendSubmitSmStart[0]);
                      submitSmRespCount[0]++;
                      submitSmLatch.countDown(1);
                      throttled.complete(false);
                    })
                    .onFailure(e -> {
                      e.printStackTrace();
                      throttled.complete(true);
                    });
                return throttled.future();
              })
              .compose(v -> submitSmLatch.await(20, TimeUnit.SECONDS))
              .compose(v -> {
                submitEnd[0] = System.currentTimeMillis();
                return deliverSmRespLatch.await(20, TimeUnit.SECONDS);
              })
              .compose(v -> {
                deliverEnd[0] = System.currentTimeMillis();
                var closePromise = Promise.<Void>promise();
                sess.close(closePromise);
                return closePromise.future();
              })
              .onComplete(ar -> {
                sess.close(Promise.promise());
                log.info("done threads={}, sessions={}, {}, this={}, that={}, ssl={}", THREADS, SESSIONS, (LOADED? "text": "no text"), SYSTEM_ID, sess.getBoundToSystemId(), SSL);
                var submitSmThroughput = ((double)submitSmRespCount[0]/((double)(submitEnd[0] - start[0])/1000.0));
                log.info(
                    "submitSm=" + submitSmCount[0] +
                    ", submitSmResp=" + submitSmRespCount[0] +
                        ", throughput=" + submitSmThroughput);
                log.info("submitSm latency=" + (0.000_001 * (double)submitSmLatencySumNano[0]/(double)submitSmRespCount[0]));
                log.info("submit_sm time=" + (submitEnd[0] - start[0]) + "ms");

                var deliverSmThroughput = ((double)deliverSmRespCount[0]/((double)(deliverEnd[0] - start[0])/1000.0));
                log.info(
                    "deliverSm=" + deliverSmCount[0] +
                    ", deliverSmResp=" + deliverSmRespCount[0] +
                        ", throughput=" + deliverSmThroughput);
                log.info("deliverSmResp latency=" + (0.000_001 * (double)deliverSmRespLatencySumNano[0]/(double)deliverSmRespCount[0]));
                log.info("deliver_sm time=" + if1stNegGet2nd(deliverEnd[0] - start[0], Double.NaN) + "ms");

                log.info("Overall throughput=" + (submitSmThroughput + deliverSmThroughput));
                startPromise.complete();
  //              vertx.close(); // не позволяет деплоить несколько верикалей
              });
        })
        .onFailure(e -> {
          log.error("could not bind", e);
          startPromise.fail(e);
        });


    // make second session
//    client
//        .bind("localhost", 2776)
//        .onSuccess(sess -> {
//        });
  }

  private void setSourceAndDestAddress(SubmitSm ssm) {
    var rng = new Random();
    var from = String.valueOf(rng.nextInt(100000));
    var to = String.valueOf(rng.nextInt(100000));
    ssm.setSourceAddress(new Address((byte) 2, (byte) 2, from));
    ssm.setDestAddress(new Address((byte) 2, (byte) 2, to));
  }

  private double if1stNegGet2nd(long l, double d) {
    return l < 0? d: l;
  }

  private void addShortMessage(SubmitSm ssm) {
    String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
//    String text160 = "txId:" + java.util.UUID.randomUUID() + ";";
//    byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);
//    byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM7);
//    byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_UCS_2);
    byte[] textBytes = text160.getBytes(StandardCharsets.UTF_8);

//    byte[] textBytes = new byte[0];
//    try {
//      CharsetEncoder gsmEncoder = new Gsm7BitCharset("UTF-8", new String[]{"gsm"}).newEncoder();
//      textBytes = gsmEncoder.encode(CharBuffer.wrap(text160)).array();
//    } catch (CharacterCodingException e) {
//      e.printStackTrace();
//    }
    try {
      ssm.setShortMessage(textBytes);
    } catch (SmppInvalidArgumentException e) {
      e.printStackTrace();
    }
  }
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

  public static void main(String[] args) {
    var vertex = Vertx.vertx();
    var depOpts = new DeploymentOptions()
      .setInstances(SESSIONS) // TODO scale to 2 and more
      .setWorkerPoolSize(THREADS)
      ;
    vertex.deployVerticle(SmppClientMain.class.getCanonicalName(), depOpts);
  }
}
