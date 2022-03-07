package com.example.smpp.main;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.example.smpp.Smpp;
import com.example.smpp.client.SmppClient;
import com.example.smpp.util.Gsm7BitCharset;
import com.example.smpp.util.Loop;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmppClientMain extends AbstractVerticle {

//  private static final int SUBMIT_SM_NUMBER = 50_000_000;
//  private static final int SUBMIT_SM_NUMBER = 10_000_000;
//  private static final int SUBMIT_SM_NUMBER = 2_000_000;
  private static final int SUBMIT_SM_NUMBER = 1_000_000;
//  private static final int SUBMIT_SM_NUMBER = 100_000;
//  private static final int SUBMIT_SM_NUMBER = 20_000;
//  private static final int SUBMIT_SM_NUMBER = 10_000;
//  private static final int SUBMIT_SM_NUMBER = 1_000;
//  private static final int SUBMIT_SM_NUMBER = 10;
//  private static final int SUBMIT_SM_NUMBER = 4;
//  private static final int SUBMIT_SM_NUMBER = 1;
  SmppClient client;
  ExecutorService executorService = Executors.newSingleThreadExecutor();

  @Override
  public void start(Promise<Void> startPromise) {

    var start = new long[]{0};
    var end = new long[]{0};
    var submitSmLatencySumNano = new long[]{0};
    var submitSmRespCount = new long[]{0};
    var submitSmCount = new long[]{0};
    var deliverSmRespCount = new long[]{0};
    var deliverSmCount = new long[]{0};

    client = Smpp.client(vertx);
    client
      .onRequest(req -> {
        // FIXME some req.getRequest() are null
        deliverSmCount[0]++;
        req.getSession()
          .reply(req.getRequest().createResponse())
            .onSuccess(nothing -> {
              deliverSmRespCount[0]++;
            });
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
              addShortMessage(ssm);
              var sendSubmitSmStart = new long[]{System.nanoTime()};
              sess.send(ssm)
                  .onSuccess(submitSmResp -> {
//System.out.println("got resp " + submitSmResp);
                    submitSmLatencySumNano[0] += (System.nanoTime() - sendSubmitSmStart[0]);
                    submitSmRespCount[0]++;

                    throttled.complete(false);
                  })
                  .onFailure(e -> {
                    e.printStackTrace();
                    throttled.complete(true);
                  });
              return throttled.future();
            })
            .onComplete(v -> {
              end[0] = System.currentTimeMillis();
              System.out.println("done");
              var submitSmThroughput = ((double)submitSmRespCount[0]/((double)(end[0] - start[0])/1000.0));
              System.out.println(
                  "submitSm=" + submitSmCount[0] +
                  ", submitSmResp=" + submitSmRespCount[0] +
                      ", throughput=" + submitSmThroughput);
              System.out.println("submitSm latency=" + (0.000_001 * (double)submitSmLatencySumNano[0]/(double)submitSmRespCount[0]));

              var deliverSmThroughput = ((double)deliverSmRespCount[0]/((double)(end[0] - start[0])/1000.0));
              System.out.println(
                  "deliverSm=" + deliverSmCount[0] +
                  ", deliverSmResp=" + deliverSmRespCount[0] +
                      ", throughput=" + deliverSmThroughput);

              System.out.println("Overall throughput=" + (submitSmThroughput + deliverSmThroughput));
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
//    String text160 = "\u20AC ";
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
// cloudhopper
//submitSm=1000000, submitSmResp=999970, throughput 143221.1400744772
//submitSm latency=0.27763656144584337
//deliverSm=0, deliverSmResp=0, throughput 0.0

// vertex-smpp, no text
//submitSm=1000000, submitSmResp=999975, throughput 139039.90545050055
//submitSm latency=0.30520423226480664
//deliverSm=0, deliverSmResp=0, throughput 0.0

// vertex-smpp, text
//submitSm=1000000, submitSmResp=999955, throughput 57435.66915565767
//submitSm latency=0.5932053640283812
//deliverSm=0, deliverSmResp=0, throughput 0.0

// vertex-smpp(3), text
//submitSm=3000000, submitSmResp=2999958, throughput 91000
//submitSm latency=1.1380006381772763
//deliverSm=0, deliverSmResp=0, throughput 0.0

// vertex-smpp(3), text
//2022-03-06 20:25:00,759 [main] WARN  c.c.s.demo.PerformanceClientMain - Performance client finished:
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain -        Sessions: 3
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain -     Window Size: 1000
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain - Sessions Failed: 0
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain -            Time: 19945 ms
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain -   Target Submit: 3000000
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain -   Actual Submit: 3000000
//2022-03-06 20:25:00,761 [main] WARN  c.c.s.demo.PerformanceClientMain -  Actual Deliver: 0
//2022-03-06 20:25:00,762 [main] WARN  c.c.s.demo.PerformanceClientMain - ssm  Throughput: 150413,638 per sec
//2022-03-06 20:25:00,762 [main] WARN  c.c.s.demo.PerformanceClientMain - dsm  Throughput: 0,000 per sec
//2022-03-06 20:25:00,762 [main] WARN  c.c.s.demo.PerformanceClientMain - All  Throughput: 150413,638 per sec
  public static void main(String[] args) {
    var vertex = Vertx.vertx();
    var depOpts = new DeploymentOptions()
      .setInstances(1) // TODO scale to 2 and more
      .setWorkerPoolSize(1)
      ;
    vertex.deployVerticle(SmppClientMain.class.getCanonicalName(), depOpts);
  }
}
