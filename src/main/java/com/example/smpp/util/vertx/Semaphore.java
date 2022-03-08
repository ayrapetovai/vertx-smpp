package com.example.smpp.util.vertx;

import io.vertx.core.*;

// TODO
//  - blocking behavior for release and tryRelease
//  - check in aquire and release values are not 0
//  - debug tryAquire, was not tested
//  - release no more for valueCounter to became greater then initialValue
public class Semaphore {

  public static Semaphore create(Vertx vertx, int initialValue) {
    return new Semaphore(vertx, initialValue);
  }

  private final int initialValue;
  private final Vertx vertx;
  private int valueCounter = 0;

  private Semaphore(Vertx vertx, int initialValue) {
    this.vertx = vertx;
    this.initialValue = initialValue;
    this.valueCounter = initialValue;
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
      var taskRef = new Handler[]{null};
      var task = (Handler<Void>) v -> {
        if (valueCounter < value) {
          vertx.runOnContext(taskRef[0]);
        } else {
          valueCounter -= value;
          aquirePromise.complete();
        }
      };
      taskRef[0] = task;
      vertx.runOnContext(task);
    } else {
      valueCounter -= value;
      aquirePromise.complete();
    }
    return aquirePromise.future();
  }

  public void release(int value) {
    if (valueCounter + value <= initialValue) {
      valueCounter += value;
    }
  }
}
