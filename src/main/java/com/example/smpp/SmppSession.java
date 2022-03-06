package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import io.vertx.core.Future;

public interface SmppSession {
  <T extends PduResponse> Future<T>  send(PduRequest<T> req);
  Future<Void> reply(PduResponse pduResponse);
}
