package com.example.smpp.util.core;

import io.vertx.core.*;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class FlowControl {

  public static Future<Void> forLoopInt(Vertx vertx, int initial, int upperBound, Handler<Integer> handler) {
    var completed = Promise.<Void>promise();
    var counter = new IntegerBox(initial);
    var ctx = vertx.getOrCreateContext();
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

  public static Future<Void> awaitCondition(Vertx vertx, BooleanSupplier condition, long timeout) {
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
          vertx.runOnContext(jobRef.get());
        }
      };
      jobRef.set(job);
      vertx.runOnContext(job);
    }
    return awaited.future();
  }
}
