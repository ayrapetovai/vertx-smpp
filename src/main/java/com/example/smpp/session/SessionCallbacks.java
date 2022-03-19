package com.example.smpp.session;

import com.example.smpp.types.PduRequestContext;
import com.example.smpp.types.PduResponseContext;
import com.example.smpp.types.BindInfo;
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
