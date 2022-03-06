package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;

public class PduRequestContext<T extends PduResponse> {
  private final PduRequest<T> request;
  private final SmppSession session;

  public PduRequestContext(PduRequest<T> request, SmppSession session) {
    this.request = request;
    this.session = session;
  }

  public PduRequest<T> getRequest() {
    return request;
  }

  public SmppSession getSession() {
    return session;
  }

  @Override
  public String toString() {
    return request.toString();
  }
}
