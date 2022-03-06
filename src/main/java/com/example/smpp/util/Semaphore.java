package com.example.smpp.util;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;

public class Semaphore {
    private static class Msg {
        int value;
        Promise<Void> promise;

        public Msg(int value, Promise<Void> promise) {
            this.value = value;
            this.promise = promise;
        }
    }

    private static class MsgCodec implements MessageCodec {

        @Override
        public void encodeToWire(Buffer buffer, Object o) {

        }

        @Override
        public Object decodeFromWire(int pos, Buffer buffer) {
            return null;
        }

        @Override
        public Object transform(Object o) {
            return o;
        }

        @Override
        public String name() {
            return "semaphoreMsgCodec";
        }

        @Override
        public byte systemCodecID() {
            return -1;
        }
    }

    public static Semaphore create(Vertx vertx, int initialValue) {
        if (semaphoreCounter == 0) {
            vertx.eventBus().registerCodec(new MsgCodec());
        }
        return new Semaphore(vertx, initialValue);
    }

    private static int semaphoreCounter = 0;
    private static DeliveryOptions deliveryOptions = new DeliveryOptions().setLocalOnly(true).setCodecName("semaphoreMsgCodec");
    private final String semaphoreName = "semaphore" + semaphoreCounter++;
    private int valueCounter = 0;
    private final Vertx vertx;

    private Semaphore(Vertx vertx, int initialValue) {
        this.vertx = vertx;
        this.valueCounter = initialValue;
        vertx.eventBus()
                .<Msg>localConsumer(semaphoreName)
                .handler(message -> {
                    var msg = message.body();
                    if (this.valueCounter < msg.value) {
                        vertx.eventBus().send(semaphoreName, msg, deliveryOptions);
                    } else {
                        valueCounter -= msg.value;
                        vertx.runOnContext(nothing -> msg.promise.complete()); // does not work
//                        msg.promise.complete();
                    }
                });
    }

    public Future<Void> aquire(int value) {
      var promise = Promise.<Void>promise();
      if (valueCounter < value) {
          vertx.eventBus().send(semaphoreName, new Msg(value, promise), deliveryOptions);
      } else {
          valueCounter -= value;
          promise.complete();
      }
      return promise.future();
    }

    public void release(int value) {
        valueCounter += value;
    }
}
