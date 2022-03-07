package com.example.smpp.util.vertx;

import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

// TODO
//  - blocking behavior for release and tryRelease
//  - check in aquire and release values are not 0
//  - debug tryAquire, was not tested
//  - release no more for valueCounter to became greater then initialValue
public class Semaphore {
  private static class Msg {
    int value;
    Promise<Void> promise;

    public Msg(int value, Promise<Void> promise) {
        this.value = value;
        this.promise = promise;
    }
  }

  public static Semaphore create(Vertx vertx, int initialValue) {
    if (instanceNumber == 0) {
      try {
        vertx.eventBus().registerCodec(new NoConversionLocalCodec());
      } catch (Exception ignore) {

      }
    }
    return new Semaphore(vertx, initialValue);
  }

  private static final DeliveryOptions deliveryOptions = new DeliveryOptions().setLocalOnly(true).setCodecName(NoConversionLocalCodec.CODEC_NAME);
  private static int instanceNumber = 0;
  private final String semaphoreName = "Semaphore#" + instanceNumber++;
  private final int initialValue;
  private final EventBus eventBus;
  private int valueCounter = 0;

  private Semaphore(Vertx vertx, int initialValue) {
    this.eventBus = vertx.eventBus();
    this.initialValue = initialValue;
    this.valueCounter = initialValue;

    this.eventBus
        .<Msg>localConsumer(semaphoreName)
        .handler(message -> {
          var msg = message.body();
          if (this.valueCounter < msg.value) {
            this.eventBus.send(semaphoreName, msg, deliveryOptions);
          } else {
            valueCounter -= msg.value;
            vertx.runOnContext(nothing -> msg.promise.complete());
          }
        });
  }

  /**
   * "Nonblocking"
   * @param value - amount of resource to aquire.
   * @return true if value was aquired, false if it has not changed.
   */
  public Future<Boolean> tryAquire(int value) {
    var aquirePromise = Promise.<Boolean>promise();
    if (valueCounter >= value) {
      valueCounter -= value;
      aquirePromise.complete(true);
    } else {
      aquirePromise.complete(false);
    }
    return aquirePromise.future();
  }

  public Future<Void> aquire(int value) {
    var aquirePromise = Promise.<Void>promise();
    if (valueCounter < value) {
      this.eventBus.send(semaphoreName, new Msg(value, aquirePromise), deliveryOptions);
    } else {
      valueCounter -= value;
      aquirePromise.complete();
    }
    return aquirePromise.future();
  }

  public void release(int value) {
    valueCounter += value;
  }
}
