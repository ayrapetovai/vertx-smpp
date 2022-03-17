package com.example.smpp.client;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import com.example.smpp.SmppSession;
import com.example.smpp.Pool;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.session.ClientSessionConfigurator;
import com.example.smpp.util.futures.BindFuture;
import com.example.smpp.util.SendBindRefusedException;
import io.netty.channel.ChannelPipeline;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.impl.NetClientImpl;
import io.vertx.core.spi.metrics.TCPMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.example.smpp.util.Helper.*;

public class SmppClientImpl extends NetClientImpl implements SmppClient {
  private static final Logger log = LoggerFactory.getLogger(SmppClientImpl.class);

  private final VertxInternal vertx;
  private final Pool pool = new Pool();
  private final SmppClientOptions options;

  private Handler<ClientSessionConfigurator> configurator;
  private SmppSessionImpl session; // -> lastOpenedSession

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
  public BindFuture<SmppSession> bind(String host, int port) {
    var sessionPromise = BindFuture.<SmppSession>promise(vertx.getOrCreateContext());
    connect(port, host)
        .compose(socket -> {
          BaseBind<? extends BaseBindResp> bindRequest = bindRequesstByBinType(session.getOptions().getBindType());
          bindRequest.setSystemId(session.getOptions().getSystemId());
          return session.send(bindRequest, session.getOptions().getBindTimeout())
              .onFailure(e -> {
                session.close(Promise.promise(), false);
                sessionPromise.tryFail(e);
              })
              .compose(bindResp -> {
                if (bindResp.getCommandStatus() == SmppConstants.STATUS_OK) {
                  var systemId = bindResp.getSystemId();
                  log.trace("bound to pear: {}", systemId);
                  session.setBoundToSystemId(systemId);
                  session.setState(sessionStateByBindType(session.getOptions().getBindType()));
                  session.setTargetInterface(intVerFromTlv(bindResp));
                  sessionPromise.complete(session);
                } else {
                  var closePromise = Promise.<Void>promise();
                  var bindStatus = bindResp.getCommandStatus();
                  closePromise.future()
                      .onComplete(nothing ->
                              sessionPromise.fail(new SendBindRefusedException("did not receive bind acknowledge, status=" + bindStatus, bindStatus))
                      );
                  session.close(closePromise, false);
                }
                return sessionPromise.future();
              });
        });
    return sessionPromise.future();
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
