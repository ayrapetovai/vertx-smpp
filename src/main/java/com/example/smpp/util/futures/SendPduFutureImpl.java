package com.example.smpp.util.futures;

import com.example.smpp.util.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.impl.future.PromiseInternal;

import static com.example.smpp.model.SendPduExceptionType.*;

class SendPduFutureImpl<T> extends AbstractPduFuture<T> implements SendPduFuture<T> {

  public SendPduFutureImpl(PromiseInternal<T> promise) {
    super(promise);
  }

  @Override
  public SendPduFuture<T> future() {
    return this;
  }

  @Override
  public SendPduFuture<T> onComplete(Handler<AsyncResult<T>> handler) {
    delegateAsPromise.onComplete(handler);
    return this;
  }

  @Override
  public SendPduFuture<T> onSuccess(Handler<T> handler) {
    delegateAsPromise.onSuccess(handler);
    return this;
  }

  @Override
  public SendPduFuture<T> onFailure(Handler<Throwable> handler) {
    delegateAsPromise.onFailure(handler);
    return this;
  }

  @Override
  public SendPduFuture<T> onWindowTimeout(Handler<SendPduWindowTimeoutException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == WINDOW_TIMEOUT) {
          handler.handle((SendPduWindowTimeoutException)e);
        }
      }
    });
    return this;
  }

  @Override
  public SendPduFuture<T> onChannelClosed(Handler<SendPduChannelClosedException> handler) {
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
  public SendPduFuture<T> onWrongOperation(Handler<SendPduWrongOperationException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == WRONG_OPERATION) {
          handler.handle((SendPduWrongOperationException)e);
        }
      }
    });
    return this;
  }

  @Override
  public SendPduFuture<T> onDiscarded(Handler<SendPduDiscardedException> handler) {
    delegateAsPromise.onFailure(e -> {
      if (e instanceof SendPduFailedException) {
        var error = (SendPduFailedException) e;
        if (error.getType() == REQUEST_DISCARDED_ON_CLOSE) {
          handler.handle((SendPduDiscardedException)e);
        }
      }
    });
    return this;
  }
}
