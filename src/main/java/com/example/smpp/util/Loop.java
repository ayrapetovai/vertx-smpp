package com.example.smpp.util;

import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class Loop {
  private static final Logger log = LoggerFactory.getLogger(Loop.class);
  private final Set<Long> timerIds = new HashSet<>();
  private final Vertx vertx;

  private boolean running = true;

  public Loop(Vertx vertx) {
    this.vertx = vertx;
  }

  public Future<Void> forLoopInt(int initial, int upperBound, Function<Integer, Future<Boolean>> handler) {
    var completed = Promise.<Void>promise();
    var counter = new AtomicInteger(initial);

    var maxTimers = Math.min(170, upperBound); // there was no more than 134749 rps, to config
    var timerCounter = new int[] {maxTimers};
    var delay = 1;

    var taskRef = new Handler[]{null};
    var task = (Handler<Long>) id -> {
      int newCounterValue = counter.getAndIncrement();
//      log.warn("Timer newCounterValue=" + newCounterValue);
      if (newCounterValue < (upperBound - initial)) {
        handler.apply(newCounterValue)
          .onSuccess(throttled -> {
            if (throttled && timerCounter[0] > 1) {
              vertx.cancelTimer(id);
              timerCounter[0]--;
//              log.warn("Timer canceled, timers=" + timerCounter[0]);
            } else if (timerCounter[0] < maxTimers){
              vertx.setPeriodic(delay, taskRef[0]);
              timerCounter[0]++;
//              log.warn("Timer created, timers=" + timerCounter[0]);
            }
          });
//      } else if (!completed.future().isComplete()){
      } else {
        vertx.cancelTimer(id);
        timerCounter[0]--;
        completed.tryComplete();
      }
    };
    taskRef[0] = task;
    for (int i = 0; i < maxTimers; i++) {
      vertx.setPeriodic(delay, task);
    }
    return completed.future();
  }

  public void forever(Function<Long, Future<Boolean>> handler) {
    var counter = new AtomicLong(0);
    var maxTimers = 170;
    var timerCounter = new int[] {maxTimers};
    var delay = 1;

    var taskRef = new Handler[]{null};
    var task = (Handler<Long>) id -> {
      handler.apply(counter.getAndIncrement())
        .onSuccess(throttled -> {
          if (throttled && timerCounter[0] > 1) {
            vertx.cancelTimer(id);
            timerIds.remove(id);
            timerCounter[0]--;
//            log.warn("Timer canceled, timers=" + timerCounter[0]);
          } else if (timerCounter[0] < maxTimers){
            timerIds.add(vertx.setPeriodic(delay, taskRef[0]));
            timerCounter[0]++;
//            log.warn("Timer created, timers=" + timerCounter[0]);
          }
        });
    };
    taskRef[0] = task;
    for (int i = 0; i < maxTimers; i++) {
      timerIds.add(vertx.setPeriodic(delay, task));
    }
  }

//  public Future<Void> forLoopInt(int initial, int upperBound, Function<Integer, Future<Boolean>> handler) {
//    var completed = Promise.<Void>promise();
//    var counter = new AtomicInteger(initial);
//
//    var ctx = vertx.getOrCreateContext();
//
//    var jobRef = new Handler<?>[]{null};
//    var job = (Handler<Void>) doJob -> {
//      var cnt = counter.getAndIncrement();
//      if (running && cnt < upperBound) {
//        handler.apply(cnt);
//        ctx.runOnContext((Handler<Void>) jobRef[0]);
//      } else {
//        completed.complete();
//      }
//    };
//    jobRef[0] = job;
//    if (initial >= upperBound) {
//      completed.complete();
//    } else {
//      ctx.runOnContext(job);
//    }
//    return completed.future();
//  }
//
//  public void forever(Function<Long, Future<Boolean>> handler) {
//    var counter = new AtomicLong(0);
//    var ctx = vertx.getOrCreateContext();
//    ctx.runOnContext(doJob -> {
//      if (running) {
//        handler
//          .apply(counter.getAndIncrement())
//          .compose(throttled -> {
//            if (running) {
//              forever(handler);
//            }
//            return Future.succeededFuture();
//          });
//      }
//    });
//  }

  public void stop() {
    running = false;
    timerIds.forEach(vertx::cancelTimer);
  }
}
