package com.example.smpp.server;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.Window;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import com.example.smpp.util.vertx.Semaphore;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.net.impl.SSLHelper;
import io.vertx.core.spi.metrics.NetworkMetrics;

import java.util.function.Supplier;

public class SmppServerSession extends ConnectionBase implements SmppSession {
  private final Window window = new Window();
  private final Semaphore windowGuard;
  private int sequenceCounter = 0;
  Supplier<ContextInternal> streamContextSupplier;
  SSLHelper sslHelper;
  NetServerOptions options;
  SmppServerConnectionHandler handler;

  public SmppServerSession(Supplier<ContextInternal> streamContextSupplier, SSLHelper sslHelper, NetServerOptions options, ChannelHandlerContext chctx, EventLoopContext context) {
    super(context, chctx);
    windowGuard = Semaphore.create(context.owner(), 500);
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
    if (msg instanceof PduRequest) {
      handler.requestHandler.handle(new PduRequestContext<>((PduRequest<?>) msg, this));
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
              writeToChannel(req, written);
              written.future()
                  .onFailure(respProm::tryFail);
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
    writeToChannel(pduResponse, written);
    return written.future();
  }
}
