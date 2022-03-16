package com.example.smpp.util;

import com.example.smpp.model.SendPduExceptionType;
import com.example.smpp.model.SmppSessionState;

public class SendPduWrongOperationException extends SendPduFailedException {

  private final SmppSessionState state;

  public SendPduWrongOperationException(String message, SmppSessionState state) {
    super(message);
    this.state = state;
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.WRONG_OPERATION;
  }

  public SmppSessionState getState() {
    return state;
  }
}
