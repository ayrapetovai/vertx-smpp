package com.example.smpp.futures;

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
