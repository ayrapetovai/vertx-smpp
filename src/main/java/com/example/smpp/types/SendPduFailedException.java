package com.example.smpp.types;

import com.example.smpp.model.SendPduExceptionType;

public abstract class SendPduFailedException extends Exception {

  public SendPduFailedException(String message) {
    super(message);
  }

  public SendPduFailedException(String message, Exception cause) {
    super(message, cause);
  }

  public abstract SendPduExceptionType getType();

}
