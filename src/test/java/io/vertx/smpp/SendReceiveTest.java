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
import io.vertx.smpp.model.SmppBindType;
import io.vertx.smpp.pdu.DeliverSm;
import io.vertx.smpp.pdu.SubmitSm;
import io.vertx.smpp.session.SmppSession;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class SendReceiveTest {

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  /**
   * Check if client will receive deliver_sm at RECEIVER session, with precondition that server
   * sends deliver_sm to RECEIVER session when it got a submit_sm on TRANSMITTER session.
   */
  @Test
  public void clientGotDeliverSmAtReceiverSession(TestContext context) {
    var clientReceiverSession = new AtomicReference<SmppSession>();
    var clientBoundSync = context.async();
    var server = Smpp.server(rule.vertx())
            .configure(cfg -> {
              cfg.onCreated(sess -> {
                // TODO it is probably not the best way to have API that makes user-server-code to check bindType this way
                if (sess.getOptions().getBindType() == SmppBindType.RECEIVER) {
                  clientReceiverSession.set(sess);
                }
              });
              cfg.onRequest(reqCtx -> {
                if (reqCtx.getRequest() instanceof SubmitSm) {
                  reqCtx.getSession()
                      .reply(reqCtx.getRequest().createResponse())
                      .onFailure(context::fail);
                  clientReceiverSession.get()
                      .send(new DeliverSm())
                      .onFailure(context::fail);
                }
              });
            });
    server.start()
        .compose(smppServer -> Smpp.client(rule.vertx())
            .configure(cfg -> {
              cfg.setBindType(SmppBindType.RECEIVER);
              cfg.onRequest(reqCtx -> {
                if (reqCtx.getRequest() instanceof DeliverSm) {
                  reqCtx.getSession()
                      .reply(reqCtx.getRequest().createResponse())
                      .onSuccess(v -> clientBoundSync.complete())
                      .onFailure(context::fail);
                }
              });
            })
            .bind(server.actualPort()))
        .compose(sessReceiver -> {
          if (!sessReceiver.isBound()) {
            context.fail("session transmitter is not bound");
          }
          return Smpp.client(rule.vertx())
              .configure(cfg -> cfg.setBindType(SmppBindType.TRANSMITTER))
              .bind(server.actualPort());
        })
        .onComplete(asyncResult -> {
          var sessTransmitter = asyncResult.result();
          if (!sessTransmitter.isBound()) {
            context.fail("session transmitter is not bound");
          } else {
            sessTransmitter.send(new SubmitSm())
                .onFailure(context::fail);
          }
        })
        .onFailure(e -> {
          context.fail("cannot create client");
        });
  }
}
