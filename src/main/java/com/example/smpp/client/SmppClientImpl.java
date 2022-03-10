package com.example.smpp.client;

import com.cloudhopper.smpp.pdu.BindTransceiver;
import com.example.smpp.SmppSession;
import com.example.smpp.Pool;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.session.ClientSessionConfigurator;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetClientImpl;
import io.vertx.core.spi.metrics.TCPMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmppClientImpl extends NetClientImpl implements SmppClient {
  private static final Logger log = LoggerFactory.getLogger(SmppClientImpl.class);

  private final VertxInternal vertx;
  private final Pool pool = new Pool();
  private final SmppClientOptions options;

  private Handler<ClientSessionConfigurator> configurator;
  private SmppSessionImpl session;

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
    var worker = new SmppClientWorker(vertx.createEventLoopContext(), configurator, pool);
    session = worker.handle(pipeline.channel());
  }

  @Override
  public Future<SmppSession> bind(String host, int port) {
    var sessionPromise = Promise.<SmppSession>promise();
    return connect(port, host)
        .compose(socket -> {
          var bindRequest = new BindTransceiver();
          bindRequest.setSystemId(session.getOptions().getSystemId());
          return session.send(bindRequest, session.getOptions().getBindTimeout())
              .onFailure(e -> {
                session.close(Promise.promise(), false);
                sessionPromise.tryFail(e);
              })
              .compose(bindResp -> {
                // TODO if (bindResp.status)
                var systemId = bindResp.getSystemId(); // TODO systemId надо отдать коду пользователя
                log.trace("bound to client: {}", systemId);
                session.setBoundToSystemId(systemId);
                sessionPromise.complete(session);
                return sessionPromise.future();
              });
        });
  }

  @Override
  public SmppClient configure(Handler<ClientSessionConfigurator> configurator) {
    if (this.configurator == null) {
      this.configurator = configurator;
    } else {
      throw new IllegalStateException("configure once");
    }
    return this;
  }
}
