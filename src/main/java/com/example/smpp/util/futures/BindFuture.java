package com.example.smpp.util.futures;

import com.example.smpp.util.SendBindRefusedException;
import com.example.smpp.util.SendPduChannelClosedException;
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
