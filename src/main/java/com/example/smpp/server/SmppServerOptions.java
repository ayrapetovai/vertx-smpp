package com.example.smpp.server;

import io.vertx.core.net.NetServerOptions;

public class SmppServerOptions extends NetServerOptions {
  // boolean dropAllOnUnbind = false; // При получении unbind, не досылать resp и другие pdu, сразу убить сессию
  // boolean replyToUnbind = true; // В ответ на unbind посылать unbind_resp
  // Duration bindTimeout; // Время, за которое сервер выдаст результат бинда
  // Duration unbindTimeout; // Время, которе дается клиенту на отправку unbind_resp
}
