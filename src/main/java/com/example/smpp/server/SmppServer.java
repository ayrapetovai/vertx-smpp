package com.example.smpp.server;

//   Copyright 2022 Artem Ayrapetov
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

import com.example.smpp.session.ServerSessionConfigurator;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Handler;

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
  SmppServer configure(Handler<ServerSessionConfigurator> configurator);
//  ? getPool(); // get all connected (bound) client systems with their session pools
}
