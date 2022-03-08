package com.example.smpp.session;

import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Handler;

public interface SessionCallbacks {
  void onCreated(Handler<SmppSession> createdHandler);
  void onRequest(Handler<PduRequestContext<?>> requestHandler);
  void onUnexpectedResponse(Handler<PduResponse> unexpectedResponseHandler);
  void onClose(Handler<SmppSession> closeHandler);
  void onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler);
}
