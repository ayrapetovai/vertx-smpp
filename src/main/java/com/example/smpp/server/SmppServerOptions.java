package com.example.smpp.server;

import io.vertx.core.net.NetServerOptions;

public class SmppServerOptions extends NetServerOptions {
  // boolean dropAllOnUnbind = false; // При получении unbind, не досылать resp и другие pdu, сразу вырабать сессию
  // boolean replyToUnbind = true; // В ответ на unbind посылать unbind_resp
}
