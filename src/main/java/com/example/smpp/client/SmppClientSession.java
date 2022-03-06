package com.example.smpp.client;

import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.Window;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import com.example.smpp.util.Semaphore;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.spi.metrics.NetworkMetrics;

public class SmppClientSession extends ConnectionBase implements SmppSession {
  private final Window window = new Window();
  private final Handler<PduRequestContext<?>> requestHandler;
  private int sequenceCounter = 0;
  private final Semaphore windowGuard;

  protected SmppClientSession(ContextInternal context, ChannelHandlerContext chctx, Handler<PduRequestContext<?>> requestHandler) {
    super(context, chctx);
    this.requestHandler = requestHandler;
    windowGuard = Semaphore.create(context.owner(), 500);
  }

  @Override
  public NetworkMetrics metrics() {
    return null;
  }

  @Override
  protected void handleInterestedOpsChanged() {

  }

  @Override
  public void handleMessage(Object msg) {
    var pdu = (Pdu) msg;
    if (pdu.isResponse()) {
      var pduResp = (PduResponse) msg;
      var respProm = window.complement(pduResp.getSequenceNumber());
      respProm.tryComplete(pduResp);
      windowGuard.release(1);
    } else {
      var pduReq = (PduRequest<?>) msg;
      requestHandler.handle(new PduRequestContext<>(pduReq, this));
    }
  }

  public<T extends PduResponse> Future<T> send(PduRequest<T> req) {
    if (channel().isOpen()) {
      return windowGuard.aquire(1)
          .compose(v -> {
            req.setSequenceNumber(sequenceCounter++);
            Promise<T> respProm = window.<T>offer(req.getSequenceNumber(), System.currentTimeMillis() + 1000);
            var written = context.<Void>promise();
            writeToChannel(req, written);
            written.future()
                .onFailure(respProm::tryFail);
            return respProm.future();
          });
    } else {
      return Future.failedFuture("channel is closed");
    }
  }

  @Override
  public Future<Void> reply(PduResponse pduResponse) {
    var written = context.<Void>promise();
    writeToChannel(pduResponse, written);
    return written.future();
  }
}
