package com.example.smpp.client;

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

import com.example.smpp.SmppConstants;
import com.example.smpp.pdu.BaseBind;
import com.example.smpp.pdu.BaseBindResp;
import com.example.smpp.session.SmppSession;
import com.example.smpp.model.Pool;
import com.example.smpp.session.SmppSessionImpl;
import com.example.smpp.session.ClientSessionConfigurator;
import com.example.smpp.futures.BindFuture;
import com.example.smpp.types.SendBindRefusedException;
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

  private Handler<ClientSessionConfigurator> configurator = x -> {};
  private SmppSessionImpl lastOpenedSession;

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
    if (lastOpenedSession == null) {
      lastOpenedSession = worker.handle(pipeline.channel());
    } else {
      throw new IllegalStateException("last session was not handled");
    }
  }

  @Override
  public BindFuture<SmppSession> bind(int port) {
    return bind("localhost", port);
  }

  @Override
  public BindFuture<SmppSession> bind(String host, int port) {
    var sessionPromise = BindFuture.<SmppSession>promise(vertx.getOrCreateContext());
    connect(port, host) // Method `this.initChannel` is called inside `super.connect`, and fills lastOpenedSession field.
        .compose(socket -> {
          var session = lastOpenedSession;
          lastOpenedSession = null;
          BaseBind<? extends BaseBindResp> bindRequest = bindRequestByBindType(session);
          return session.send(bindRequest, session.getOptions().getBindTimeout())
              .onFailure(e -> {
                session.close(Promise.promise(), false);
                sessionPromise.tryFail(e); // TODO custom exception, and custom BindFuture.onSendFailed
              })
              .compose(bindResp -> {
                if (bindResp.getCommandStatus() == SmppConstants.STATUS_OK) {
                  var systemId = bindResp.getSystemId();
                  log.trace("bound to pear: {}", systemId);
                  session.setBoundToSystemId(systemId);
                  session.setState(sessionStateByBindType(session.getOptions().getBindType()));
                  session.setTargetInterfaceVersion(intVerFromTlv(bindResp));
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
    this.configurator = configurator;
    return this;
  }
}
