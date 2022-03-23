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
import java.util.function.BooleanSupplier;

public class FlowControl {

  public static Future<Void> forLoopInt(Context ctx, int initial, int upperBound, Handler<Integer> handler) {
    var completed = Promise.<Void>promise();
    var counter = new IntegerBox(initial);
    var jobRef = new Reference<Handler<Void>>();
    var job = (Handler<Void>) doJob -> {
      var cnt = counter.getAndIncrement();
      if (cnt < upperBound) {
        handler.handle(cnt);
        ctx.runOnContext(jobRef.get());
      } else {
        completed.complete();
      }
    };
    jobRef.set(job);
    if (initial >= upperBound) {
      completed.complete();
    } else {
      ctx.runOnContext(job);
    }
    return completed.future();
  }

  public static Future<Void> awaitCondition(Context ctx, BooleanSupplier condition, long timeout) {
    var awaited = Promise.<Void>promise();
    var expiresAt = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout);
    if (condition.getAsBoolean()) {
      awaited.complete();
    } else {
      var jobRef = new Reference<Handler<Void>>();
      var job = (Handler<Void>) doJob -> {
        var expired = System.nanoTime() >= expiresAt && timeout > 0;
        if (condition.getAsBoolean() && !expired) {
          awaited.complete();
        } else if (expired) {
          awaited.fail("expired");
        } else {
          ctx.runOnContext(jobRef.get());
        }
      };
      jobRef.set(job);
      ctx.runOnContext(job);
    }
    return awaited.future();
  }

  public static Future<Void> whileCondition(Context ctx, BooleanSupplier condition, Runnable handler) {
    var completed = Promise.<Void>promise();
    var jobRef = new Reference<Handler<Void>>();
    var job = (Handler<Void>) doJob -> {
      if (condition.getAsBoolean()) {
        handler.run();
        ctx.runOnContext(jobRef.get());
      } else {
        completed.complete();
      }
    };
    jobRef.set(job);
    if (!condition.getAsBoolean()) {
      completed.complete();
    } else {
      ctx.runOnContext(job);
    }
    return completed.future();
  }
}
