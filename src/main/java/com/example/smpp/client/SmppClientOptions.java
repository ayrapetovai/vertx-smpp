package com.example.smpp.client;

import io.vertx.core.net.NetClientOptions;

// R - хотелось бы менять в рантайме
// S - это поле должно иметь одно и тоже значение на протяжении всего времени работы сессии
public class SmppClientOptions extends NetClientOptions {
// blueprint
// S  private String host;  // per client
// S  private int port;  // per client
// S  private long connectTimeout;  // per client
// S  private byte interfaceVersion;  // per client


// draft
// S  private SmppBindType type;  // per session
// S  private String systemId;  // per session
// S  private String password;  // per session
// S  private String systemType;  // per session
// S  private Address addressRange;  // per session: ton, npi, address
// S  private long bindTimeout;       //  per session, length of time to wait for a bind response
// R  private long requestExpiryTimeout; // per session
// R  private int windowSize; // per session
// R  private long windowWaitTimeout; // per session
// R  private long windowMonitorInterval;// per session (request expire check interval)
// R  private long writeTimeout; // per session
// R  private boolean countersEnabled; // per session (metrics enabled)
// R  private LoggingOptions loggingOptions; // per session: log_pdu, log_bytes or not
}
