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

import io.netty.util.concurrent.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.future.PromiseInternal;

class ReplayPduFutureImpl<T> extends AbstractPduFuture<T, ReplayPduFuture<T>> implements ReplayPduFuture<T> {

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
