package com.example.smpp.server;

import io.vertx.core.net.NetServerOptions;

public class SmppServerOptions extends NetServerOptions {
  // boolean dropAllOnUnbind = false; // per session? При получении unbind, не досылать resp и другие pdu, сразу убить сессию
  // boolean replyToUnbind = true; //per session? В ответ на unbind посылать unbind_resp
  // Duration bindTimeout; // per session? Время, за которое сервер выдаст bind_resp
  // Duration unbindTimeout; // per session? Время, которе дается клиенту на отправку unbind_resp, по истечении все pdu в окне дропаются и соединение закрывается

//    private String name; // for identification on local JVM?
//    private int windowSize; // per session?
//    private long bindTimeout; // per session?
//    private String systemId; // per session?

//    private int defaultWindowSize; // per session?
//    private long defaultWindowWaitTimeout; // per session? (aquire(..., TimeUnit.))
//    private long defaultRequestExpiryTimeout; // per session?
//    private long defaultWindowMonitorInterval; // per session?
//    private boolean defaultSessionCountersEnabled = false; // per session?

//    private boolean autoNegotiateInterfaceVersion;
//    private byte interfaceVersion;// smpp version the server supports
//    private int maxConnectionSize;
//    private boolean reuseAddress;

//    private boolean jmxEnabled;
//    private String jmxDomain;
}
