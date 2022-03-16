package com.example.smpp.util;

import com.example.smpp.model.SendPduExceptionType;

public abstract class SendPduFailedException extends Exception {
  public SendPduFailedException(String message) {
    super(message);
  }
  public abstract SendPduExceptionType getType();

}
