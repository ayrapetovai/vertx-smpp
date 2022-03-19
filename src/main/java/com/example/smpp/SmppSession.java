package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.session.SessionOptionsView;
import com.example.smpp.util.futures.ReplayPduFuture;
import com.example.smpp.util.futures.SendPduFuture;
import io.vertx.core.Closeable;
import io.vertx.core.Promise;
import io.vertx.core.spi.metrics.MetricsProvider;

public interface SmppSession extends Closeable, MetricsProvider {

  void setReferenceObject(Object object);
  <RefObj> RefObj getReferenceObject(Class<RefObj> clazz);

  /**
   * Network connection is established with a pear. But no bind_request was sent yet.
   * Intercommunication with PDU requests and responses is impossible in this state.
   * @return true session is opened, false session is either bound or unbound or closed.
   */
  boolean isOpened();
  boolean isBound();
  boolean isUnbound();
  boolean isClosed();
  boolean canSend(int commandId);
  boolean canReceive(int commandId);
//  SmppSessionSubState getSubState(); // SEND_PAUSED, REPLAY_PAUSED, SEND_REPLAY_PAUSED, PLAY.

  Long getId();

  /**
   * Id систем к которой создано сетевое подключение и прошел успешный bind
   * @return
   */
  String getBoundToSystemId();
  boolean areOptionalParametersSupported();
  /**
   * Запрос помещается в окно (очередь на отправку) и отправляется, котогда до него додет черед.
    * @param req
   * @param <T>
   * @return
   */
  <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req);

  <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req, long offerTimeout);

  /**
   * Немедленная отправка ответа без помещения в окно
   * @param pduResponse
   * @return
   */
  ReplayPduFuture<Void> reply(PduResponse pduResponse);

  // void pauseAll();
  // void pauseSend();
  // void pauseReply();
  // void failAllUnsentRequests();

  SessionOptionsView getOptions();
  void close(Promise<Void> completion, boolean sendUnbindRequired);
}
