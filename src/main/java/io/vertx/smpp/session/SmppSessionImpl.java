package io.vertx.smpp.session;

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

import io.vertx.smpp.SmppConstants;
import io.vertx.smpp.model.SequenceCounter;
import io.vertx.smpp.pdu.*;
import io.vertx.smpp.types.*;
import io.vertx.smpp.model.Pool;
import io.vertx.smpp.model.SmppSessionState;
import io.vertx.smpp.util.core.FlowControl;
import io.vertx.smpp.futures.ReplayPduFuture;
import io.vertx.smpp.futures.SendPduFuture;
import io.vertx.smpp.util.core.Semaphore;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.Metrics;
import io.vertx.core.spi.metrics.NetworkMetrics;
import io.vertx.smpp.util.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SmppSessionImpl extends ConnectionBase implements SmppSession {
  private static final Logger log = LoggerFactory.getLogger(SmppSessionImpl.class);

  private final Pool pool;
  private final Long id;
  private final Window window;
  private final Semaphore windowGuard;
  private final SmppSessionOptions options;
  private final boolean isServer;
  private final long windowMonitorId;
  private final long overflowMonitorId;
  private final SequenceCounter sequenceCounter = new SequenceCounter();
  private final byte thisInterface = SmppConstants.VERSION_3_4;

  private byte targetInterface;
  private SmppSessionState state = SmppSessionState.OPENED;
  private String boundToSystemId;
  private Object referenceObject;
  private boolean previousWritable;
  private boolean currentWritable;

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
    this.window = new Window(context);
    this.windowMonitorId = vertx.setPeriodic(options.getWindowMonitorInterval(), timerId -> {
      try {
        window.purgeAllExpired(expiredRecord -> {
          if (expiredRecord.responsePromise != null) {
            log.trace("pdu.sequence={} expired on send", expiredRecord.sequenceNumber);
            windowGuard.release(1);
            expiredRecord.responsePromise.tryFail(
                new SendPduRequestTimeoutException("no response on time, request expired", expiredRecord.offeredAt, expiredRecord.expiresAt)
            );
          }
        });
      } catch (Exception e) {
        log.error("Expiring periodic routing failed", e);
      }
    });

    if (options.getWriteQueueSize() > 0) {
      doSetWriteQueueMaxSize(options.getWriteQueueSize());
    }

    this.overflowMonitorId = vertx.setPeriodic(options.getOverflowMonitorInterval(), timerId -> {
      previousWritable = currentWritable;
      currentWritable = channel().isWritable();

      if (previousWritable && !currentWritable) {
        context.emit(options.getOnOverflowed());
      } else if (!previousWritable && currentWritable) {
        context.emit(options.getOnDrained());
      }
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
        this.state = SmppSessionState.UNBOUND;
        doPause();
        replyUnchecked(((Unbind) pdu).createResponse())
            .compose(ar -> {
              var closePromise = Promise.<Void>promise();
              close(closePromise, false);
              return closePromise.future();
            });
      } else if (pdu instanceof BaseBind<?>) {
        var bindRequest = (BaseBind<? extends BaseBindResp>) pdu;
        var respCmdStatus = options.getOnBindReceived()
            .apply(new BindInfo(bindRequest)); // TODO check returned status (it must be enum)
        var bindResp = bindRequest.createResponse();
        Helper.addInterfaceVersionTlv(bindResp, getThisInterfaceVersion(), bindRequest.getInterfaceVersion());
        bindResp.setSystemId(options.getSystemId());
        bindResp.setCommandStatus(respCmdStatus);
        this.reply(bindResp)
            .onComplete(respResult -> {
              if (respResult.failed() || respCmdStatus != SmppConstants.STATUS_OK) {
                SmppSessionImpl.this.close(Promise.promise(), false);
              } else {
                setBoundToSystemId(bindRequest.getSystemId());
                this.setState(Helper.sessionStateByCommandId(bindRequest.getCommandId()));
                this.options.getOnCreated().handle(this);
              }
            });
      } else {
        options.getOnRequest().handle(new PduRequestContext<>((PduRequest<?>) msg, this));
      }
    } else {
      var pduResp = (PduResponse) msg;
      if (pduResp.getCommandId() != SmppConstants.CMD_ID_UNBIND_RESP && !this.state.canReceive(isServer, pdu.getCommandId())) {
        this.options.getOnForbiddenResponse()
            .handle(new PduResponseContext(pduResp, this));
        return;
      }
      var respProm = window.complement(pduResp.getSequenceNumber());
      if (respProm != null && pduResp.hasSequenceNumberAssigned()) {
        windowGuard.release(1);
        if (pduResp.getCommandId() != SmppConstants.CMD_ID_GENERIC_NACK) {
          respProm.tryComplete(pduResp);
        } else {
          var resultMessage = pduResp.getResultMessage();
          respProm.tryFail(new SendPduNackkedException("request malformed: " + resultMessage, resultMessage, pduResp.getCommandStatus()));
        }
      } else {
        this.options.getOnUnexpectedResponse().handle(new PduResponseContext(pduResp, this));
      }
    }
  }

  private SendPduFuture<UnbindResp> sendUnbind() {
    return doSendUnchecked(new Unbind(), 0);
  }

  // TODO send must return PduResponseContext not only the response, but session, request and so on...
  @Override
  public <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req) {
    return send(req, 0);
  }

  @Override
  public <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req, long offerTimeout) {
    if (req.getCommandId() == SmppConstants.CMD_ID_UNBIND) {
      return SendPduFuture // TODO SendPduUnbindTriedException
          .failedFuture(new SendPduWrongOperationException("Do unbind by close session", state));
    }
    if (!this.state.canSend(isServer, req.getCommandId())) {
      return SendPduFuture
          .failedFuture(new SendPduWrongOperationException("For state " + state.name() + " operation " + req.getName() + " is wrong", state));
    }
    return doSendUnchecked(req, offerTimeout);
  }

  private  <T extends PduResponse> SendPduFuture<T> doSendUnchecked(PduRequest<T> req, long offerTimeout) {
    if (channel().isOpen()) {
      if (channel().bytesBeforeUnwritable() == 0) {
        return SendPduFuture.failedFuture(new SendPduWriteOverflowedException("Cannot write to channel", channel().bytesBeforeWritable()));
      }
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
              try {
                writeToChannel(req, written);
              } catch (Exception e) {
                written.fail(new SendPduWriteFailedException("write request to channel failed", e));
              }
            } else {
              windowGuard.release(1);
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
    return replyUnchecked(pduResponse);
  }

  private ReplayPduFuture<Void> replyUnchecked(PduResponse pduResponse) {
    var replyPromise = ReplayPduFuture.<Void>promise(context);
// TODO consider checks
//    this.channel().isWritable(); {this.channel().bytesBeforeWritable();}
//    this.channel().isActive();
    if (channel().isOpen()) {
      try {
        writeToChannel(pduResponse, replyPromise); // TODO timeout?
      } catch (Exception e) {
        replyPromise.fail(new SendPduWriteFailedException("write response to channel failed", e));
      }
    } else {
      replyPromise.fail(new SendPduChannelClosedException("write response failed, channel closed"));
    }
    return replyPromise.future();
  }

  /**
   * Closes the session - discards all pdus is flag is set.
   * todo timeouts
   * @param completion promise for connection is being closed
   */
  @Override
  public void close(Promise<Void> completion) {
    this.close(completion, options.isSendUnbindOnClose());
  }

  @Override
  public void close(Promise<Void> completion, boolean sendUnbindRequired) {
    if (state == SmppSessionState.CLOSED) {
      completion.tryComplete();
      return;
    }
    if (!options.isDiscardAllOnUnbind()) {
      FlowControl
          .awaitCondition(vertx.getOrCreateContext(), () -> window.size() == 0, options.getDiscardTimeout())
          .onComplete(ar -> {
            if (ar.succeeded()) {
              log.debug("awaiting of window succeed");
            } else {
              log.error("awaiting of window failed, window size {}",  window.size(), ar.cause());
            }
            closeWithUnbind(completion, sendUnbindRequired);
          });
    } else {
      log.debug("close without waiting for window to get drained");
      closeWithUnbind(completion, sendUnbindRequired);
    }
  }

  @Override
  protected void handleClosed() {
    super.handleClosed();
    close(context.promise(), false);
  }

  private void closeWithUnbind(Promise<Void> completion, boolean sendUnbindRequired) {
    this.state = SmppSessionState.UNBOUND;
    log.debug("session#{} closing", getId());
    if (sendUnbindRequired) {
      var unbindRespFuture = sendUnbind();
      if (options.isAwaitUnbindResp()) {
        var taskId = -1L;
        if (options.getUnbindTimeout() > 0) {
          taskId = vertx.setTimer(options.getUnbindTimeout(), id -> {
            log.debug("unbind response timed out");
            dispose(completion);
          });
        }
        var unbindTimeoutTaskId = taskId; // keep compiler happy
        unbindRespFuture
            .onSuccess(unbindResp -> {
              if (unbindTimeoutTaskId > 0) {
                vertx.cancelTimer(unbindTimeoutTaskId);
              }

              if (log.isDebugEnabled()) {
                if (unbindRespFuture.succeeded()) {
                  log.debug("session#{} did unbindResp(success), disposing session", getId());
                } else {
                  log.debug("session#{} did unbindResp(failure), disposing session, error: {}", getId(), unbindRespFuture.cause().getMessage());
                }
              }
              dispose(completion);
            })
            .onFailure(e -> {
              if (unbindTimeoutTaskId > 0) {
                vertx.cancelTimer(unbindTimeoutTaskId);
              }
              log.debug("error on retrieving unbind_resp, error: {}", e.getMessage());
              dispose(completion);
            });
      }
    } else {
      dispose(completion);
    }
  }

  private void dispose(Promise<Void> completion) {
    vertx.cancelTimer(windowMonitorId);
    vertx.cancelTimer(overflowMonitorId);
    doPause();
    flushBytesRead();
    pool.remove(id);
    state = SmppSessionState.CLOSED;
    super.close(completion);
    completion.future()
        .onComplete(nothing -> {
          options.getOnClose().handle(this);
          log.debug("session disposed, pool.size={}, windows.size={}", pool.size(), window.size());
          window.discardAll();
        });
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

  public void setState(SmppSessionState state) {
    log.debug("{} moved to state {}", this, state);
    this.state = state;
  }

  @Override
  public boolean isOpened() {
    return this.state == SmppSessionState.OPENED;
  }

  @Override
  public boolean isBound() {
    return this.state == SmppSessionState.BOUND_RX || this.state == SmppSessionState.BOUND_TX || this.state == SmppSessionState.BOUND_TRX;
  }

  @Override
  public boolean isUnbound() {
    return this.state == SmppSessionState.UNBOUND;
  }

  @Override
  public boolean isClosed() {
    return this.state == SmppSessionState.CLOSED;
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
  public Metrics getMetrics() {
    return new SmppSessionMetrics();
  }

  @Override
  public boolean isMetricsEnabled() {
    return options.getCountersEnabled();
  }

  public byte getThisInterfaceVersion() {
    return thisInterface;
  }

  public byte getTargetInterfaceVersion() {
    return targetInterface;
  }

  public void setTargetInterfaceVersion(byte targetInterface) {
    this.targetInterface = targetInterface;
  }

  @Override
  public boolean areOptionalParametersSupported() {
    return (this.targetInterface >= SmppConstants.VERSION_3_4);
  }

  @Override
  public boolean canSend(int commandId) {
    return this.state.canSend(isServer, commandId);
  }

  @Override
  public boolean canReceive(int commandId) {
    return this.state.canReceive(isServer, commandId);
  }

  @Override
  public String toString() {
    return "Session(" + state + ":" + id + (isServer? "-server": "-client") + " -> " + boundToSystemId + ')';
  }
}
