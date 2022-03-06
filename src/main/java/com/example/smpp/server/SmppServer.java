package com.example.smpp.server;

import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface SmppServer {
  // smpp builder's static API
  SmppServer onSessionCreated(Handler<SmppSession> sessionCreatedHandler);
  SmppServer onRequest(Handler<PduRequestContext<?>> pduRequestHandler);

  // API for built smpp server
  Future<SmppServer> start(String host, int port);
}
