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
import com.example.smpp.types.SendPduFailedException;
import io.vertx.core.Handler;
import io.vertx.core.impl.future.PromiseInternal;


import static com.example.smpp.model.SendPduExceptionType.*;

class BindFutureImpl<T> extends AbstractPduFuture<T, BindFuture<T>> implements BindFuture<T> {

  public BindFutureImpl(PromiseInternal<T> promise) {
    super(promise);
  }

  @Override
  public BindFuture<T> future() {
    return this;
  }

  @Override
  public BindFuture<T> onSuccess(Handler<T> handler) {
    delegateAsPromise.onSuccess(handler);
    return this;
  }

  @Override
  public BindFuture<T> onFailure(Handler<Throwable> handler) {
    delegateAsPromise.onFailure(handler);
    return this;
  }


  @Override
  public BindFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == CHANNEL_CLOSED) {
          handler.handle((SendPduChannelClosedException)e);
        }
      }
    });
    return this;
  }

  @Override
  public BindFuture<T> onRefuse(Handler<SendBindRefusedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == BIND_REFUSED) {
          handler.handle((SendBindRefusedException)e);
        }
      }
    });
    return this;
  }
}
