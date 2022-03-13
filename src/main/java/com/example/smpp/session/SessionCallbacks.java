package com.example.smpp.session;

import com.example.smpp.PduRequestContext;
import com.example.smpp.PduResponseContext;
import com.example.smpp.SmppSession;
import com.example.smpp.model.BindInfo;
import io.vertx.core.Handler;

import java.util.function.Function;

// TODO
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);
public interface SessionCallbacks {
  void onCreated(Handler<SmppSession> createdHandler);
  void onRequest(Handler<PduRequestContext<?>> requestHandler);
  void onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler);
  void onClose(Handler<SmppSession> closeHandler);
  void onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler);
  void onBindReceived(Function<BindInfo, Integer> onBindReceived);
  void onForbiddenRequest(Handler<PduRequestContext<?>> onForbiddenRequest);
  void onForbiddenResponse(Handler<PduResponseContext> onForbiddenResponse);
}
