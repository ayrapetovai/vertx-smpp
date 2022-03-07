package com.example.smpp.server;

import com.cloudhopper.smpp.pdu.BaseBind;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import io.vertx.core.Future;
import io.vertx.core.Handler;

public interface SmppServer {
  // smpp builder's static API

  /**
   * Срабатывает на запрос соединения, когда в канал пришел BindTransmitter или BindReceiver или BindTranceiver
   * UnboundSmppSession - сессия в которую нельзя писать и читать, но можно задать хэндлеры
   * @param bindRequestedHandler - принимает сессию, чтобы можно было ее сконфигурировать
   * @return - UnbindRespStatusCode - com.cloudhopper.smpp.SmppConstants.STATUS_*
   */
//  SmppServer onBindRequested(Function<UnboundSmppSession, UnbindRespStatusCode> configurator);
//  ----> SmppSession
//  SmppServer onSessionDestroyed(Handler<SmppSession> sessionCreatedHandler);
  SmppServer onSessionCreated(Handler<SmppSession> sessionCreatedHandler); // удалить
  // ---> SmppSession
  SmppServer onRequest(Handler<PduRequestContext<?>> pduRequestHandler);

  // API for built smpp server
  Future<SmppServer> start(String host, int port);
//  Future<Void> shutdown();
}
