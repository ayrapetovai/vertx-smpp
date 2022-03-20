package io.vertx.smpp.client;

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

import io.vertx.smpp.session.SmppSessionImpl;
import io.vertx.smpp.model.Pool;
import io.vertx.smpp.session.ClientSessionConfigurator;
import io.vertx.smpp.session.SmppSessionOptions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.VertxHandler;
import io.vertx.smpp.util.channel.*;

import java.util.concurrent.TimeUnit;

public class SmppClientWorker {
  private final EventLoopContext context;
  private final Pool pool;
  private final Handler<ClientSessionConfigurator> configurator;

  public SmppClientWorker(EventLoopContext context, Handler<ClientSessionConfigurator> configurator, Pool pool) {
    this.context = context;
    this.configurator = configurator;
    this.pool = pool;
  }

  public SmppSessionImpl handle(Channel ch) {
    ChannelPipeline pipeline = ch.pipeline();

    var sessOpts = new SmppSessionOptions();
    configurator.handle(sessOpts);

    var writeTimeout = sessOpts.getWriteTimeout();
    if (writeTimeout > 0) {
      // FIXME WriteTimeoutHandler does not fire
      WriteTimeoutHandler writeTimeoutHandler = new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS);
      pipeline.addLast("writeTimeout", writeTimeoutHandler);
    }

    PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
    pipeline.addLast("smppDecoder", new SmppSessionPduDecoder(transcoder));
    pipeline.addLast("smppEncoder", new SmppSessionPduEncoder(transcoder));

    VertxHandler<SmppSessionImpl> handler = VertxHandler.create(chctx ->
        pool.add(id -> {
          return new SmppSessionImpl(pool, id, context, chctx, sessOpts, false);
        })
    );
//    handler.addHandler(conn -> {
//      context.emit(conn, connectionHandler::handle);
//    });
    pipeline.addLast("handler2", handler);
    return handler.getConnection();
  }
}
