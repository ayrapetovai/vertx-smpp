package com.example.smpp.util;

import com.example.smpp.model.SendPduExceptionType;

public class SendPduWindowTimeoutException extends SendPduFailedException {

  private final int windowSize;

  public SendPduWindowTimeoutException(String message, int windowSize) {
    super(message);
    this.windowSize = windowSize;
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.WINDOW_TIMEOUT;
  }

  public int getWindowSize() {
    return windowSize;
  }
}
