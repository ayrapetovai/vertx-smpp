package com.example.smpp.main;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.example.smpp.Smpp;
import com.example.smpp.client.SmppClient;
import com.example.smpp.util.smpp.Gsm7BitCharset;
import com.example.smpp.util.vertx.CountDownLatch;
import com.example.smpp.util.vertx.Loop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.nio.charset.CharsetEncoder;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SmppClientMain extends AbstractVerticle {

//  private static final int SUBMIT_SM_NUMBER = 50_000_000;
//  private static final int SUBMIT_SM_NUMBER = 10_000_000;
//  private static final int SUBMIT_SM_NUMBER = 2_000_000;
  private static final int SUBMIT_SM_NUMBER = 1_000_000;
//  private static final int SUBMIT_SM_NUMBER = 100_000;
//  private static final int SUBMIT_SM_NUMBER = 20_000;
//  private static final int SUBMIT_SM_NUMBER = 10_000;
//  private static final int SUBMIT_SM_NUMBER = 7_000;
//  private static final int SUBMIT_SM_NUMBER = 1_000;
//  private static final int SUBMIT_SM_NUMBER = 10;
//  private static final int SUBMIT_SM_NUMBER = 4;
//  private static final int SUBMIT_SM_NUMBER = 1;
  SmppClient client;
  ExecutorService executorService = Executors.newSingleThreadExecutor();

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

    client = Smpp.client(vertx);
    client
      .onRequest(req -> {
        // FIXME some req.getRequest() are null
        if (req.getRequest() instanceof DeliverSm) {
          deliverSmCount[0]++;
          var sendDeliverSmRespStart = new long[]{System.nanoTime()};
          req.getSession()
              .reply(req.getRequest().createResponse())
              .onSuccess(nothing -> {
                deliverSmRespLatencySumNano[0] += (System.nanoTime() - sendDeliverSmRespStart[0]);
                deliverSmRespCount[0]++;
                deliverSmRespLatch.countDown(1);
              });
        }
      })
      .bind("localhost", 2776)
      .onSuccess(sess -> {
//        startPromise.complete();
        start[0] = System.currentTimeMillis();
        System.out.println("client bound");
        new Loop(vertx)
            .forLoopInt(0, SUBMIT_SM_NUMBER, i -> {
              var throttled = Promise.<Boolean>promise();
              submitSmCount[0]++;
              var ssm = new SubmitSm();
//              addShortMessage(ssm);
              var sendSubmitSmStart = new long[]{System.nanoTime()};
              sess.send(ssm)
                  .onSuccess(submitSmResp -> {
//System.out.println("got resp " + submitSmResp);
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
            .compose(v -> {
              var ret =  submitSmLatch.await(25, TimeUnit.SECONDS);
              submitEnd[0] = System.currentTimeMillis();
              return ret;
            })
            .compose(v -> {
              var ret = deliverSmRespLatch.await(90, TimeUnit.SECONDS);
              deliverEnd[0] = System.currentTimeMillis();
              return ret;
            })
            .onComplete(ar -> {
              System.out.println("done");
              var submitSmThroughput = ((double)submitSmRespCount[0]/((double)(submitEnd[0] - start[0])/1000.0));
              System.out.println(
                  "submitSm=" + submitSmCount[0] +
                  ", submitSmResp=" + submitSmRespCount[0] +
                      ", throughput=" + submitSmThroughput);
              System.out.println("submitSm latency=" + (0.000_001 * (double)submitSmLatencySumNano[0]/(double)submitSmRespCount[0]));

              var deliverSmThroughput = ((double)deliverSmRespCount[0]/((double)(deliverEnd[0] - start[0])/1000.0));
              System.out.println(
                  "deliverSm=" + deliverSmCount[0] +
                  ", deliverSmResp=" + deliverSmRespCount[0] +
                      ", throughput=" + deliverSmThroughput);
              System.out.println("deliverSmResp latency=" + (0.000_001 * (double)deliverSmRespLatencySumNano[0]/(double)deliverSmRespCount[0]));

              System.out.println("Overall throughput=" + (submitSmThroughput + deliverSmThroughput));
              System.out.println("Time=" + (submitEnd[0] - start[0]) + "ms");
//              startPromise.complete();
//              vertx.close();
            });
      })
      .onFailure(startPromise::fail);


    // make second session
//    client
//        .bind("localhost", 2776)
//        .onSuccess(sess -> {
//        });
  }

  private static final CharsetEncoder gsmEncoder = new Gsm7BitCharset("UTF-8", new String[]{"gsm"}).newEncoder();

  private void addShortMessage(SubmitSm ssm) {
    String text160 = "\u20AC Lorem [ipsum] dolor sit amet, consectetur adipiscing elit. Proin feugiat, leo id commodo tincidunt, nibh diam ornare est, vitae accumsan risus lacus sed sem metus.";
//    String text160 = "txId:" + UUID.randomUUID() + ";";
    byte[] textBytes = CharsetUtil.encode(text160, CharsetUtil.CHARSET_GSM);
//    byte[] textBytes = text160.getBytes(StandardCharsets.UTF_8);

//    byte[] textBytes = new byte[0];
//    try {
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
// vertex-smpp(1), text
//submitSm=1000000, submitSmResp=1000000, throughput=47947.83275795934
//submitSm latency=0.7100937028389999
//deliverSm=1000001, deliverSmResp=1000001, throughput=47945.58181905356
//deliverSmResp latency=0.05510074804025196
//Overall throughput=95893.4145770129
//Time=20856ms

// vertex-smpp(1), no text
//submitSm=1000000, submitSmResp=1000000, throughput=130174.43374121322
//submitSm latency=0.34885200490900004
//deliverSm=1000001, deliverSmResp=1000001, throughput=130157.6207210725
//deliverSmResp latency=0.06413832364967635
//Overall throughput=260332.0544622857
//Time=7682ms

// cloudhopper(1), text
//submitSm=1000000, submitSmResp=1000000, throughput=47959.330487746396
//submitSm latency=0.692746395918
//deliverSm=788751, deliverSmResp=788751, throughput=37822.528052172245
//deliverSmResp latency=0.04484290301628777
//Overall throughput=85781.85853991864
//Time=20851ms

// cloudhopper(1), no text
//submitSm=1000000, submitSmResp=1000000, throughput=139353.40022296543
//submitSm latency=0.33387468261099995
//deliverSm=113226, deliverSmResp=113226, throughput=15774.031763722485
//deliverSmResp latency=0.03360748752053415
//Overall throughput=155127.4319866879
//Time=7176ms

  public static void main(String[] args) {
    var vertex = Vertx.vertx();
    var depOpts = new DeploymentOptions()
      .setInstances(1) // TODO scale to 2 and more
      .setWorkerPoolSize(1)
      ;
    vertex.deployVerticle(SmppClientMain.class.getCanonicalName(), depOpts);
  }
}
