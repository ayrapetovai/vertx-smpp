package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import io.vertx.core.Future;

public interface SmppSession {
//  SmppSessionBindType getBindType(); // TRANCEIVER, TRANSMITTER, RECEIVER, TRANCEIVER(smpp-v5?)
//  SmppSessionState getState(); // INITIAL, OPENED, BINDING, BOUND, UNBINDING, CLOSED
//  Set<SmppSession> getPool(); // Другие сессии того же клиента
//  String getId(); // id = systemId + bind_type + serial_number
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);

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
}
