package com.example.smpp.futures;

import com.example.smpp.types.SendPduChannelClosedException;
import com.example.smpp.types.SendPduFailedException;
import com.example.smpp.types.SendPduWriteFailedException;
import com.example.smpp.types.SendPduWrongOperationException;
import io.netty.util.concurrent.FutureListener;
import io.vertx.core.Future;
import io.vertx.core.Handler;
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

  ReplayPduFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);

  ReplayPduFuture<T> onWrongOperation(Handler<SendPduWrongOperationException> handler);

  ReplayPduFuture<T> onWriteFailed(Handler<SendPduWriteFailedException> handler);
}
