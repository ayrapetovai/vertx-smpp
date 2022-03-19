package com.example.smpp.util.futures;

import com.example.smpp.util.*;
import io.vertx.core.AsyncResult;
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

  // TODO здесь надо перечислить все методы, которые возвращают Future<T> и заменить на этот интерфейс

  @Override
  SendPduFuture<T> onComplete(Handler<AsyncResult<T>> handler);

  @Override
  SendPduFuture<T> onSuccess(Handler<T> handler);

  @Override
  SendPduFuture<T> onFailure(Handler<Throwable> handler);

  SendPduFuture<T> onWindowTimeout(Handler<SendPduWindowTimeoutException> handler);

  SendPduFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);

  SendPduFuture<T> onWrongOperation(Handler<SendPduWrongOperationException> handler);

  SendPduFuture<T> onDiscarded(Handler<SendPduDiscardedException> handler);

  SendPduFuture<T> onWriteFailed(Handler<SendPduWriteFailedException> handler);
}
