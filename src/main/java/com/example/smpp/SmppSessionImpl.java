package com.example.smpp;

import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.Unbind;
import com.example.smpp.session.SessionOptionsView;
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
  private final SessionOptionsView optionsView;
  private final long expireTimerId;
  private int sequenceCounter = 0;

  public SmppSessionImpl(Pool pool, Long id, ContextInternal context, ChannelHandlerContext chctx, SessionOptionsView optionsView) {
    super(context, chctx);
    Objects.requireNonNull(pool);
    Objects.requireNonNull(id);
    Objects.requireNonNull(optionsView);
    this.pool = pool;
    this.id = id;
    this.windowGuard = Semaphore.create(context.owner(), optionsView.getWindowSize());
    this.optionsView = optionsView;
    this.expireTimerId = vertx.setPeriodic(optionsView.getWindowMonitorInterval(), timerId -> {
      window.forAllExpired(expiredRecord -> {
        var exRec = (Window.RequestRecord<?>) expiredRecord;
        if (exRec.responsePromise != null) {
          log.trace("pdu.sequence={} expired on send", exRec.sequenceNumber);
          windowGuard.release(1);
          exRec.responsePromise.tryFail("Expired on send");
        }
      });
    });
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
              return closePromise.future();
            });
      } else {
        optionsView.getOnRequest().handle(new PduRequestContext<>((PduRequest<?>) msg, this));
      }
    } else {
      var pduResp = (PduResponse) msg;
      var respProm = window.complement(pduResp.getSequenceNumber());
      if (respProm != null) {
        windowGuard.release(1);
        respProm.tryComplete(pduResp);
      } else {
        this.optionsView.getOnUnexpectedResponse().handle(new PduResponseContext(pduResp, this));
      }
    }
  }

  @Override
  public <T extends PduResponse> Future<T> send(PduRequest<T> req) {
    return send(req, optionsView.getWriteTimeout());
  }

  @Override
  public <T extends PduResponse> Future<T> send(PduRequest<T> req, long sendTimeout) {
    if (channel().isOpen()) {
      return windowGuard.acquire(1, optionsView.getWindowWaitTimeout())
          .compose(v -> {
            req.setSequenceNumber(sequenceCounter++);
            Promise<T> respProm = window.<T>offer(req.getSequenceNumber(), System.currentTimeMillis() + sendTimeout);
            if (respProm != null) {
              if (channel().isOpen()) {
                var written = context.<Void>promise();
                written.future()
                    .onFailure(respProm::tryFail);
                writeToChannel(req, written);
              } else {
                windowGuard.release(1);
                respProm.tryFail("acquired; channel is closed");
              }
              return respProm.future();
            } else {
              return Future.failedFuture("unexpected pdu response");
            }
          });
    } else {
      return Future.failedFuture("not acquired; channel is closed");
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
    this.close(completion, optionsView.isSendUnbindOnClose());
  }

  @Override
  public void close(Promise<Void> completion, boolean sendUnbindRequired) {
    log.debug("session#{} closing", getId());
    if (sendUnbindRequired) {
      // TODO сделать ожидание отправок
      //  if (awaitAllSent) {
      //    awaitAllSent() -> { pauseSend; pauseReply }
      //      onComplete( ... )
      //  }
      var unbindRespFuture = send(new Unbind());
      if (optionsView.isAwaitUnbindResp()) {
        unbindRespFuture
          .compose(unbindResp -> {
            if (log.isDebugEnabled()) {
              log.debug("session#{} did unbindResp({}) close", getId(), unbindRespFuture.succeeded() ? "success" : "failure");
            }
            vertx.cancelTimer(this.expireTimerId);
            pool.remove(id);
            super.close(completion);
            optionsView.getOnClose().handle(this);
            return completion.future();
          });
      } else {
        vertx.cancelTimer(this.expireTimerId);
        pool.remove(id);
        super.close(completion);
        optionsView.getOnClose().handle(this);
      }
    } else {
      vertx.cancelTimer(this.expireTimerId);
      pool.remove(id);
      super.close(completion);
      optionsView.getOnClose().handle(this);
    }
  }

  public SessionOptionsView getOptions() {
    return this.optionsView;
  }

  @Override
  public Long getId() {
    return id;
  }
}
