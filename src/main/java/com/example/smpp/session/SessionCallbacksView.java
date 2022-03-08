package com.example.smpp.session;

import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Handler;

public interface SessionCallbacksView {
  Handler<SmppSession> getOnCreated();
  Handler<PduRequestContext<?>> getOnRequest();
  // TODO PduResponse -> PduResponseContext
  Handler<PduResponse> getOnUnexpectedResponse();
  Handler<SmppSession> getOnClose();
  Handler<SmppSession> getOnUnexpectedClose();
}
