package com.example.smpp;

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
