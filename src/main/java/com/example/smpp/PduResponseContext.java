package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduResponse;

public class PduResponseContext {
  private final PduResponse response;
  private final SmppSession session;

  public PduResponseContext(PduResponse response, SmppSession session) {
    this.response = response;
    this.session = session;
  }

  public PduResponse getResponse() {
    return response;
  }

  public SmppSession getSession() {
    return session;
  }

  @Override
  public String toString() {
    return response.toString();
  }
}
