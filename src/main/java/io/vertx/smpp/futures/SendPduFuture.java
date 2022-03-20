package io.vertx.smpp.futures;

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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseImpl;
import io.vertx.smpp.types.*;

public interface SendPduFuture<T> extends Future<T>, Promise<T> {

  static <T> SendPduFuture<T> promise(ContextInternal contextInternal) {
    return new SendPduFutureImpl<>(new PromiseImpl<>(contextInternal));
  }

  static <T, E extends SendPduFailedException> SendPduFuture<T> failedFuture(E e) {
    var promise = new PromiseImpl<T>();
    promise.fail(e);
    return new SendPduFutureImpl<>(promise);
  }

  // TODO Implement all methods which return Future<T>, make them return SendPduFuture, as well for other custom futures.

  @Override
  SendPduFuture<T> onComplete(Handler<AsyncResult<T>> handler);

  @Override
  SendPduFuture<T> onSuccess(Handler<T> handler);

  @Override
  SendPduFuture<T> onFailure(Handler<Throwable> handler);

  SendPduFuture<T> onWindowTimeout(Handler<SendPduWindowTimeoutException> handler);

  SendPduFuture<T> onTimeout(Handler<SendPduRequestTimeoutException> handler);

  SendPduFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);

  SendPduFuture<T> onWrongOperation(Handler<SendPduWrongOperationException> handler);

  SendPduFuture<T> onDiscarded(Handler<SendPduDiscardedException> handler);

  SendPduFuture<T> onWriteFailed(Handler<SendPduWriteFailedException> handler);

  SendPduFuture<T> onNackked(Handler<SendPduNackkedException> handler);
}
