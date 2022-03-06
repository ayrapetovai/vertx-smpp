package com.example.smpp.main;

import com.cloudhopper.smpp.pdu.DeliverSm;
import com.example.smpp.Smpp;
import com.example.smpp.server.SmppServer;
import com.example.smpp.server.SmppServerImpl;
import com.example.smpp.server.SmppServerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.net.JksOptions;

public class SmppServerMain extends AbstractVerticle {

  SmppServer server;

  @Override
  public void start(Promise<Void> startPromise) {
    var opts = new SmppServerOptions();
    opts
      .setSsl(true)
      .setKeyStoreOptions(
        new JksOptions()
          .setPath("server-keystore.jks")
          .setPassword("wibble")
      );
    server = Smpp.server(vertx, opts);
    server
      .onSessionCreated(sess -> {
        System.out.println("session created " + sess);
      })
      .onRequest(req -> {
        var sess = req.getSession(); // FIXME some req.getRequest() are null
        sess
          .reply(req.getRequest().createResponse())
          .onSuccess(nothing -> {
            vertx.runOnContext(__ -> {
              sess.send(new DeliverSm())
                .onSuccess(resp -> {});
            });
          });
      })
      .start("localhost", 2776)
//      .start("localhost", 2777)
      .onSuccess(done -> {
        System.out.println("Server online");
        startPromise.complete();
      })
      .onFailure(startPromise::fail);
  }

  public static void main(String[] args) {
    var vertex = Vertx.vertx();
    var depOpts = new DeploymentOptions()
      .setInstances(1) // TODO scale to 2 and more
      .setWorkerPoolSize(1)
      ;
    vertex.deployVerticle(SmppServerMain.class.getCanonicalName(), depOpts);
  }
}
