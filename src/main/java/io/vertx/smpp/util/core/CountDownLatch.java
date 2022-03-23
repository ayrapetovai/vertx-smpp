package io.vertx.smpp.util.core;

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
    return await(10*365, TimeUnit.DAYS);
  }

  public Future<Void> await(long timeout, TimeUnit unit) {
    var awaitPromise = Promise.<Void>promise();
    if (this.count == 0) {
      awaitPromise.tryComplete();
    } else {
      var expiresAtNano = System.nanoTime() + unit.toNanos(timeout);
      var taskRef = new Reference<Handler<Void>>();
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
