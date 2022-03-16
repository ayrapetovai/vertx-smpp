package com.example.smpp.util.futures;

import io.netty.util.concurrent.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.future.PromiseInternal;

class ReplayPduFutureImpl<T> extends AbstractPduFuture<T> implements ReplayPduFuture<T> {

  public ReplayPduFutureImpl(PromiseInternal<T> promise) {
    super(promise);
  }

  @Override
  public ReplayPduFuture<T> future() {
    return this;
  }

  @Override
  public ReplayPduFuture<T> onSuccess(Handler<T> handler) {
    delegateAsPromise.onSuccess(handler);
    return this;
  }

  @Override
  public ReplayPduFuture<T> onFailure(Handler<Throwable> handler) {
    delegateAsPromise.onFailure(handler);
    return this;
  }

  @Override
  public void operationComplete(Future<T> future) throws Exception {
    if (future.isSuccess()) {
      complete(future.getNow());
    } else {
      fail(future.cause());
    }
  }
}
