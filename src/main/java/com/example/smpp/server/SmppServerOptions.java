package com.example.smpp.server;

import io.vertx.core.net.NetServerOptions;

public class SmppServerOptions extends NetServerOptions {
  private String name; // for identification on local JVM?
  private int maxConnectionSize;
  private boolean autoNegotiateInterfaceVersion;
  private byte interfaceVersion; // smpp version the server supports
  private boolean reuseAddress;
  private boolean jmxEnabled;
  private String jmxDomain;
}
