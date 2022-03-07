package com.example.smpp.util.vertx;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

public class CountDownLatch {
  private static final DeliveryOptions deliveryOptions = new DeliveryOptions().setLocalOnly(true).setCodecName(NoConversionLocalCodec.CODEC_NAME);
  private static int instanceNumber = 0;
  private final EventBus eventBus;
  private int count;
  private String queueName = "CountDownLatch#" + instanceNumber++;

  private static class Task {
    public final long expiresAtNano;
    public final Promise<Void> promise;

    private Task(long expiresAtNano, Promise<Void> promise) {
      this.expiresAtNano = expiresAtNano;
      this.promise = promise;
    }
  }

  public CountDownLatch(Vertx vertx, int count) {
    this.eventBus = vertx.eventBus();
    this.count = count;

    if (instanceNumber == 0) {
      try {
        vertx.eventBus().registerCodec(new NoConversionLocalCodec());
      } catch (Exception ignore) {

      }
    }

    this.eventBus
        .<Task>localConsumer(queueName, msg -> {
          var task = msg.body();
          if (this.count == 0) {
            task.promise.tryComplete();
          } else {
            if (System.nanoTime() < task.expiresAtNano) {
              vertx.eventBus()
                  .send(queueName, task, deliveryOptions);
            } else {
              task.promise.fail("CountDownLatch:expired");
            }
          }
        });
  }

  public Future<Void> await() {
    return await(Long.MAX_VALUE, TimeUnit.DAYS);
  }

  public Future<Void> await(long timeout, TimeUnit unit) {
    var awaitPromise = Promise.<Void>promise();
    if (this.count == 0) {
//      vertx.runOnContext(awaitPromise::tryComplete); // TODO test, if runOnContext is better?
      awaitPromise.tryComplete();
    } else {
      this.eventBus
          .send(queueName, new Task(System.nanoTime() +  unit.toNanos(timeout), awaitPromise), deliveryOptions);
    }
    return awaitPromise.future();
  }

  public void countDown(int countee) {
    this.count -= countee;
    if (this.count < 0) {
      this.count = 0;
    }
  }
}
