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
  private int valueCounter;

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
      var taskRef = new Reference<Handler<Void>>();
      var task = (Handler<Void>) v -> {
        if (valueCounter < value) {
          vertx.runOnContext(taskRef.get());
        } else {
          valueCounter -= value;
          acquirePromise.complete();
        }
      };
      taskRef.set(task);
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
      var taskRef = new Reference<Handler<Void>>();
      var task = (Handler<Void>) v -> {
        if (System.nanoTime() < expiresAt || timeout <= 0) {
          if (valueCounter < value) {
            vertx.runOnContext(taskRef.get());
          } else {
            valueCounter -= value;
            acquirePromise.complete();
          }
        } else {
          acquirePromise.fail("acquire expired by timeout " + timeout);
        }
      };
      taskRef.set(task);
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

  public int getCounter() {
    return this.valueCounter;
  }
}
