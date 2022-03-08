package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import io.vertx.core.Closeable;
import io.vertx.core.Future;

public interface SmppSession extends Closeable {
//  SmppSessionBindType getBindType(); // TRANCEIVER, TRANSMITTER, RECEIVER, TRANCEIVER(smpp-v5?)
//  SmppSessionState getState(); // INITIAL?, OUTBOUND, OPENED, BINDING, BOUND, UNBINDING, CLOSED
//  [?] SessionState {SmppState state, boolean sendPaused, boolean replyPaused }
//  SmppSessionSubState getSubState(); // SEND_PAUSED, REPLAY_PAUSED, SEND_REPLAY_PAUSED, PLAY.
//  String getId(); // id = systemId + bind_type + serial_number
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);

  Long getId();

  /**
   * Запрос помещается в окно (очередь на отправку) и отправляется, котогда до него додет черед.
   * TODO возвращенный future должен получать не PduResponse, а обвертку в которой будет инфа
   *  о том, какая именно произошла ошибка: запись в канал, ошибка формата, запрос протух в окне и т. п.
   * @param req
   * @param <T>
   * @return
   */
  <T extends PduResponse> Future<T>  send(PduRequest<T> req);

  /**
   * TODO возвращенный future должен получать не Void, а обвертку в которой будет инфа
   *  о том, какая именно произошла ошибка: запись в канал, ошибка формата, запись в канал протухла и т. п.
   * Немедленная отправка ответа без помещения в окно
   * @param pduResponse
   * @return
   */
  Future<Void> reply(PduResponse pduResponse);

  // void pauseAll();
  // void pauseSend();
  // void pauseReply();
  // void failAllUnsentRequests();
}
