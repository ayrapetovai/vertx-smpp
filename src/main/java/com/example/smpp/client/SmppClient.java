package com.example.smpp.client;

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

import com.example.smpp.session.ClientSessionConfigurator;
import com.example.smpp.session.SmppSession;
import com.example.smpp.futures.BindFuture;
import io.vertx.core.Handler;

public interface SmppClient {
  BindFuture<SmppSession> bind(int port);

  BindFuture<SmppSession> bind(String host, int port);

  SmppClient configure(Handler<ClientSessionConfigurator> configurator);
}
