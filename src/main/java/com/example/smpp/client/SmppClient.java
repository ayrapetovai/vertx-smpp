package com.example.smpp.client;

import com.example.smpp.SmppSession;
import com.example.smpp.session.ClientSessionConfigurator;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface SmppClient {
  // API for built smpp client
  Future<SmppSession> bind(String host, int port);

  SmppClient configure(Handler<ClientSessionConfigurator> configurator);
}
