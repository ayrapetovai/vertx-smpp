package com.example.smpp.util.futures;

import com.example.smpp.util.SendBindRefusedException;
import com.example.smpp.util.SendPduChannelClosedException;
import com.example.smpp.util.SendPduFailedException;
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
