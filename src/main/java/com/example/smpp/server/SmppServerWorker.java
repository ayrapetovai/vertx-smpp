package com.example.smpp.server;

import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import com.example.smpp.Pool;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.SmppSessionPduDecoder;
import com.example.smpp.SmppSessionPduEncoder;
import com.example.smpp.session.ServerSessionConfigurator;
import com.example.smpp.session.SmppSessionOptions;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.vertx.core.Handler;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.impl.*;
import io.vertx.core.spi.metrics.TCPMetrics;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;

public class SmppServerWorker implements Handler<Channel> {
  final EventLoopContext context;
  private Function<ServerSessionConfigurator, Boolean> configurator;
  Supplier<ContextInternal> streamContextSupplier;
  SmppServerImpl smppServer;
  VertxInternal vertx;
  SSLHelper sslHelper;
  NetServerOptions options;
  final Pool pool;

  public SmppServerWorker(
      EventLoopContext context,
      Supplier<ContextInternal> streamContextSupplier,
      SmppServerImpl smppServer,
      VertxInternal vertx,
      SSLHelper sslHelper,
      NetServerOptions options,
      Function<ServerSessionConfigurator, Boolean> configurator,
      Pool pool
  ) {
    this.context = context;
    this.streamContextSupplier = streamContextSupplier;
    this.smppServer = smppServer;
    this.vertx = vertx;
    this.sslHelper = sslHelper;
    this.options = options;
    this.configurator = configurator;
    this.pool = pool;
  }

  @Override
  public void handle(Channel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    if (sslHelper.isSSL()) {
      SslHandler handler = new SslHandler(sslHelper.createEngine(vertx));
      handler.setHandshakeTimeout(sslHelper.getSslHandshakeTimeout(), sslHelper.getSslHandshakeTimeoutUnit());
      pipeline.addLast("ssl", handler);
      ChannelPromise p = ch.newPromise();
      pipeline.addLast("handshaker", new SslHandshakeCompletionHandler(p));
      p.addListener(future -> {
        if (future.isSuccess()) {
          configureSmpp(pipeline);
        } else {
          handleException(future.cause());
        }
      });
    } else {
      configureSmpp(pipeline);
    }
  }

  private TCPMetrics<?> getMetrics() {
    return null;
  }

  private void configureSmpp(ChannelPipeline pipeline) {
    //      if (logEnabled) {
//        pipeline.addLast("logging", new LoggingHandler(options.getActivityLogDataFormat()));
//      }

    PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
    pipeline.addLast("smppDecoder", new SmppSessionPduDecoder(transcoder));
    pipeline.addLast("smppEncoder", new SmppSessionPduEncoder(transcoder));

//      if (options.isDecompressionSupported()) {
//        pipeline.addLast("inflater", new HttpContentDecompressor(false));
//      }

//      if (options.isCompressionSupported()) {
//        pipeline.addLast("deflater", new HttpChunkContentCompressor(options.getCompressionLevel()));
//      }

//    if (sslHelper.isSSL()) {
//      // only add ChunkedWriteHandler when SSL is enabled otherwise it is not needed as FileRegion is used.
//      pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());       // For large file / sendfile support
//    }
    int idleTimeout = options.getIdleTimeout();
    int readIdleTimeout = options.getReadIdleTimeout();
    int writeIdleTimeout = options.getWriteIdleTimeout();
    if (idleTimeout > 0 || readIdleTimeout > 0 || writeIdleTimeout > 0) {
      pipeline.addLast("idle", new IdleStateHandler(readIdleTimeout, writeIdleTimeout, idleTimeout, options.getIdleTimeoutUnit()));
    }

    // TODO uncomment and implement
//    if (!smpp.requestAccept()) {
//      sendServiceUnavailable(pipeline.channel());
//      return;
//    }

    var sessOpts = new SmppSessionOptions[]{null};
    VertxHandler<SmppSessionImpl> handler = VertxHandler.create(chctx -> {
      var sess = pool.add(id -> {
        sessOpts[0] = new SmppSessionOptions(id);
        var allowBind = configurator.apply(sessOpts[0]); // TODO allowBind, wtf?
        return new SmppSessionImpl(pool, id, context, chctx, sessOpts[0], true);
      });
//            context.emit(chctx.handler(), connectionHandler::handle);
      return sess;
    });
    handler.addHandler(conn -> {
      // FIXME сессия еще не связана, boundTo == null
//      context.emit(conn, sessOpts[0].getOnCreated()::handle);
    });
    pipeline.addLast("handler", handler);
    var sess  = handler.getConnection();
  }

  private void handleException(Throwable e) {
    // TODO
    e.printStackTrace();
  }

  private void sendServiceUnavailable(Channel ch) {
    // TODO copied AS IS, make TO BE
    ch.writeAndFlush(
        Unpooled.copiedBuffer("HTTP/1.1 503 Service Unavailable\r\n" +
          "Content-Length:0\r\n" +
          "\r\n", StandardCharsets.ISO_8859_1))
      .addListener(ChannelFutureListener.CLOSE);
  }
}
