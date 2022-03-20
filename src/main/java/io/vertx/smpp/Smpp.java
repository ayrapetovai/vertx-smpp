package io.vertx.smpp;

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

import io.vertx.smpp.client.SmppClient;
import io.vertx.smpp.client.SmppClientImpl;
import io.vertx.smpp.client.SmppClientOptions;
import io.vertx.smpp.server.SmppServer;
import io.vertx.smpp.server.SmppServerImpl;
import io.vertx.smpp.server.SmppServerOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;

public interface Smpp {

  static SmppServer server(Vertx vertx) {
    return new SmppServerImpl((VertxInternal) vertx, new SmppServerOptions());
  }

  static SmppServer server(Vertx vertx, SmppServerOptions options) {
    return new SmppServerImpl((VertxInternal) vertx, options);
  }

  static SmppClient client(Vertx vertx) {
    return new SmppClientImpl((VertxInternal) vertx, new SmppClientOptions(), new CloseFuture());
  }

  static SmppClient client(Vertx vertx, SmppClientOptions options) {
    return new SmppClientImpl((VertxInternal) vertx, options, new CloseFuture());
  }

}
