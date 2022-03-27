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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SimpleTest {

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Test
  public void checkStartupAndBind(TestContext context) {
      var clientBoundSync = context.async();
      var server = Smpp.server(rule.vertx());
      server.start()
          .compose(smppServer ->
            Smpp.client(rule.vertx())
                .bind(server.actualPort())
          )
          .onComplete(asyncResult -> {
            if (asyncResult.succeeded()) {
              var sess = asyncResult.result();
              if (sess.isBound()) {
                clientBoundSync.complete();
              } else {
                context.fail("session is not bound");
              }
            } else {
              context.fail("cannot create client");
            }
        });
  }
}
