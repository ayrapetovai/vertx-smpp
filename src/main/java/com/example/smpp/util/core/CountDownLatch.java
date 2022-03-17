package com.example.smpp.util.core;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.lang.reflect.Array;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class CountDownLatch {
  private final Vertx vertx;
  private int count;

  public CountDownLatch(Vertx vertx, int count) {
    this.vertx = vertx;
    this.count = count;
  }

  public Future<Void> await() {
    return await(10*365, TimeUnit.DAYS);
  }

  public Future<Void> await(long timeout, TimeUnit unit) {
    var awaitPromise = Promise.<Void>promise();
    if (this.count == 0) {
      awaitPromise.tryComplete();
    } else {
      var expiresAtNano = System.nanoTime() + unit.toNanos(timeout);
      var taskRef = new AtomicReference<Handler<Void>>();
      var task = (Handler<Void>) v -> {
        if (this.count == 0) {
          awaitPromise.tryComplete();
        } else {
          if (System.nanoTime() < expiresAtNano) {
            vertx.runOnContext(taskRef.get());
          } else {
            awaitPromise.fail(new IllegalStateException("expired"));
          }
        }
      };
      taskRef.set(task);
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
