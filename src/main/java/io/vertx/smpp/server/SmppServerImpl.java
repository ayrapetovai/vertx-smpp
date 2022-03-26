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
import io.vertx.smpp.session.ServerSessionConfigurator;
import io.vertx.smpp.util.core.CountDownLatch;
import io.netty.channel.Channel;
import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SmppServerImpl extends NetServerImpl implements Cloneable, SmppServer {
  private static final Logger log = LoggerFactory.getLogger(SmppServerImpl.class);

  private final Pool pool = new Pool();

  private Handler<ServerSessionConfigurator> configurator = x -> {};

  public SmppServerImpl(VertxInternal vertx, SmppServerOptions options) {
    super(vertx, options);
    connectHandler(sock -> {
    });
  }

  @Override
  protected Handler<Channel> childHandler(ContextInternal context, SocketAddress socketAddress, SSLHelper sslHelper) {
    EventLoopContext connContext;
    if (context instanceof EventLoopContext) {
      connContext = (EventLoopContext) context;
    } else {
      connContext = vertx.createEventLoopContext(context.nettyEventLoop(), context.workerPool(), context.classLoader());
    }
    return new SmppServerWorker(connContext, vertx, sslHelper, options, configurator, pool);
  }

  @Override
  public int actualPort() {
    return super.actualPort();
  }

  @Override
  public Future<SmppServer> start(String host, int port) {
    return listen(port, host).map(this);
  }

  @Override
  public Future<SmppServer> start() {
    return listen(options.getPort(), options.getHost()).map(this);
  }

  @Override
  public SmppServer configure(Handler<ServerSessionConfigurator> configurator) {
    this.configurator = configurator;
    return this;
  }

  @Override
  public void close(Promise<Void> completion) {
    log.debug("closing sessions {}", pool.size());
    var latch = new CountDownLatch(vertx, pool.size());
    pool.forEach(sess -> {
      var closeSessionPromise = vertx.<Void>promise();
      sess.close(closeSessionPromise);
      closeSessionPromise.future()
          .onComplete(ar -> {
            log.debug("{} close counted", sess);
            latch.countDown(1);
          });
    });
    latch.await(10, TimeUnit.SECONDS)
        .onComplete(nothing -> {
          log.debug("closing NetServer");
          super.close(completion);
        });
  }
}
