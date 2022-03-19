package com.example.smpp.futures;

import com.example.smpp.types.SendBindRefusedException;
import com.example.smpp.types.SendPduChannelClosedException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseImpl;

public interface BindFuture<T> extends Future<T>, Promise<T> {

  static <T> BindFuture<T> promise(ContextInternal contextInternal) {
    return new BindFutureImpl<>(new PromiseImpl<>(contextInternal));
  }

  @Override
  BindFuture<T> future();
  BindFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);
  BindFuture<T> onRefuse(Handler<SendBindRefusedException> handler);
}
