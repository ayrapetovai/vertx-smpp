package io.vertx.smpp.server;

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

import io.vertx.smpp.model.Pool;
import io.vertx.smpp.session.SmppSessionImpl;
import io.vertx.smpp.session.ServerSessionConfigurator;
import io.vertx.smpp.session.SmppSessionOptions;
import io.netty.channel.*;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.NetServerOptions;
import io.vertx.core.net.impl.*;
import io.vertx.core.spi.metrics.TCPMetrics;
import io.vertx.smpp.util.channel.*;

import java.util.concurrent.TimeUnit;

public class SmppServerWorker implements Handler<Channel> {
  private final EventLoopContext context;
  private final Handler<ServerSessionConfigurator> configurator;
  private final VertxInternal vertx;
  private final SSLHelper sslHelper;
  private final NetServerOptions options;
  private final Pool pool;

  public SmppServerWorker(
      EventLoopContext context,
      VertxInternal vertx,
      SSLHelper sslHelper,
      NetServerOptions options,
      Handler<ServerSessionConfigurator> configurator,
      Pool pool
  ) {
    this.context = context;
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
    var sessOpts = new SmppSessionOptions();
    configurator.handle(sessOpts);

//      if (logEnabled) {
//        pipeline.addLast("logging", new LoggingHandler(options.getActivityLogDataFormat()));
//      }

    // FIXME sessOpts.writeTimeout is 0, before configurator.handle(sessOpts);
    var writeTimeout = sessOpts.getWriteTimeout();
    if (writeTimeout > 0) {
      WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS);
      pipeline.addLast("writeTimeout", writeTimeoutHandler);
    }

    PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
    pipeline.addLast("smppDecoder", new SmppSessionPduDecoder(transcoder));
    pipeline.addLast("smppEncoder", new SmppSessionPduEncoder(transcoder));

    int idleTimeout = options.getIdleTimeout();
    int readIdleTimeout = options.getReadIdleTimeout();
    int writeIdleTimeout = options.getWriteIdleTimeout();
    if (idleTimeout > 0 || readIdleTimeout > 0 || writeIdleTimeout > 0) {
      pipeline.addLast("idle", new IdleStateHandler(readIdleTimeout, writeIdleTimeout, idleTimeout, options.getIdleTimeoutUnit()));
    }

    VertxHandler<SmppSessionImpl> handler = VertxHandler.create(chctx -> {
      var sess = pool.add(id -> {
        return new SmppSessionImpl(pool, id, context, chctx, sessOpts, true);
      });
//            context.emit(chctx.handler(), connectionHandler::handle);
      return sess;
    });
    handler.addHandler(conn -> {
      // FIXME session is not bound yet, boundTo == null
//      context.emit(conn, sessOpts[0].getOnCreated()::handle);
    });
    pipeline.addLast("handler", handler);
    var sess  = handler.getConnection();
  }

  private void handleException(Throwable e) {
    // TODO
    e.printStackTrace();
  }
}
