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
