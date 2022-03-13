package com.example.smpp.client;

import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.SmppSessionPduDecoder;
import com.example.smpp.SmppSessionPduEncoder;
import com.example.smpp.Pool;
import com.example.smpp.session.ClientSessionConfigurator;
import com.example.smpp.session.SmppSessionOptions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.VertxHandler;

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
