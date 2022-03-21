package io.vertx.smpp.demo;

import com.cloudhopper.commons.charset.CharsetUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.smpp.Smpp;
import io.vertx.smpp.pdu.SubmitSm;
import io.vertx.smpp.types.Address;
import io.vertx.smpp.types.SmppInvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send message to http server
 * $ curl -d '{"text":"Hello!", "sender": "41213", "receiver": "12412312"}' -X POST http://localhost:8080/message
 */
public class SmppGatewayMain extends AbstractVerticle {
  public static final Logger log = LoggerFactory.getLogger(SmppGatewayMain.class);

  public static class SmppClientVerticle extends AbstractVerticle {
    public void start(Promise<Void> startPromise) {
      log.info("starting smpp-client");
      Smpp.client(vertx)
          .configure(cfg -> {
            log.info("configuring smpp-client");
            cfg.setSystemId("vertx-smpp-gateway");
            cfg.onRequest(reqCtx -> reqCtx.getSession().reply(reqCtx.getRequest().createResponse()));
          })
          .bind(2776)
          .onSuccess(sess -> {
            log.info("{} bound", sess);
            vertx.eventBus()
                .localConsumer("messages", (Message<JsonObject> envelop) -> {
                  sess.send(createSms(envelop.body()));
                });
                startPromise.complete();
          })
          .onFailure(startPromise::fail);
    }

    private SubmitSm createSms(JsonObject message) {
      var text = message.getString("text");
      var sender = message.getString("sender");
      var receiver = message.getString("receiver");

      var submitSm = new SubmitSm();
      submitSm.setSourceAddress(new Address((byte) 1, (byte) 1, sender));
      submitSm.setDestAddress(new Address((byte) 1, (byte) 1, receiver));
      try {
        submitSm.setShortMessage(CharsetUtil.encode(text, CharsetUtil.CHARSET_GSM));
      } catch (SmppInvalidArgumentException e) {
        e.printStackTrace();
      }
      return submitSm;
    }
  }

  public static class HttpServerVerticl extends AbstractVerticle {
    public void start(Promise<Void> startPromise) {
      vertx.createHttpServer()
          .requestHandler(request -> {
            if ("/message".equals(request.path())) {
              request.body()
                  .map(JsonArray::new)
                  .onSuccess(messages -> {
                    for (var message: messages) {
                      vertx.eventBus().publish("messages", message);
                    }
                    request.response()
                        .setStatusCode(200)
                        .end();
                  })
                  .onFailure(e -> {
                    log.error("error", e);
                    request.response()
                        .setStatusCode(500)
                        .end(e.getMessage());
                  });
            } else {
              request.response()
                  .setStatusCode(400)
                  .end("no such path");
            }
          })
          .listen(8080)
          .onSuccess(server -> startPromise.complete())
          .onFailure(startPromise::fail);
    }
  }

  public void start(Promise<Void> startPromise) {
    log.info("deploying smpp-client");
    vertx.deployVerticle(new SmppClientVerticle())
        .compose(v -> {
          log.info("smpp-client deployed");
          log.info("deploying http-server");
          return vertx.deployVerticle(new HttpServerVerticl());
        })
        .onSuccess(__ -> {
          log.info("http-server deployed");
          startPromise.complete();
        })
        .onFailure(startPromise::fail);
  }

  // jmeter, 1 thread, 1 connection| Throughput | Avg | Min | Max | Err |
  // summary = 2693857 in 00:03:52 |  11593.5/s |  0  |  0  | 243 |  0  |
  public static void main(String[] args) {
    var vertx = Vertx.vertx();
    vertx.deployVerticle(SmppGatewayMain.class.getCanonicalName());
  }
}
