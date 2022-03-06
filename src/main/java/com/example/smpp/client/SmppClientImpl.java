package com.example.smpp.client;

import com.cloudhopper.smpp.pdu.BindTransceiver;
import com.example.smpp.PduRequestContext;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetClientImpl;
import io.vertx.core.spi.metrics.TCPMetrics;

public class SmppClientImpl extends NetClientImpl implements SmppClient {
  private final VertxInternal vertx;
  private final SmppClientOptions options;

  private SmppClientSession session;
  private Handler<PduRequestContext<?>> requestHandler;

  public SmppClientImpl(VertxInternal vertx, SmppClientOptions options, CloseFuture closeFuture) {
    super(vertx, options, closeFuture);
    this.vertx = vertx;
    this.options = options;
  }

  public SmppClientImpl(VertxInternal vertx, TCPMetrics metrics, SmppClientOptions options, CloseFuture closeFuture) {
    super(vertx, metrics, options, closeFuture);
    this.vertx = vertx;
    this.options = options;
  }

  @Override
  protected void initChannel(ChannelPipeline pipeline) {
    var worker = new SmppClientWorker(vertx.createEventLoopContext(), requestHandler);
    session = worker.handle(pipeline.channel());
  }

  @Override
  public Future<SmppClientSession> bind(String host, int port) {
    var sessionPromise = Promise.<SmppClientSession>promise();
    return connect(port, host)
        .compose(socket -> {
          return session.send(new BindTransceiver())
              .compose(bindTransceiverResp -> {
                sessionPromise.complete(session);
                return sessionPromise.future();
              });
        });
  }

  @Override
  public SmppClient onRequest(Handler<PduRequestContext<?>> pduRequestHandler) {
    this.requestHandler = pduRequestHandler;
    return this;
  }
}
