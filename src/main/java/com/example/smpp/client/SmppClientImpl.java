package com.example.smpp.client;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.tlv.TlvConvertException;
import com.example.smpp.SmppSession;
import com.example.smpp.Pool;
import com.example.smpp.SmppSessionImpl;
import com.example.smpp.model.SmppSessionState;
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
  public Future<SmppSession> bind(String host, int port) {
    var sessionPromise = Promise.<SmppSession>promise();
    return connect(port, host)
        .compose(socket -> {
          BaseBind<? extends BaseBindResp> bindRequest = null;
          switch (session.getOptions().getBindType()) {
            case TRANSMITTER:
              bindRequest = new BindTransmitter(); break;
            case RECEIVER:
              bindRequest = new BindReceiver(); break;
            case TRANSCEIVER:
              bindRequest = new BindTransceiver();
          }
          bindRequest.setSystemId(session.getOptions().getSystemId());
          return session.send(bindRequest, session.getOptions().getBindTimeout())
              .onFailure(e -> {
                session.close(Promise.promise(), false);
                sessionPromise.tryFail(e);
              })
              .compose(bindResp -> {
                if (bindResp.getCommandStatus() == SmppConstants.STATUS_OK) {
                  var systemId = bindResp.getSystemId(); // TODO systemId надо отдать коду пользователя
                  log.trace("bound to client: {}", systemId);
                  session.setBoundToSystemId(systemId);
                  switch (session.getOptions().getBindType()) {
                    case TRANSMITTER:
                      session.setState(SmppSessionState.BOUND_TX); break;
                    case RECEIVER:
                      session.setState(SmppSessionState.BOUND_RX); break;
                    case TRANSCEIVER:
                      session.setState(SmppSessionState.BOUND_TRX);
                  }
                  var intVer = SmppConstants.VERSION_3_3;
                  Tlv scInterfaceVersion = bindResp.getOptionalParameter(SmppConstants.TAG_SC_INTERFACE_VERSION);
                  if (scInterfaceVersion != null) {
                    try {
                      byte tempInterfaceVersion = scInterfaceVersion.getValueAsByte();
                      if (tempInterfaceVersion >= SmppConstants.VERSION_3_4) {
                        intVer = SmppConstants.VERSION_3_4;
                      } else {
                        intVer = SmppConstants.VERSION_3_3;
                      }
                    } catch (TlvConvertException e) {
                      log.warn("Unable to convert sc_interface_version to a byte value: {}", e.getMessage());
                      intVer = SmppConstants.VERSION_3_3;
                    }
                  }
                  session.setTargetInterface(intVer);
                  sessionPromise.complete(session);
                } else {
                  var closePromise = Promise.<Void>promise();
                  closePromise.future()
                          .onComplete(nothing -> sessionPromise.fail("did not receive bind acknowledge"));
                  session.close(closePromise, false);
                }
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
