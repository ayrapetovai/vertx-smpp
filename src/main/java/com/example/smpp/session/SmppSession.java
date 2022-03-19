package com.example.smpp.session;

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

import com.example.smpp.futures.ReplayPduFuture;
import com.example.smpp.futures.SendPduFuture;
import com.example.smpp.pdu.PduRequest;
import com.example.smpp.pdu.PduResponse;
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
