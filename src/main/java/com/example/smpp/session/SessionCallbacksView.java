package com.example.smpp.session;

import com.example.smpp.PduRequestContext;
import com.example.smpp.PduResponseContext;
import com.example.smpp.SmppSession;
import com.example.smpp.model.BindInfo;
import io.vertx.core.Handler;

import java.util.function.Function;

public interface SessionCallbacksView {
  Handler<SmppSession> getOnCreated();
  Handler<PduRequestContext<?>> getOnRequest();
  Handler<SmppSession> getOnClose();
  Handler<PduResponseContext> getOnUnexpectedResponse();
  Handler<SmppSession> getOnUnexpectedClose();
  Function<BindInfo, Integer> getOnBindReceived();
  Handler<PduRequestContext<?>> getOnForbiddenRequest();
  Handler<PduResponseContext> getOnForbiddenResponse();
}
