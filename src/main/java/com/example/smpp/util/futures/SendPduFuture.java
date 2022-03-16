package com.example.smpp.util.futures;

import com.example.smpp.util.SendPduChannelClosedException;
import com.example.smpp.util.SendPduFailedException;
import com.example.smpp.util.SendPduWindowTimeoutException;
import com.example.smpp.util.SendPduWrongOperationException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseImpl;

public interface SendPduFuture<T> extends Future<T>, Promise<T> {

  static <T> SendPduFuture<T> promise(ContextInternal contextInternal) {
    return new SendPduFutureImpl<>(new PromiseImpl<>(contextInternal));
  }

  static <T, E extends SendPduFailedException> SendPduFuture<T> failedFuture(E e) {
    var promise = new PromiseImpl<T>();
    promise.fail(e);
    return new SendPduFutureImpl<>(promise);
  }

  @Override
  SendPduFuture<T> onSuccess(Handler<T> handler);

  @Override
  SendPduFuture<T> onFailure(Handler<Throwable> handler);

  SendPduFuture<T> onWindowTimeout(Handler<SendPduWindowTimeoutException> handler);

  SendPduFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);

  SendPduFuture<T> onWrongOperation(Handler<SendPduWrongOperationException> handler);
}
