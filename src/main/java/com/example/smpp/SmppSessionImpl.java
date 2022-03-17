package com.example.smpp;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import com.example.smpp.model.BindInfo;
import com.example.smpp.model.SmppSessionState;
import com.example.smpp.session.SessionOptionsView;
import com.example.smpp.session.SmppSessionOptions;
import com.example.smpp.util.*;
import com.example.smpp.util.futures.ReplayPduFuture;
import com.example.smpp.util.futures.SendPduFuture;
import com.example.smpp.util.core.Semaphore;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.core.spi.metrics.NetworkMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static com.example.smpp.util.Helper.addInterfaceVersionTlv;
import static com.example.smpp.util.Helper.sessionStateByCommandId;

public class SmppSessionImpl extends ConnectionBase implements SmppSession {
  private static final Logger log = LoggerFactory.getLogger(SmppSessionImpl.class);

  private final Pool pool;
  private final Long id;
  private final Window window = new Window();
  private final Semaphore windowGuard;
  private final SmppSessionOptions options;
  private final boolean isServer;
  private final long expireTimerId;
  private final SequenceCounter sequenceCounter = new SequenceCounter();
  private final byte thisInterface = SmppConstants.VERSION_3_4;

  private byte targetInterface;
  private SmppSessionState state = SmppSessionState.OPEN;
  private String boundToSystemId;
  private Object referenceObject;

  public SmppSessionImpl(Pool pool, Long id, ContextInternal context, ChannelHandlerContext chctx, SmppSessionOptions options, boolean isServer) {
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
        var respCmdStatus = options.getOnBindReceived()
            .apply(new BindInfo<>(bindRequest));
        var bindResp = bindRequest.createResponse();
        addInterfaceVersionTlv(bindResp, getThisInterface(), bindRequest.getInterfaceVersion());
        bindResp.setSystemId(options.getSystemId());
        bindResp.setCommandStatus(respCmdStatus);
        this.reply(bindResp)
            .onComplete(respResult -> {
              if (respResult.failed() || respCmdStatus != SmppConstants.STATUS_OK) {
                SmppSessionImpl.this.close(Promise.promise(), false);
              } else {
                setBoundToSystemId(bindRequest.getSystemId());
                this.setState(sessionStateByCommandId(bindRequest.getCommandId()));
                this.options.getOnCreated().handle(this);
              }
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

  private SendPduFuture<UnbindResp> sendUnbind() {
    return send(new Unbind(), 0);
  }

  @Override
  public <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req) {
    if (req instanceof Unbind) {
      return SendPduFuture
          .failedFuture(new SendPduWrongOperationException("Do unbind by close session", state));
    }
    return send(req, 0);
  }

  @Override
  public <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req, long offerTimeout) {
    if (!this.state.canSend(isServer, req.getCommandId())) {
      return SendPduFuture
          .failedFuture(new SendPduWrongOperationException("For state " + state.name() + " operation " + req.getName() + " is wrong", state));
    }
    if (channel().isOpen()) {
      var sendPromise = SendPduFuture.<T>promise(vertx.getOrCreateContext());
      windowGuard.acquire(1, offerTimeout)
          .onSuccess(v -> {
            if (!req.hasSequenceNumberAssigned()) {
              req.setSequenceNumber(sequenceCounter.getAndInc());
            }
            window.offer(req, sendPromise, System.currentTimeMillis() + options.getRequestExpiryTimeout());
            if (channel().isOpen()) {
              var written = context.<Void>promise();
              written.future()
                  .onFailure(e -> {
                    windowGuard.release(1);
                    sendPromise.tryFail(e);
                  });
              writeToChannel(req, written);
            } else {
              windowGuard.release(1);
              // TODO если задана стратегия очистки окна на закрытии (надо собрать все запросы и передать их в хендлер)
              //  но делать это надо не здесь, а в адапторе канала, для этого адаптер должен знать о сессии, чтобы достать окно и очистить его.
              sendPromise.tryFail(new SendPduChannelClosedException("channel is closed"));
            }
          })
          .onFailure(e -> {
            var winSz = windowGuard.getCounter();
            sendPromise.fail(new SendPduWindowTimeoutException("window acquired, but timeout window with size " + winSz, winSz));
          });
      return sendPromise;
    } else {
      return SendPduFuture.failedFuture(new SendPduChannelClosedException("channel is closed"));
    }
  }

  @Override
  public ReplayPduFuture<Void> reply(PduResponse pduResponse) {
    if (!this.state.canSend(isServer, pduResponse.getCommandId())) {
      return ReplayPduFuture.failedFuture(new SendPduWrongOperationException(state + " forbidden for reply, pdu " + pduResponse.getName(), state));
    }
    var replyPromise = ReplayPduFuture.<Void>promise(context);
// TODO consider checks
//    this.channel().isWritable(); {this.channel().bytesBeforeWritable();}
//    this.channel().isActive();
    writeToChannel(pduResponse, replyPromise);
    return replyPromise.future();
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
//      doPause();
//      flush();
//      flushBytesRead();
      var unbindRespFuture = sendUnbind();
      if (options.isAwaitUnbindResp()) {
        unbindRespFuture
          .onComplete(unbindResp -> {
            if (log.isDebugEnabled()) {
              log.debug("session#{} did unbindResp({}), disposing session", getId(), unbindRespFuture.succeeded() ? "success" : "failure");
            }
            dispose(completion);
          });
      }
    } else {
      dispose(completion);
    }
  }

  private void dispose(Promise<Void> completion) {
    vertx.cancelTimer(expireTimerId);
    pool.remove(id);
    state = SmppSessionState.CLOSED;
    super.close(completion);
    options.getOnClose().handle(this);
    log.debug("session disposed");
  }

  public SessionOptionsView getOptions() {
    return this.options;
  }

  @Override
  public void setReferenceObject(Object object) {
    this.referenceObject = object;
  }

  @Override
  public <RefObj> RefObj getReferenceObject(Class<RefObj> clazz) {
    return clazz.cast(referenceObject);
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

  public byte getThisInterface() {
    return thisInterface;
  }

  public byte getTargetInterface() {
    return targetInterface;
  }

  public void setTargetInterface(byte targetInterface) {
    this.targetInterface = targetInterface;
  }
}
