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

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class SimpleTest {

  @Test
  public void checkStartupAndBind(Vertx vertx, VertxTestContext context) {
      var clientBoundSync = context.checkpoint();
      var server = Smpp.server(vertx);
      server.start()
          .compose(smppServer ->
            Smpp.client(vertx)
                .bind(server.actualPort())
          )
          .onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
              var sess = asyncResult.result();
              if (sess.isBound()) {
                clientBoundSync.flag();
              } else {
                context.failNow("session is not bound");
              }
            } else {
              context.failNow("cannot create client");
            }
        });
  }
}
