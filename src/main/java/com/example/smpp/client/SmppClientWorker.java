package com.example.smpp.client;

import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import com.example.smpp.SmppSession;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.SmppSessionPduDecoder;
import com.example.smpp.SmppSessionPduEncoder;
import com.example.smpp.Pool;
import com.example.smpp.session.ClientSessionConfigurator;
import com.example.smpp.session.SmppSessionOptions;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.VertxHandler;

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
    PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
    pipeline.addLast("smppDecoder", new SmppSessionPduDecoder(transcoder));
    pipeline.addLast("smppEncoder", new SmppSessionPduEncoder(transcoder));

    VertxHandler<SmppSessionImpl> handler = VertxHandler.create(chctx ->
        pool.add(id -> {
          var sessOpts = new SmppSessionOptions(id);
          configurator.handle(sessOpts);
          return new SmppSessionImpl(pool, id, context, chctx, sessOpts);
        })
    );
//    handler.addHandler(conn -> {
//      context.emit(conn, connectionHandler::handle);
//    });
    pipeline.addLast("handler2", handler);
    return handler.getConnection();
  }
}
