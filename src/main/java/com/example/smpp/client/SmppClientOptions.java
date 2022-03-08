package com.example.smpp.client;

import io.vertx.core.net.NetClientOptions;

public class SmppClientOptions extends NetClientOptions {
  private String host;
  private int port;
  private long connectTimeout;
  private byte interfaceVersion;
}
