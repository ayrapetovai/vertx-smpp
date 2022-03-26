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

import io.vertx.smpp.types.SendPduChannelClosedException;
import io.vertx.smpp.types.SendPduWriteFailedException;
import io.vertx.smpp.types.SendPduWrongOperationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.FutureInternal;
import io.vertx.core.impl.future.Listener;
import io.vertx.core.impl.future.PromiseInternal;

import java.util.function.Function;

// package-private
abstract class AbstractPduFuture<T, F extends Future<T>> implements Future<T>, Promise<T>, FutureInternal<T> {

  protected final PromiseInternal<T> delegateAsPromise;
  protected final Future<T> delegateAsFuture;

  public AbstractPduFuture(PromiseInternal<T> delegateAsPromise) {
    this.delegateAsPromise = delegateAsPromise;
    this.delegateAsFuture = delegateAsPromise.future();
  }

  @Override
  public ContextInternal context() {
    return delegateAsPromise.context();
  }

  @Override
  public void addListener(Listener<T> listener) {
    delegateAsPromise.addListener(listener);
  }

  @Override
  public boolean tryComplete(T result) {
    return delegateAsPromise.tryComplete(result);
  }

  @Override
  public boolean isComplete() {
    return delegateAsFuture.isComplete();
  }

  @Override
  public F onComplete(Handler<AsyncResult<T>> handler) {
    return (F) delegateAsFuture.onComplete(handler);
  }

  @Override
  public T result() {
    return delegateAsFuture.result();
  }

  @Override
  public Throwable cause() {
    return delegateAsFuture.cause();
  }

  @Override
  public boolean succeeded() {
    return delegateAsFuture.succeeded();
  }

  @Override
  public boolean failed() {
    return delegateAsFuture.failed();
  }

  @Override
  public <U> Future<U> compose(Function<T, Future<U>> successMapper, Function<Throwable, Future<U>> failureMapper) {
    return delegateAsFuture.compose(successMapper, failureMapper);
  }

  @Override
  public <U> Future<U> transform(Function<AsyncResult<T>, Future<U>> mapper) {
    return delegateAsFuture.transform(mapper);
  }

  @Override
  public <U> F eventually(Function<Void, Future<U>> mapper) {
    return (F) delegateAsFuture.eventually(mapper);
  }

  @Override
  public <U> Future<U> map(Function<T, U> mapper) {
    return delegateAsFuture.map(mapper);
  }

  @Override
  public <V> Future<V> map(V value) {
    return delegateAsFuture.map(value);
  }

  @Override
  public Future<T> otherwise(Function<Throwable, T> mapper) {
    return delegateAsFuture.otherwise(mapper);
  }

  @Override
  public Future<T> otherwise(T value) {
    return delegateAsFuture.otherwise(value);
  }

  @Override
  public boolean tryFail(Throwable throwable) {
    return delegateAsPromise.tryFail(throwable);
  }

  @Override
  public boolean tryFail(String message) {
    return delegateAsPromise.tryFail(message);
  }

  @Override
  public F recover(Function<Throwable, Future<T>> mapper) {
    return (F) compose(Future::succeededFuture, mapper);
  }

  public F onChannelClosed(Handler<SendPduChannelClosedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduChannelClosedException) {
        handler.handle((SendPduChannelClosedException)e);
      }
    });
    return (F) this;
  }

  public F onWrongOperation(Handler<SendPduWrongOperationException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduWrongOperationException) {
        handler.handle((SendPduWrongOperationException)e);
      }
    });
    return (F) this;
  }

  public F onWriteFailed(Handler<SendPduWriteFailedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduWriteFailedException) {
        handler.handle((SendPduWriteFailedException)e);
      }
    });
    return (F) this;
  }
}
