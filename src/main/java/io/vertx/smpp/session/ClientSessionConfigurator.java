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
import io.vertx.smpp.model.SmppBindType;
import io.vertx.smpp.types.Address;
import io.vertx.smpp.types.BindInfo;
import io.vertx.smpp.types.PduRequestContext;
import io.vertx.smpp.types.PduResponseContext;

import java.util.function.Function;

// TODO
//  public boolean sniffInbound(Pdu pdu);
//  public boolean sniffOutbound(Pdu pdu);
public interface ClientSessionConfigurator {
  ClientSessionConfigurator setBindType(SmppBindType bindType);
  ClientSessionConfigurator setSystemId(String systemId);
  ClientSessionConfigurator setPassword(String password);
  ClientSessionConfigurator setSystemType(String systemType);
  ClientSessionConfigurator setAddressRange(Address addressRange);

  ClientSessionConfigurator setDiscardAllOnUnbind(boolean dropAllOnUnbind);
  ClientSessionConfigurator setReplyToUnbind(boolean replyToUnbind);
  ClientSessionConfigurator setBindTimeout(long bindTimeout);
  ClientSessionConfigurator setUnbindTimeout(long unbindTimeout);
  ClientSessionConfigurator setRequestExpiryTimeout(long requestExpiryTimeout);
  ClientSessionConfigurator setWindowSize(int windowSize);
  ClientSessionConfigurator setWindowWaitTimeout(long windowWaitTimeout);
  ClientSessionConfigurator setWindowMonitorInterval(long windowMonitorInterval);
  ClientSessionConfigurator setWriteTimeout(long writeTimeout);
  ClientSessionConfigurator setWriteQueueSize(int writeQueueSize);
  ClientSessionConfigurator setOverflowMonitorInterval(long overflowMonitorInterval);
  ClientSessionConfigurator setCountersEnabled(boolean countersEnabled);
  ClientSessionConfigurator setLogPduBody(boolean logBytes);

  ClientSessionConfigurator onCreated(Handler<SmppSession> createdHandler);
  ClientSessionConfigurator onRequest(Handler<PduRequestContext<?>> requestHandler);
  ClientSessionConfigurator onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler);
  ClientSessionConfigurator onClosed(Handler<SmppSession> closedHandler);
  ClientSessionConfigurator onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler);
  ClientSessionConfigurator onBindReceived(Function<BindInfo, Integer> onBindReceived);
  ClientSessionConfigurator onForbiddenRequest(Handler<PduRequestContext<?>> onForbiddenRequest);
  ClientSessionConfigurator onForbiddenResponse(Handler<PduResponseContext> onForbiddenResponse);
  ClientSessionConfigurator onOverflowed(Handler<Void> onOverflowed);
  ClientSessionConfigurator onDrained(Handler<Void> onDrained);
}