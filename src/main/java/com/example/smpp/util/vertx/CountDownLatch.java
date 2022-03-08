package com.example.smpp.util.vertx;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.concurrent.TimeUnit;

public class CountDownLatch {
  private final Vertx vertx;
  private int count;

  public CountDownLatch(Vertx vertx, int count) {
    this.vertx = vertx;
    this.count = count;
  }

  public Future<Void> await() {
    return await(Long.MAX_VALUE, TimeUnit.DAYS);
  }

  public Future<Void> await(long timeout, TimeUnit unit) {
    var awaitPromise = Promise.<Void>promise();
    if (this.count == 0) {
      awaitPromise.tryComplete();
    } else {
      var expiresAtNano = System.nanoTime() + unit.toNanos(timeout);
      var taskRef = new Handler[]{null};
      var task = (Handler<Void>)v -> {
        if (this.count == 0) {
          awaitPromise.tryComplete();
        } else {
          if (System.nanoTime() < expiresAtNano) {
            vertx.runOnContext(taskRef[0]);
          } else {
            awaitPromise.fail("CountDownLatch:expired");
          }
        }
      };
      taskRef[0] = task;
      vertx.runOnContext(task);
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
