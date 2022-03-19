package com.example.smpp.util;

import com.example.smpp.model.SendPduExceptionType;

public class SendPduNackkedException extends SendPduFailedException {

  private final String resultMessage;
  private final int status;

  public SendPduNackkedException(String message, String resultMessage, int status) {
    super(message);
    this.resultMessage = resultMessage;
    this.status = status;
  }

  public String getResultMessage() {
    return resultMessage;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.GENERIC_NACK_RECEIVED;
  }
}
