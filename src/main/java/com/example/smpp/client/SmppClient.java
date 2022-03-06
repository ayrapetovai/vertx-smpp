package com.example.smpp.client;

import com.example.smpp.PduRequestContext;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface SmppClient {
  // smpp builder's static API
  SmppClient onRequest(Handler<PduRequestContext<?>> pduRequestHandler);

  // API for built smpp client
  Future<SmppClientSession> bind(String host, int port);
}
