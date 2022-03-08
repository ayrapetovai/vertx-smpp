package com.example.smpp;

import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.Unbind;
import com.example.smpp.server.Pool;
import com.example.smpp.util.vertx.Semaphore;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.NetworkMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SmppSessionImpl extends ConnectionBase implements SmppSession {
  private static final Logger log = LoggerFactory.getLogger(SmppSessionImpl.class);

  private final Pool pool;
  private final Long id;
  private final Window window = new Window();
  private final Semaphore windowGuard;
  private final SmppSessionCallbacks callbacks;
  private int sequenceCounter = 0;
  private boolean sendUnbind = true;
  private boolean awaitUnbindResp = true;

  public SmppSessionImpl(Pool pool, Long id, ContextInternal context, ChannelHandlerContext chctx, SmppSessionCallbacks callbacks) {
    super(context, chctx);
    Objects.requireNonNull(pool);
    Objects.requireNonNull(id);
    Objects.requireNonNull(callbacks);
    this.pool = pool;
    this.id = id;
    this.windowGuard = Semaphore.create(context.owner(), 600);
    this.callbacks = callbacks;
  }

  @Override
  public NetworkMetrics metrics() {
    return null;
  }

  @Override
  protected void handleInterestedOpsChanged() {

  }

  /**
   * generic_nack as response?
   */
  @Override
  public void handleMessage(Object msg) {
    var pdu = (Pdu) msg;
    if (pdu.isRequest()) {
      if (pdu instanceof Unbind) {
        doPause();
        reply(((Unbind) pdu).createResponse())
            .compose(ar -> {
              var closePromise = Promise.<Void>promise();
              close(closePromise, false);
              closePromise.future().onComplete(a -> {
                //  fireSessionClosed
                log.debug("closed");
              });
              return closePromise.future();
            });
      } else {
        callbacks.requestHandler.handle(new PduRequestContext<>((PduRequest<?>) msg, this));
      }
    } else {
      var pduResp = (PduResponse) msg;
      var respProm = window.complement(pduResp.getSequenceNumber());
      if (respProm != null) { // TODO при протухании запроса, его надо не только удалить из окна но и вернуть ресурс в семафор
        respProm.tryComplete(pduResp);
        windowGuard.release(1);
      }
    }
  }

  @Override
  public<T extends PduResponse> Future<T> send(PduRequest<T> req) {
    if (channel().isOpen()) {
      return windowGuard.aquire(1)
          .compose(v -> {
            req.setSequenceNumber(sequenceCounter++);
            Promise<T> respProm = window.<T>offer(req.getSequenceNumber(), System.currentTimeMillis() + 1000);
            if (channel().isOpen()) {
              var written = context.<Void>promise();
              written.future()
                  .onFailure(respProm::tryFail);
              writeToChannel(req, written);
            } else {
              windowGuard.release(1);
              respProm.tryFail("aquired; channel is closed");
            }
            return respProm.future();
          });
    } else {
      return Future.failedFuture("not aquired; channel is closed");
    }
  }

  @Override
  public Future<Void> reply(PduResponse pduResponse) {
    var written = context.<Void>promise();
// TODO consider checks
//    this.channel().isWritable(); {this.channel().bytesBeforeWritable();}
//    this.channel().isActive();
    writeToChannel(pduResponse, written);
    return written.future();
  }

  /**
   * Closes the session - discards all pdus.
   * todo timeouts
   * @param completion promise for connection is being closed
   */
  @Override
  public void close(Promise<Void> completion) {
    this.close(completion, this.sendUnbind);
  }

  public void close(Promise<Void> completion, boolean sendUnbindRequired) {
    log.debug("session#{} closing", getId());
    if (sendUnbindRequired) {
      // TODO сделать ожидание отправок
      //  if (awaitAllSent) {
      //    awaitAllSent() -> { pauseSend; pauseReply }
      //      onComplete( ... )
      //  }
      var unbindRespFuture = send(new Unbind());
      if (awaitUnbindResp) {
        unbindRespFuture
          .compose(unbindResp -> {
            log.debug("session#{} did unbindResp close", getId());
            pool.remove(id);
            super.close(completion);
            return completion.future();
          });
      } else {
        pool.remove(id);
        super.close(completion);
      }
    } else {
      pool.remove(id);
      super.close(completion);
    }
  }

  @Override
  public Long getId() {
    return id;
  }
}
