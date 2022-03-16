package com.example.smpp.util.futures;

import com.example.smpp.util.SendPduFailedException;
import io.netty.util.concurrent.FutureListener;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseImpl;

public interface ReplayPduFuture<T> extends Future<T>, Promise<T>, FutureListener<T> {

  static <T> ReplayPduFuture<T> promise(ContextInternal contextInternal) {
    return new ReplayPduFutureImpl<>(new PromiseImpl<>(contextInternal));
  }

  static <T, E extends SendPduFailedException> ReplayPduFuture<T> failedFuture(E e) {
    var promise = new PromiseImpl<T>();
    promise.fail(e);
    return new ReplayPduFutureImpl<>(promise);
  }

  @Override
  ReplayPduFuture<T> future();
}
