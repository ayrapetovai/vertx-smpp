package com.example.smpp.session;

import com.example.smpp.PduRequestContext;
import com.example.smpp.PduResponseContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Handler;

// TODO
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);
public interface SessionCallbacks {
  void onCreated(Handler<SmppSession> createdHandler);
  void onRequest(Handler<PduRequestContext<?>> requestHandler);
  void onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler);
  void onClose(Handler<SmppSession> closeHandler);
  void onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler);
  void onBindReceived(Handler<PduRequestContext<?>> bindReceived);
}
