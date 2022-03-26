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

import io.vertx.core.Handler;
import io.vertx.smpp.types.BindInfo;
import io.vertx.smpp.types.PduRequestContext;
import io.vertx.smpp.types.PduResponseContext;

import java.util.function.Function;

// TODO
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);
public interface ServerSessionConfigurator {
  ServerSessionConfigurator setSystemId(String systemId);
  ServerSessionConfigurator setDiscardAllOnUnbind(boolean dropAllOnUnbind);
  ServerSessionConfigurator setReplyToUnbind(boolean replyToUnbind);
  ServerSessionConfigurator setSendUnbindOnClose(boolean sendUnbindOnClose);
  ServerSessionConfigurator setAwaitUnbindResp(boolean awaitUnbindResp);
  ServerSessionConfigurator setBindTimeout(long bindTimeout);
  ServerSessionConfigurator setUnbindTimeout(long unbindTimeout);
  ServerSessionConfigurator setRequestExpiryTimeout(long requestExpiryTimeout);
  ServerSessionConfigurator setWindowSize(int windowSize);
  ServerSessionConfigurator setWindowWaitTimeout(long windowWaitTimeout);
  ServerSessionConfigurator setWindowMonitorInterval(long windowMonitorInterval);
  ServerSessionConfigurator setWriteTimeout(long writeTimeout);
  ServerSessionConfigurator setWriteQueueSize(int writeQueueSize);
  ServerSessionConfigurator setOverflowMonitorInterval(long overflowMonitorInterval);
  ServerSessionConfigurator setCountersEnabled(boolean countersEnabled);
  ServerSessionConfigurator setLogPduBody(boolean logBytes);
  ServerSessionConfigurator onCreated(Handler<SmppSession> createdHandler);
  ServerSessionConfigurator onRequest(Handler<PduRequestContext<?>> requestHandler);
  ServerSessionConfigurator onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler);
  ServerSessionConfigurator onClosed(Handler<SmppSession> closedHandler);
  ServerSessionConfigurator onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler);
  ServerSessionConfigurator onBindReceived(Function<BindInfo, Integer> onBindReceived);
  ServerSessionConfigurator onForbiddenRequest(Handler<PduRequestContext<?>> onForbiddenRequest);
  ServerSessionConfigurator onForbiddenResponse(Handler<PduResponseContext> onForbiddenResponse);
  ServerSessionConfigurator onOverflowed(Handler<Void> onOverflowed);
  ServerSessionConfigurator onDrained(Handler<Void> onDrained);
}