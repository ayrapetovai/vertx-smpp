package com.example.smpp.client;

import io.vertx.core.net.NetClientOptions;

// R - хотелось бы менять в рантайме
// S - это поле должно иметь одно и тоже значение на протяжении всего времени работы сессии
public class SmppClientOptions extends NetClientOptions {
// S  private String name; // per session

// configuration settings

// S  private SmppBindType type;  // per session
// S  private String systemId;  // per session
// S  private String password;  // per session
// S  private String systemType;  // per session
// S  private byte interfaceVersion;  // all sessions, interface version requested by us or them
// S  private Address addressRange;  // per session: ton, npi, address
// S  private long bindTimeout;       //  per session, length of time to wait for a bind response

//    // if > 0, then activated
// R  private long requestExpiryTimeout; // per session
// R  private int windowSize; // per session
// R  private long windowWaitTimeout; // per session
// R  private long windowMonitorInterval;// per session (request expire check interval)
// R  private long writeTimeout; // per session
// R  private boolean countersEnabled; // per session (metrics enabled)
// R  private LoggingOptions loggingOptions; // per session: log_pdu, log_bytes or not
}
