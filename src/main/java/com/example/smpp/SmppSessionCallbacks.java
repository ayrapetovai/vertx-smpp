package com.example.smpp;

import io.vertx.core.Handler;

public class SmppSessionCallbacks {
  public Handler<PduRequestContext<?>> requestHandler;
}
