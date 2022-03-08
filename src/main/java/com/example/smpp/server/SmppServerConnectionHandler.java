package com.example.smpp.server;

import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Handler;

public class SmppServerConnectionHandler  implements Handler<SmppSession> {
  public Handler<SmppSession> connectionHandler = sess -> {};
  public Handler<PduRequestContext<?>> requestHandler = req -> {};
  public Handler<PduResponse> responseHandler = res -> {};

  public SmppServerConnectionHandler(SmppServerImpl smppServer) {

  }

  @Override
  public void handle(SmppSession sess) {
//    sess.handler = this;
  }
}
