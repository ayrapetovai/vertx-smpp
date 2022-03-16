package com.example.smpp.util;

import com.example.smpp.model.SendPduExceptionType;

public class SendBindRefusedException extends SendPduFailedException {

  private final int status;

  public SendBindRefusedException(String message, int status) {
    super(message);
    this.status = status;
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.BIND_REFUSED;
  }

  public int getStatus() {
    return status;
  }
}
