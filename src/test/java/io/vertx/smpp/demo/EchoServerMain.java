package io.vertx.smpp.demo;

import io.vertx.smpp.Smpp;
import io.vertx.smpp.SmppConstants;
import io.vertx.smpp.pdu.DeliverSm;
import io.vertx.smpp.pdu.SubmitSm;
import io.vertx.smpp.pdu.SubmitSmResp;
import io.vertx.smpp.server.SmppServer;
import io.vertx.smpp.server.SmppServerOptions;
import io.vertx.smpp.types.SmppInvalidArgumentException;
import io.vertx.core.*;
import io.vertx.core.net.JksOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class EchoServerMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(EchoServerMain.class);

  private static final int     INSTANCES = 1;
  private static final int     THREADS = 1;
  private static final long    SUBMIT_SM_RESP_DELAY = 0;
  private static final boolean SEND_DELIVERY = true;
  private static final long    DELIVER_SM_RESP_DELAY = 0;
  private static final boolean SSL = false;
  private static final boolean REFUSE_ALL_BINDS = false;

  @Override
  public void start(Promise<Void> startPromise) {
    var opts = new SmppServerOptions();
    if (SSL) {
      opts.setSsl(true)
          .setKeyStoreOptions(
              new JksOptions()
                  .setPath("src/test/resources/keystore")
                  .setPassword("changeit"));
    }
    var server = Smpp.server(vertx, opts);
    server
        .configure(cfg -> {
          log.info("user code: configuring new session");
          cfg.setSystemId("vertx-smpp-server");
          cfg.setWindowSize(600);
          cfg.setWriteTimeout(2000);
          cfg.setRequestExpiryTimeout(1000); // Duration for send request and receive response
          cfg.onBindReceived(bindInfo -> {
            var systemId = bindInfo.getBindRequest().getSystemId();
            var password = bindInfo.getBindRequest().getPassword();
            log.info("user code: inbound bind from " + systemId);
            if (check(systemId, password)) {
              return SmppConstants.STATUS_OK;
            } else {
              return SmppConstants.STATUS_BINDFAIL;
            }
          });
          cfg.onCreated(sess -> log.info("user code: session#{} created, bound to {}", sess.getId(), sess.getBoundToSystemId()));
          cfg.onRequest(reqCtx -> {
            var sendSubmitSmRespTask = (Runnable) () -> {
              var sess = reqCtx.getSession();
              var messageId = UUID.randomUUID().toString();
              var submitSmResp = (SubmitSmResp) reqCtx.getRequest().createResponse();
              submitSmResp.setMessageId(messageId);
              sess.reply(submitSmResp)
                  .onSuccess(nothing -> {
                    if (SEND_DELIVERY && reqCtx.getRequest() instanceof SubmitSm) {
                      var sendDeliverSmTask = (Runnable)() -> {
                        if (!sess.isClosed()) {
                          sess.send(createDeliverSm(messageId))
                              .onSuccess(resp -> {})
                              .onFailure(e -> log.error("cannot send deliver_sm, error: {}", e.getMessage()));
                        } else {
                          log.error("user code: session#{} is closed, cannot send deliver_sm", sess.getId());
                        }
                      };
                      if (DELIVER_SM_RESP_DELAY > 0) {
                        vertx.setTimer(DELIVER_SM_RESP_DELAY, id -> sendDeliverSmTask.run());
                      } else {
                        sendDeliverSmTask.run();
                      }
                    }
                  })
                  .onFailure(e -> log.error("cannot replay with submit_sm_resp, error: {}", e.getMessage()));
            };

            if (SUBMIT_SM_RESP_DELAY > 0) {
              vertx.setTimer(SUBMIT_SM_RESP_DELAY, id -> sendSubmitSmRespTask.run());
            } else {
              sendSubmitSmRespTask.run();
            }
          });
          cfg.onClosed(sess -> log.info("user code: session#{} closed", sess.getId()));
          cfg.onForbiddenRequest(reqCtx -> log.info("user code: forbidden req {}", reqCtx.getRequest().getName()));
          cfg.onForbiddenResponse(rspCtx -> log.info("user code: forbidden rsp {}", rspCtx.getResponse().getName()));
        })
        .start("localhost", SSL? 2777: 2776)
        .onSuccess(done -> {
          log.info("Server online, ssl {}", (SSL?"on": "off"));
          startPromise.complete();
        })
        .onFailure(e -> {
          log.error("cannot start server", e);
          vertx.close();
        });

    onShutdown(vertx, server);
  }

  private DeliverSm createDeliverSm(String messageId) {
    var deliverSm = new DeliverSm();
    try {
      deliverSm.setShortMessage(messageId.getBytes());
    } catch (SmppInvalidArgumentException ignores) {}
    return deliverSm;
  }

  private boolean check(String systemId, String password) {
    return !REFUSE_ALL_BINDS;
  }

  public static void main(String[] args) {
    var vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(THREADS));
    vertx.deployVerticle(EchoServerMain.class.getCanonicalName(), new DeploymentOptions().setInstances(INSTANCES));
  }

  private static void onShutdown(Vertx vertx, SmppServer server) {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      var closePromise = Promise.<Void>promise();
      var latch = new CountDownLatch(1);
      if (server.isListening()) {
        server.close(closePromise);
        closePromise.future()
            .onComplete(ar -> {
              log.info("Server offline");
              vertx.close()
                  .onComplete(unused -> {
                    log.debug("vertx closed");
                    latch.countDown();
                  });
            });
      } else {
        log.info("Server was not listening");
        vertx.close()
            .onComplete(unused -> {
              log.debug("vertx closed");
              latch.countDown();
            });
      }
      try {
        log.debug("waiting for server and vertx to shutdown");
        latch.await();
      } catch (InterruptedException e) {
        log.error("shutdown interrupted", e);
      }
    }));
  }
}
