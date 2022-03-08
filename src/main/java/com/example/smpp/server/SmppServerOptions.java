package com.example.smpp.server;

import io.vertx.core.net.NetServerOptions;

// R - хотелось бы менять в рантайме
// S - это поле должно иметь одно и тоже значение на протяжении всего времени работы сессии
public class SmppServerOptions extends NetServerOptions {
// R  boolean dropAllOnUnbind = false; // per session? При получении unbind, не досылать resp и другие pdu, сразу убить сессию
// R  boolean replyToUnbind = true; //per session? В ответ на unbind посылать unbind_resp
// R  Duration bindTimeout; // per session? Время, за которое сервер выдаст bind_resp
// R  Duration unbindTimeout; // per session? Время, которе дается клиенту на отправку unbind_resp, по истечении все pdu в окне дропаются и соединение закрывается

// R  private int windowSize; // per session?
// S  private String name; // for identification on local JVM?
// S  private String systemId; // per session?

// R  private long defaultWindowWaitTimeout; // per session? (aquire(..., TimeUnit.))
// R  private long defaultRequestExpiryTimeout; // per session?
// R  private long defaultWindowMonitorInterval; // per session?
// R  private boolean defaultSessionCountersEnabled = false; // per session?

// R  private int maxConnectionSize;
// S  private boolean autoNegotiateInterfaceVersion;
// S  private byte interfaceVersion;// smpp version the server supports
// S  private boolean reuseAddress;

// S  private boolean jmxEnabled;
// S  private String jmxDomain;
}
