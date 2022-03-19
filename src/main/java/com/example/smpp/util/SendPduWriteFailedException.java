package com.example.smpp.util;

import com.example.smpp.model.SendPduExceptionType;

public class SendPduWriteFailedException extends SendPduFailedException {

  public SendPduWriteFailedException(String message, Exception cause) {
    super(message, cause);
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.WRITE_TO_CHANNEL_FAILED;
  }
}
