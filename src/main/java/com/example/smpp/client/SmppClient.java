package com.example.smpp.client;

import com.example.smpp.PduRequestContext;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface SmppClient {
  // smpp builder's static API
  // --> SmppSession
  SmppClient onRequest(Handler<PduRequestContext<?>> pduRequestHandler);
  // ---> SmppSession
// SmppServer onUnexpectedClose();

  // API for built smpp client
  Future<SmppClientSession> bind(String host, int port);
//  Future<SmppClientSession> bind(Handler<UnboundSmppSession> configurator); // *thumb up*

//  Future<SmppClientSession> bindTranceiver(String host, int port);
//  Future<SmppClientSession> bindReceiver(String host, int port);
//  Future<SmppClientSession> bindTrasmitter(String host, int port);
}
