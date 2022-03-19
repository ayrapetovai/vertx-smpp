package com.example.smpp.server;

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

import com.example.smpp.model.Pool;
import com.example.smpp.session.ServerSessionConfigurator;
import io.netty.channel.Channel;
import io.vertx.core.*;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

// FIXME implement clone()
public class SmppServerImpl extends NetServerImpl implements Cloneable, SmppServer {
  private static final Logger log = LoggerFactory.getLogger(SmppServerImpl.class);

  private final Pool pool = new Pool();

  private Handler<ServerSessionConfigurator> configurator;

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
  public Future<SmppServer> start(String host, int port) {
    return listen(port, host).map(this);
  }

  @Override
  public SmppServer configure(Handler<ServerSessionConfigurator> configurator) {
    if (this.configurator == null) {
      this.configurator = configurator;
    } else {
      throw new IllegalStateException("only one configuration");
    }
    return this;
  }

  @Override
  public void close(Promise<Void> completion) {
    log.debug("closing sessions {}", pool.size());
    var closeSessionFutures = new ArrayList<Future>(pool.size());
    pool.forEach(sess -> {
      var closeSessionPromise = vertx.<Void>promise();
      sess.close(closeSessionPromise);
      closeSessionFutures.add(closeSessionPromise.future());
    });

    CompositeFuture.all(closeSessionFutures)
        .onComplete(ar -> {
          log.debug("closing NetServer");
          super.close(completion);
        });
  }

  //  public void stop(Future<Void> future) throws Exception {
  //    // In current design, the publisher is responsible for removing the service
  //    List<Future> futures = new ArrayList<>();
  //    registeredRecords.forEach(record -> {
  //      Future<Void> cleanupFuture = Future.future();
  //      futures.add(cleanupFuture);
  //      discovery.unpublish(record.getRegistration(), cleanupFuture.completer());
  //    });
  //
  //    if (futures.isEmpty()) {
  //      discovery.close();
  //      future.complete();
  //    } else {
  //      CompositeFuture.all(futures)
  //        .setHandler(ar -> {
  //          discovery.close();
  //          if (ar.failed()) {
  //            future.fail(ar.cause());
  //          } else {
  //            future.complete();
  //          }
  //        });
  //    }
}
