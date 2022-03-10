package com.example.smpp.session;

import com.example.smpp.PduRequestContext;
import com.example.smpp.PduResponseContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Handler;

public interface SessionCallbacksView {
  Handler<SmppSession> getOnCreated();
  Handler<PduRequestContext<?>> getOnRequest();
  Handler<SmppSession> getOnClose();
  Handler<PduResponseContext> getOnUnexpectedResponse();
  Handler<SmppSession> getOnUnexpectedClose();
  Handler<PduRequestContext<?>> getOnBindReceived();
}
