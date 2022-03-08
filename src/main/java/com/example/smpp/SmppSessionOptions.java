package com.example.smpp;

interface ClientSessionConfigurator {

}

interface ServerSessionConfigurator {

}

interface SessionView {

}

public class SmppSessionOptions {
// blueprint
// S  private String name; // per session
// S  private SmppBindType type;  // per session
// S  private String systemId;  // per session
// S  private String password;  // per session
// S  private String systemType;  // per session
// S  private Address addressRange;  // per session: ton, npi, address

// R  boolean dropAllOnUnbind = false; // per session? При получении unbind, не досылать resp и другие pdu, сразу убить сессию
// R  boolean replyToUnbind = true; //per session? В ответ на unbind посылать unbind_resp
// R  Duration bindTimeout; // per session? Время, за которое сервер выдаст bind_resp
// R  Duration unbindTimeout; // per session? Время, которе дается клиенту на отправку unbind_resp, по истечении все pdu в окне дропаются и соединение закрывается
// R  private long requestExpiryTimeout; // per session
// R  private int windowSize; // per session
// R  private long windowWaitTimeout; // per session
// R  private long windowMonitorInterval;// per session (request expire check interval)
// R  private long writeTimeout; // per session
// R  private boolean countersEnabled; // per session (metrics enabled)
// R  private LoggingOptions loggingOptions; // per session: log_pdu, log_bytes or not
}
