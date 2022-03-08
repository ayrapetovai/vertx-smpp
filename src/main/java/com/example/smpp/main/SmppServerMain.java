package com.example.smpp.main;

import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.example.smpp.Smpp;
import com.example.smpp.server.SmppServer;
import com.example.smpp.server.SmppServerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.JksOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class SmppServerMain extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(SmppServerMain.class);

  SmppServer server;

  @Override
  public void start(Promise<Void> startPromise) {
    var opts = new SmppServerOptions();
//    opts
//      .setSsl(true)
//      .setKeyStoreOptions(
//        new JksOptions()
//          .setPath("server-keystore.jks")
//          .setPassword("wibble")
//      );
    server = Smpp.server(vertx, opts);
    server
      .onSessionCreated(sess -> {
        log.info("created session#{}", sess.getId());
      })
      .onRequest(reqCtx -> {
          var sess = reqCtx.getSession();
          sess
              .reply(reqCtx.getRequest().createResponse())
              .onSuccess(nothing -> {
                if (reqCtx.getRequest() instanceof SubmitSm) {
                  sess.send(new DeliverSm())
                      .onSuccess(resp -> {
                      })
                      .onFailure(Throwable::printStackTrace);
                }
              })
              .onFailure(Throwable::printStackTrace);
      })
      .start("localhost", 2776)
//      .start("localhost", 2777)
      .onSuccess(done -> {
        log.info("Server online");
        startPromise.complete();
      })
      .onFailure(startPromise::fail);

    onShutdown(vertx, server);
  }

  public static void main(String[] args) {
    var vertex = Vertx.vertx();
    var depOpts = new DeploymentOptions()
      .setInstances(1) // TODO scale to 2 and more
      .setWorkerPoolSize(1)
      ;
    vertex.deployVerticle(SmppServerMain.class.getCanonicalName(), depOpts);
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
