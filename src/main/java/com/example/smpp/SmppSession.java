package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.model.SmppSessionState;
import com.example.smpp.session.SessionOptionsView;
import com.example.smpp.util.futures.ReplayPduFuture;
import com.example.smpp.util.futures.SendPduFuture;
import io.vertx.core.Closeable;
import io.vertx.core.Promise;
import io.vertx.core.spi.metrics.MetricsProvider;

public interface SmppSession extends Closeable, MetricsProvider {
  SmppSessionState getState();
//  SmppSessionSubState getSubState(); // SEND_PAUSED, REPLAY_PAUSED, SEND_REPLAY_PAUSED, PLAY.

  Long getId();

  /**
   * Id систем к которой создано сетевое подключение и прошел успешный bind
   * @return
   */
  String getBoundToSystemId();

  /**
   * Запрос помещается в окно (очередь на отправку) и отправляется, котогда до него додет черед.
   * TODO возвращенный future должен получать не PduResponse, а обвертку в которой будет инфа
   *  о том, какая именно произошла ошибка: запись в канал, ошибка формата, запрос протух в окне и т. п.
   * @param req
   * @param <T>
   * @return
   */
  <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req);

  <T extends PduResponse> SendPduFuture<T> send(PduRequest<T> req, long offerTimeout);

  /**
   * TODO возвращенный future должен получать не Void, а обвертку в которой будет инфа
   *  о том, какая именно произошла ошибка: запись в канал, ошибка формата, запись в канал протухла и т. п.
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
