package com.example.smpp;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import com.example.smpp.model.SmppSessionState;
import com.example.smpp.session.SessionOptionsView;
import com.example.smpp.util.SequenceCounter;
import com.example.smpp.util.vertx.Semaphore;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.Metrics;
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
  private final SessionOptionsView options;
  private final boolean isServer;
  private final long expireTimerId;
  private final SequenceCounter sequenceCounter = new SequenceCounter();

  private SmppSessionState state = SmppSessionState.OPEN;
  private String boundToSystemId;

  public SmppSessionImpl(Pool pool, Long id, ContextInternal context, ChannelHandlerContext chctx, SessionOptionsView options, boolean isServer) {
    super(context, chctx);
    Objects.requireNonNull(pool);
    Objects.requireNonNull(id);
    Objects.requireNonNull(options);
    this.pool = pool;
    this.id = id;
    this.windowGuard = Semaphore.create(context.owner(), options.getWindowSize());
    this.options = options;
    this.isServer = isServer;
    this.expireTimerId = vertx.setPeriodic(options.getWindowMonitorInterval(), timerId -> {
      window.purgeAllExpired(expiredRecord -> {
        var exRec = (Window.RequestRecord<?>) expiredRecord;
        if (exRec.responsePromise != null) {
          log.trace("pdu.sequence={} expired on send", exRec.sequenceNumber);
          windowGuard.release(1);
          exRec.responsePromise.tryFail("no response on time, request expired");
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
      if (!this.state.canReceive(isServer, pdu.getCommandId())) {
        this.options.getOnForbiddenRequest()
            .handle(new PduRequestContext<>((PduRequest<?>) pdu, this));
        return;
      }
      if (pdu instanceof Unbind) {
        doPause();
        reply(((Unbind) pdu).createResponse())
            .compose(ar -> {
              var closePromise = Promise.<Void>promise();
              close(closePromise, false);
              return closePromise.future();
            });
        this.state = SmppSessionState.UNBOUND;
      } else if (pdu instanceof BaseBind<?>) {
        var bindRequest = (BaseBind<? extends BaseBindResp>) pdu;
        options.getOnBindReceived().handle(new PduRequestContext<>(bindRequest, this));
        var bindResp = bindRequest.createResponse();
        bindResp.setSystemId(options.getSystemId());
        this.reply(bindResp)
            .onSuccess(vd -> {
              // TODO здесь должны вызываться setBoundToSystemId, onCreated и все такое, если успешная отправка bind_resp обязательна (она обязательна)?
              setBoundToSystemId(bindRequest.getSystemId());
              this.options.getOnCreated().handle(this);
              switch (bindRequest.getCommandId()) {
                case SmppConstants.CMD_ID_BIND_RECEIVER: this.setState(SmppSessionState.BOUND_RX); break;
                case SmppConstants.CMD_ID_BIND_TRANSMITTER: this.setState(SmppSessionState.BOUND_TX); break;
                case SmppConstants.CMD_ID_BIND_TRANSCEIVER: this.setState(SmppSessionState.BOUND_TRX); break;
                default:
                  // TODO ошибка, неожиданный тип pdu
              }
            })
            .onFailure(ex -> {
              SmppSessionImpl.this.close(Promise.promise(), false);
            });
      } else {
        options.getOnRequest().handle(new PduRequestContext<>((PduRequest<?>) msg, this));
      }
    } else {
      var pduResp = (PduResponse) msg;
      if (!this.state.canReceive(isServer, pdu.getCommandId())) {
        this.options.getOnForbiddenResponse()
            .handle(new PduResponseContext(pduResp, this));
        return;
      }
      var respProm = window.complement(pduResp.getSequenceNumber());
      if (respProm != null) {
        windowGuard.release(1);
        respProm.tryComplete(pduResp);
      } else {
        this.options.getOnUnexpectedResponse().handle(new PduResponseContext(pduResp, this));
      }
    }
  }

  @Override
  public <T extends PduResponse> Future<T> send(PduRequest<T> req) {
    return send(req, 0);
  }

  @Override
  public <T extends PduResponse> Future<T> send(PduRequest<T> req, long offerTimeout) {
    if (!this.state.canSend(isServer, req.getCommandId())) {
      return Future.failedFuture(state + " forbidden for send, pdu " + req.getName());
    }
    if (channel().isOpen()) {
      return windowGuard.acquire(1, offerTimeout)
          .compose(v -> {
            if (!req.hasSequenceNumberAssigned()) {
              req.setSequenceNumber(sequenceCounter.getAndInc());
            }
            Promise<T> respProm = window.<T>offer(req.getSequenceNumber(), System.currentTimeMillis() + options.getRequestExpiryTimeout());
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
    if (!this.state.canSend(isServer, pduResponse.getCommandId())) {
      return Future.failedFuture(state + " forbidden for reply, pdu " + pduResponse.getName());
    }
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
    this.close(completion, options.isSendUnbindOnClose());
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
      if (options.isAwaitUnbindResp()) {
        unbindRespFuture
          .compose(unbindResp -> {
            if (log.isDebugEnabled()) {
              log.debug("session#{} did unbindResp({}) close", getId(), unbindRespFuture.succeeded() ? "success" : "failure");
            }
            vertx.cancelTimer(expireTimerId);
            pool.remove(id);
            state = SmppSessionState.CLOSED;
            super.close(completion);
            options.getOnClose().handle(this);
            return completion.future();
          });
      } else {
        vertx.cancelTimer(expireTimerId);
        pool.remove(id);
        state = SmppSessionState.CLOSED;
        super.close(completion);
        options.getOnClose().handle(this);
      }
    } else {
      vertx.cancelTimer(expireTimerId);
      pool.remove(id);
      state = SmppSessionState.CLOSED;
      super.close(completion);
      options.getOnClose().handle(this);
    }
  }

  public SessionOptionsView getOptions() {
    return this.options;
  }

  @Override
  public SmppSessionState getState() {
    return this.state;
  }

  public void setState(SmppSessionState state) {
    log.debug("session{} moved to state {}", this, state);
    this.state = state;
  }

  @Override
  public Long getId() {
    return this.id;
  }

  public void setBoundToSystemId(String boundToSystemId) {
    this.boundToSystemId = boundToSystemId;
  }

  @Override
  public String getBoundToSystemId() {
    return this.boundToSystemId;
  }

  @Override
  public String toString() {
    return "Session(" + id + (isServer? ":server": ":client") + "->" + boundToSystemId + ')';
  }

  @Override
  public Metrics getMetrics() {
    return new SmppSessionMetrics();
  }

  @Override
  public boolean isMetricsEnabled() {
    return options.getCountersEnabled();
  }
}
