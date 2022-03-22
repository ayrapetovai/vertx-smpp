package io.vertx.smpp.session;

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

import io.vertx.smpp.types.PduRequestContext;
import io.vertx.smpp.types.PduResponseContext;
import io.vertx.smpp.types.BindInfo;
import io.vertx.core.Handler;

import java.util.function.Function;

// TODO
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);
public interface SessionCallbacks {
  void onCreated(Handler<SmppSession> createdHandler);
  void onRequest(Handler<PduRequestContext<?>> requestHandler);
  void onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler);
  void onClosed(Handler<SmppSession> closedHandler);
  void onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler);
  void onBindReceived(Function<BindInfo, Integer> onBindReceived);
  void onForbiddenRequest(Handler<PduRequestContext<?>> onForbiddenRequest);
  void onForbiddenResponse(Handler<PduResponseContext> onForbiddenResponse);
}
