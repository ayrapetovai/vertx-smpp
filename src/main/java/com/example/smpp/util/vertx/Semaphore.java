package com.example.smpp.util.vertx;

import io.vertx.core.*;

import java.util.concurrent.TimeUnit;

// TODO
//  - blocking behavior for release and tryRelease
//  - check in acquire and release values are not 0
//  - debug tryAcquire, was not tested
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
   * @param value - amount of resource to acquire.
   * @return true if value was acquired, false if it has not changed.
   */
  public Future<Boolean> tryAcquire(int value) {
    var acquirePromise = Promise.<Boolean>promise();
    if (valueCounter >= value) {
      valueCounter -= value;
      acquirePromise.complete(true);
    } else {
      acquirePromise.complete(false);
    }
    return acquirePromise.future();
  }

  public Future<Void> acquire(int value) {
    var acquirePromise = Promise.<Void>promise();
    if (valueCounter < value) {
      var taskRef = new Handler[]{null};
      var task = (Handler<Void>) v -> {
        if (valueCounter < value) {
          vertx.runOnContext(taskRef[0]);
        } else {
          valueCounter -= value;
          acquirePromise.complete();
        }
      };
      taskRef[0] = task;
      vertx.runOnContext(task);
    } else {
      valueCounter -= value;
      acquirePromise.complete();
    }
    return acquirePromise.future();
  }

  public Future<Void> acquire(int value, long timeout) {
    var acquirePromise = Promise.<Void>promise();
    var expiresAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
    if (valueCounter < value) {
      var taskRef = new Handler[]{null};
      var task = (Handler<Void>) v -> {
        if (System.nanoTime() < expiresAt || timeout <= 0) {
          if (valueCounter < value) {
            vertx.runOnContext(taskRef[0]);
          } else {
            valueCounter -= value;
            acquirePromise.complete();
          }
        } else {
          acquirePromise.fail("acquire expired by timeout " + timeout);
        }
      };
      taskRef[0] = task;
      vertx.runOnContext(task);
    } else {
      valueCounter -= value;
      acquirePromise.complete();
    }
    return acquirePromise.future();
  }

  public void release(int value) {
    if (valueCounter + value <= initialValue) {
      valueCounter += value;
    }
  }
}
