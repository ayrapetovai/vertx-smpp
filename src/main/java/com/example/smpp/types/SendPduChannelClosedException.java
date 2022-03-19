package com.example.smpp.types;

import com.example.smpp.model.SendPduExceptionType;

public class SendPduChannelClosedException extends SendPduFailedException {

  public SendPduChannelClosedException(String message) {
    super(message);
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.CHANNEL_CLOSED;
  }
}