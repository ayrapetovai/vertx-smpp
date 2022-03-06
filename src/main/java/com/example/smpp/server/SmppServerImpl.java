package com.example.smpp.server;

import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import io.netty.channel.Channel;
import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.*;

public class SmppServerImpl extends NetServerImpl implements Cloneable, SmppServer {

  SmppServerConnectionHandler handler = new SmppServerConnectionHandler(this);

  public SmppServerImpl(VertxInternal vertx, SmppServerOptions options) {
    super(vertx, options);
    connectHandler(sock -> {
    });
  }

  @Override
  public Future<Void> close() {
    return null;
  }

  @Override
  protected Handler<Channel> childHandler(ContextInternal context, SocketAddress socketAddress, SSLHelper sslHelper) {
//    EventLoopContext connContext;
//    if (context instanceof EventLoopContext) {
//      connContext = (EventLoopContext) context;
//    } else {
//      connContext = vertx.createEventLoopContext(context.nettyEventLoop(), context.workerPool(), context.classLoader());
//    }
    return new SmppServerWorker((EventLoopContext) context, context::duplicate, this, vertx, sslHelper, options, handler);
  }

  @Override
  public Future<SmppServer> start(String host, int port) {
    return listen(port, host).map(this);
  }

  @Override
  public SmppServer onSessionCreated(Handler<SmppSession> sessionCreatedHandler) {
    handler.connectionHandler = sessionCreatedHandler;
    return this;
  }

  @Override
  public SmppServer onRequest(Handler<PduRequestContext<?>> pduRequestHandler) {
    handler.requestHandler = pduRequestHandler;
    return this;
  }
}
