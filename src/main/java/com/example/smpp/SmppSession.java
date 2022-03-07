package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import io.vertx.core.Future;

public interface SmppSession {

//  Set<SmppSession> getPool(); // Другие сессии того же клиента
//  String getId(); // id = systemId + bind_type + serial_number

  /**
   * Запрос помещается в окно (очередь на отправку) и отправляется, котогда до него додет черед.
   * @param req
   * @param <T>
   * @return
   */
  <T extends PduResponse> Future<T>  send(PduRequest<T> req);

  /**
   * Немедленная отправка ответа без помещения в окно
   * @param pduResponse
   * @return
   */
  Future<Void> reply(PduResponse pduResponse);
}
