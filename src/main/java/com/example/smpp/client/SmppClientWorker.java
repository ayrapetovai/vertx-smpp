package com.example.smpp.client;

import com.cloudhopper.smpp.channel.SmppSessionPduDecoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.SmppSessionPduEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.Handler;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.net.impl.VertxHandler;

public class SmppClientWorker {
  private final EventLoopContext context;
  private final Handler<PduRequestContext<?>> requestHandler;
  private final Handler<SmppSession> hello = (sess) -> {
    System.out.println("hello sess");
  };
  private final Handler<SmppSession> connectionHandler = (sess) -> {
    System.out.println("incoming sess");
  };

  public SmppClientWorker(EventLoopContext context, Handler<PduRequestContext<?>> requestHandler) {
    this.context = context;
    this.requestHandler = requestHandler;
  }

  public SmppSession handle(Channel ch) {
    ChannelPipeline pipeline = ch.pipeline();
    PduTranscoder transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext());
    pipeline.addLast("smppDecoder", new SmppSessionPduDecoder(transcoder));
    pipeline.addLast("smppEncoder", new SmppSessionPduEncoder(transcoder));

    VertxHandler<SmppSessionImpl> handler = VertxHandler.create(chctx ->
        new SmppSessionImpl(context, chctx, requestHandler)
    );
    handler.addHandler(conn -> {
      context.emit(conn, connectionHandler::handle);
    });
    pipeline.addLast("handler2", handler);
    var sess  = handler.getConnection();
    hello.handle(sess);
    return sess;
  }
}
