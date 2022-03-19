package com.example.smpp.types;

import com.example.smpp.model.SendPduExceptionType;

public class SendPduDiscardedException extends SendPduFailedException {

  public SendPduDiscardedException(String message) {
    super(message);
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.REQUEST_DISCARDED_ON_CLOSE;
  }
}
