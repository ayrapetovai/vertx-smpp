package com.example.smpp.demo;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.example.smpp.Smpp;
import com.example.smpp.server.SmppServer;
import com.example.smpp.server.SmppServerOptions;
import io.vertx.core.*;
import io.vertx.core.net.JksOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class EchoServerMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(EchoServerMain.class);

  private static final int INSTANCES = 1;
  private static final int THREADS = 1;
  private static final int SUBMIT_SM_RESP_DELAY = 0;
  private static final boolean SSL = false;
  private static final boolean REFUSE_ALL_BINDS = false;

  private SmppServer server;

  @Override
  public void start(Promise<Void> startPromise) {
    var clientName = new String[]{null};
    var opts = new SmppServerOptions();
    if (SSL) {
      opts.setSsl(true)
          .setKeyStoreOptions(
              new JksOptions()
                  .setPath("src/test/resources/keystore")
                  .setPassword("changeit"));
    }
    server = Smpp.server(vertx, opts);
    server
        .configure(cfg -> {
          log.info("user code: configuring new session");
          cfg.setSystemId("vertx-smpp-server");
          cfg.setWindowSize(600);
          cfg.setWriteTimeout(2000);
          cfg.setRequestExpiryTimeout(1000); // Время на отправку запроса и получение ответа
          cfg.onBindReceived(bindInfo -> {
            var systemId = bindInfo.getBindRequest().getSystemId();
            var password = bindInfo.getBindRequest().getPassword();
            log.info("user code: inbound bind from " + systemId);
            if (check(systemId, password)) {
              clientName[0] = systemId;
              return SmppConstants.STATUS_OK;
            } else {
              return SmppConstants.STATUS_BINDFAIL;
            }
          });
          cfg.onCreated(sess -> {
            log.info("user code: session#{} created, bound to {}", sess.getId(), sess.getBoundToSystemId());
          });
          cfg.onRequest(reqCtx -> {
//            if (reqCtx.getRequest() instanceof SubmitSm) {
//              try {
//                Thread.sleep(5);
//              } catch (InterruptedException e) {
//                e.printStackTrace();
//              }
//            }

            var answering = (Runnable) () -> {
              var sess = reqCtx.getSession();
              sess.reply(reqCtx.getRequest().createResponse())
                  .onSuccess(nothing -> {
                    if (reqCtx.getRequest() instanceof SubmitSm) {
                      sess.send(new DeliverSm())
                          .onSuccess(resp -> { })
                          .onFailure(Throwable::printStackTrace);
                    }
                  })
                  .onFailure(Throwable::printStackTrace);
            };

            if (SUBMIT_SM_RESP_DELAY > 0) {
              vertx.setTimer(SUBMIT_SM_RESP_DELAY, id -> {
                answering.run();
              });
            } else {
              answering.run();
            }
          });
          cfg.onClose(sess -> {
            log.info("user code: session#{} closed", sess.getId());
          });
          cfg.onForbiddenRequest(reqCtx -> {
            log.info("user code: forbidden req {}", reqCtx.getRequest().getName());
          });
          cfg.onForbiddenResponse(rspCtx -> {
            log.info("user code: forbidden rsp {}", rspCtx.getResponse().getName());
          });
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
