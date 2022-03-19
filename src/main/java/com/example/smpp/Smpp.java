package com.example.smpp;

import com.example.smpp.client.SmppClient;
import com.example.smpp.client.SmppClientImpl;
import com.example.smpp.client.SmppClientOptions;
import com.example.smpp.server.SmppServer;
import com.example.smpp.server.SmppServerImpl;
import com.example.smpp.server.SmppServerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;

public interface Smpp {

  static SmppServer server(Vertx vertx) {
    return new SmppServerImpl((VertxInternal) vertx, new SmppServerOptions());
  }

  static SmppServer server(Vertx vertx, SmppServerOptions options) {
    return new SmppServerImpl((VertxInternal) vertx, options);
  }

  static SmppClient client(Vertx vertx) {
    return new SmppClientImpl((VertxInternal) vertx, new SmppClientOptions(), new CloseFuture());
  }

  static SmppClient client(Vertx vertx, SmppClientOptions options) {
    return new SmppClientImpl((VertxInternal) vertx, options, new CloseFuture());
  }

}
