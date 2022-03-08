package com.example.smpp.server;

import com.example.smpp.session.ServerSessionConfigurator;
import io.vertx.core.Closeable;
import io.vertx.core.Future;

import java.util.function.Function;

public interface SmppServer extends Closeable {
  // smpp builder's static API

  /**
   * Срабатывает на запрос соединения, когда в канал пришел BindTransmitter или BindReceiver или BindTranceiver
   * UnboundSmppSession - сессия в которую нельзя писать и читать, но можно задать хэндлеры
   * @return - UnbindRespStatusCode - com.cloudhopper.smpp.SmppConstants.STATUS_*
   */
//  SmppServer onBindRequested(Function<UnboundSmppSession, UnbindRespStatusCode> configurator);

  // API for built smpp server
  Future<SmppServer> start(String host, int port);
  boolean isListening();

  /**
   *
   * @param configurator
   * @return true if client system is allowed to bind
   */
  SmppServer configure(Function<ServerSessionConfigurator, Boolean> configurator);
//  ? getPool(); // get all connected (bound) client systems with their session pools
}
