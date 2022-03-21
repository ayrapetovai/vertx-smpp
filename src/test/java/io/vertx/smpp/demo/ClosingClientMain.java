package io.vertx.smpp.demo;

import io.vertx.smpp.Smpp;
import io.vertx.smpp.model.SmppBindType;
import io.vertx.smpp.pdu.DeliverSm;
import io.vertx.smpp.pdu.SubmitSm;
import io.vertx.smpp.util.core.CountDownLatch;
import io.vertx.smpp.util.core.FlowControl;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClosingClientMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(ClosingClientMain.class);

  private static final String  SYSTEM_ID = "vertx-smpp-client";
  private static final int     SESSIONS = 1;
  private static final int     THREADS = 1;
  private static final int     SUBMIT_SM_NUMBER = 22;

  @Override
  public void start(Promise<Void> startPromise) {

    var closeLatch = new CountDownLatch(vertx, SUBMIT_SM_NUMBER/2);

    var discardedCount = new int[]{0};
    var requestCount = new int[]{0};
    var responseCount = new int[]{0};

    Smpp.client(vertx)
        .configure(cfg -> {
          cfg.setSystemId(SYSTEM_ID);
          cfg.setPassword("test");
          cfg.setBindType(SmppBindType.TRANSCEIVER);
          cfg.setUnbindTimeout(5000);
          cfg.setDiscardAllOnUnbind(false);
          cfg.onRequest(req -> {
            if (req.getRequest() instanceof DeliverSm) {
              req.getSession().reply(req.getRequest().createResponse());
            }
          });
        })
        .bind("localhost", 2776)
        .onSuccess(sess -> {
          log.info("user code: client bound");
          FlowControl
              .forLoopInt(vertx.getOrCreateContext(), 0, SUBMIT_SM_NUMBER, i -> {
                closeLatch.countDown(1);
                var ssm = new SubmitSm();
                if (!sess.isClosed()) {
                  requestCount[0]++;
                  sess.send(ssm)
                      .onSuccess(resp -> responseCount[0]++)
//                      .onComplete(v -> submitSmLatch.countDown(1))
                      .onDiscarded(v -> {
                        discardedCount[0]++;
                        log.warn("!!! Discarded: {}", ssm);
                      });
                }
              });

          // Close session right after half of requests were send
          // to get some requests stuck in the session window, check
          // they must be discarded
          closeLatch.await()
              .compose(v -> {
                var closePromise = Promise.<Void>promise();
                log.info("closing session after one half of requests been passed");
                sess.close(closePromise);
                return closePromise.future();
              })
              .onComplete(ar -> {
                log.info("close({}) send req/resp={}/{}, discarded={}",
                    (ar.succeeded()? "succeed": "failed"), requestCount[0], responseCount[0], discardedCount[0]);
                startPromise.complete();
              });
        })
        .onFailure(e -> {
          log.error("user code: client cannot bind", e);
          startPromise.complete();
        });
  }

  public static void main(String[] args) {
    var vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(THREADS));
    vertx.deployVerticle(ClosingClientMain.class.getCanonicalName(), new DeploymentOptions().setInstances(SESSIONS))
        .onComplete(arId -> {
          log.info("closing vertx {}", arId.result());
          vertx.close();
        });
  }
}
