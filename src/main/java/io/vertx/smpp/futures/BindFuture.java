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
import io.vertx.smpp.types.SendBindRefusedException;
import io.vertx.smpp.types.SendPduChannelClosedException;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseImpl;

import java.net.ConnectException;
import java.util.function.Function;

// TODO add all methods returning BindFuture instead of Future
//  fromCompletionStage
public interface BindFuture<T> extends Future<T>, Promise<T> {

  static <T> BindFuture<T> promise(ContextInternal contextInternal) {
    return new BindFutureImpl<>(new PromiseImpl<>(contextInternal));
  }

  @Override
  BindFuture<T> future();
  @Override
  BindFuture<T> onFailure(Handler<Throwable> handler);
  @Override
  BindFuture<T> onComplete(Handler<AsyncResult<T>> handler);
  @Override
  BindFuture<T> onSuccess(Handler<T> handler);
  @Override
  BindFuture<T> recover(Function<Throwable, Future<T>> mapper);
  BindFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler);
  BindFuture<T> onBindRefused(Handler<SendBindRefusedException> handler);
  BindFuture<T> onConnectionRefused(Handler<ConnectException> handler);
}
