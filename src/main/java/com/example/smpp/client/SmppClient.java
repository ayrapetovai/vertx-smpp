package com.example.smpp.client;

import com.example.smpp.session.ClientSessionConfigurator;
import com.example.smpp.session.SmppSession;
import com.example.smpp.futures.BindFuture;
import io.vertx.core.Handler;

public interface SmppClient {
  // API for built smpp client
  BindFuture<SmppSession> bind(String host, int port);

  SmppClient configure(Handler<ClientSessionConfigurator> configurator);
}
