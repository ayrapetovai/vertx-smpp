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

import io.vertx.smpp.SmppConstants;
import io.vertx.smpp.types.Address;
import io.vertx.smpp.types.PduRequestContext;
import io.vertx.smpp.types.PduResponseContext;
import io.vertx.smpp.types.BindInfo;
import io.vertx.smpp.model.SmppBindType;
import io.vertx.core.Handler;

import java.util.function.Function;

public class SmppSessionOptions implements ServerSessionConfigurator, ClientSessionConfigurator, SessionOptionsView {

  private SmppBindType bindType = SmppBindType.TRANSCEIVER;
  private String systemId;
  private String password;
  private String systemType;
  private Address addressRange;

  private boolean discardAllOnUnbind = false;
  private boolean replyToUnbind = true;
  private boolean sendUnbindOnClose = true;
  private boolean awaitUnbindResp = true;
  private long discardTimeout = 10000;
  private long bindTimeout = 10000;
  private long unbindTimeout = 10000;
  private long requestExpiryTimeout = 10000;
  private int windowSize = 50;
  private long windowWaitTimeout = 10000;
  private long windowMonitorInterval = 10;
  private long writeTimeout = 0;
  private int writeQueueSize = 0;
  private long overflowMonitorInterval = 10;
  private boolean countersEnabled = false;
  private boolean logPduBody = false;

  private Handler<SmppSession> createdHandler = __ -> {};
  private Handler<PduRequestContext<?>> requestHandler = __ -> {};
  private Handler<PduResponseContext> unexpectedResponseHandler = __ -> {};
  private Handler<SmppSession> closedHandler = __ -> {};
  private Handler<SmppSession> unexpectedCloseHandler = __ -> {};
  // TODO Function<BindInfo, BindRespStatusCode> onBindReceived
  private Function<BindInfo, Integer> onBindReceived = __ -> SmppConstants.STATUS_OK;
  private Handler<PduRequestContext<?>> onForbiddenRequest = __ -> {};
  private Handler<PduResponseContext> onForbiddenResponse = __ -> {};
  private Handler<Void> onOverflowed = __ -> {};
  private Handler<Void> onDrained = __ -> {};

  @Override
  public SmppSessionOptions setBindType(SmppBindType bindType) {
    this.bindType = bindType;
    return this;
  }

  @Override
  public SmppSessionOptions setSystemId(String systemId) {
    this.systemId = systemId;
    return this;
  }

  @Override
  public SmppSessionOptions setPassword(String password) {
    this.password = password;
    return this;
  }

  @Override
  public SmppSessionOptions setSystemType(String systemType) {
    this.systemType = systemType;
    return this;
  }

  @Override
  public SmppSessionOptions setAddressRange(Address addressRange) {
    this.addressRange = addressRange;
    return this;
  }

  @Override
  public SmppSessionOptions setDiscardAllOnUnbind(boolean discardAllOnUnbind) {
    this.discardAllOnUnbind = discardAllOnUnbind;
    return this;
  }

  @Override
  public SmppSessionOptions setReplyToUnbind(boolean replyToUnbind) {
    this.replyToUnbind = replyToUnbind;
    return this;
  }

  @Override
  public SmppSessionOptions setSendUnbindOnClose(boolean sendUnbindOnClose) {
    this.sendUnbindOnClose = sendUnbindOnClose;
    return this;
  }

  @Override
  public SmppSessionOptions setAwaitUnbindResp(boolean awaitUnbindResp) {
    this.awaitUnbindResp = awaitUnbindResp;
    return this;
  }

  @Override
  public SmppSessionOptions setDiscardTimeout(long discardTimeout) {
    this.discardTimeout = discardTimeout;
    return this;
  }

  @Override
  public SmppSessionOptions setBindTimeout(long bindTimeout) {
    this.bindTimeout = bindTimeout;
    return this;
  }

  @Override
  public SmppSessionOptions setUnbindTimeout(long unbindTimeout) {
    this.unbindTimeout = unbindTimeout;
    return this;
  }

  @Override
  public SmppSessionOptions setRequestExpiryTimeout(long requestExpiryTimeout) {
    this.requestExpiryTimeout = requestExpiryTimeout;
    return this;
  }

  @Override
  public SmppSessionOptions setWindowSize(int windowSize) {
    this.windowSize = windowSize;
    return this;
  }

  @Override
  public SmppSessionOptions setWindowWaitTimeout(long windowWaitTimeout) {
    this.windowWaitTimeout = windowWaitTimeout;
    return this;
  }

  @Override
  public SmppSessionOptions setWindowMonitorInterval(long windowMonitorInterval) {
    this.windowMonitorInterval = windowMonitorInterval;
    return this;
  }

  @Override
  public SmppSessionOptions setWriteQueueSize(int writeQueueSize) {
    this.writeQueueSize = writeQueueSize;
    return this;
  }

  @Override
  public SmppSessionOptions setOverflowMonitorInterval(long overflowMonitorInterval) {
    this.overflowMonitorInterval = overflowMonitorInterval;
    return this;
  }

  @Override
  public SmppSessionOptions setWriteTimeout(long writeTimeout) {
    this.writeTimeout = writeTimeout;
    return this;
  }

  @Override
  public SmppSessionOptions setCountersEnabled(boolean countersEnabled) {
    this.countersEnabled = countersEnabled;
    return this;
  }

  @Override
  public SmppSessionOptions setLogPduBody(boolean logPduBody) {
    this.logPduBody = logPduBody;
    return this;
  }

  @Override
  public SmppBindType getBindType() {
    return bindType;
  }

  @Override
  public String getSystemId() {
    return systemId;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getSystemType() {
    return systemType;
  }

  @Override
  public Address getAddressRange() {
    return addressRange;
  }

  @Override
  public boolean getReplyToUnbind() {
    return replyToUnbind;
  }

  @Override
  public boolean isDiscardAllOnUnbind() {
    return discardAllOnUnbind;
  }

  @Override
  public boolean isSendUnbindOnClose() {
    return sendUnbindOnClose;
  }

  @Override
  public boolean isAwaitUnbindResp() {
    return awaitUnbindResp;
  }

  @Override
  public long getDiscardTimeout() {
    return discardTimeout;
  }

  @Override
  public long getBindTimeout() {
    return bindTimeout;
  }

  @Override
  public long getUnbindTimeout() {
    return unbindTimeout;
  }

  @Override
  public long getRequestExpiryTimeout() {
    return requestExpiryTimeout;
  }

  @Override
  public int getWindowSize() {
    return windowSize;
  }

  @Override
  public long getWindowWaitTimeout() {
    return windowWaitTimeout;
  }

  @Override
  public long getWindowMonitorInterval() {
    return windowMonitorInterval;
  }

  @Override
  public long getWriteTimeout() {
    return writeTimeout;
  }

  @Override
  public int getWriteQueueSize() {
    return this.writeQueueSize;
  }

  @Override
  public long getOverflowMonitorInterval() {
    return this.overflowMonitorInterval;
  }

  @Override
  public boolean getCountersEnabled() {
    return countersEnabled;
  }

  @Override
  public boolean getLogPduBody() {
    return logPduBody;
  }

  @Override
  public SmppSessionOptions onCreated(Handler<SmppSession> createdHandler) {
    this.createdHandler = createdHandler;
    return this;
  }

  @Override
  public SmppSessionOptions onRequest(Handler<PduRequestContext<?>> requestHandler) {
    this.requestHandler = requestHandler;
    return this;
  }

  @Override
  public SmppSessionOptions onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler) {
    this.unexpectedResponseHandler = unexpectedResponseHandler;
    return this;
  }

  @Override
  public SmppSessionOptions onClosed(Handler<SmppSession> closedHandler) {
    this.closedHandler = closedHandler;
    return this;
  }

  @Override
  public SmppSessionOptions onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler) {
    this.unexpectedCloseHandler = unexpectedCloseHandler;
    return this;
  }

  @Override
  public SmppSessionOptions onBindReceived(Function<BindInfo, Integer> onBindReceived) {
    this.onBindReceived = onBindReceived;
    return this;
  }

  @Override
  public SmppSessionOptions onForbiddenRequest(Handler<PduRequestContext<?>> onForbiddenRequest) {
    this.onForbiddenRequest = onForbiddenRequest;
    return this;
  }

  @Override
  public SmppSessionOptions onForbiddenResponse(Handler<PduResponseContext> onForbiddenResponse) {
    this.onForbiddenResponse = onForbiddenResponse;
    return this;
  }

  @Override
  public SmppSessionOptions onOverflowed(Handler<Void> onOverflowed) {
    this.onOverflowed = onOverflowed;
    return this;
  }

  @Override
  public SmppSessionOptions onDrained(Handler<Void> onDrained) {
    this.onDrained = onDrained;
    return this;
  }

  public Handler<SmppSession> getOnCreated() {
    return createdHandler;
  }

  public Handler<PduRequestContext<?>> getOnRequest() {
    return requestHandler;
  }

  public Handler<PduResponseContext> getOnUnexpectedResponse() {
    return unexpectedResponseHandler;
  }

  public Handler<SmppSession> getOnClose() {
    return closedHandler;
  }

  public Handler<SmppSession> getOnUnexpectedClose() {
    return unexpectedCloseHandler;
  }

  public Function<BindInfo, Integer> getOnBindReceived() {
    return this.onBindReceived;
  }

  public Handler<PduRequestContext<?>> getOnForbiddenRequest() {
    return this.onForbiddenRequest;
  }

  public Handler<PduResponseContext> getOnForbiddenResponse() {
    return this.onForbiddenResponse;
  }

  public Handler<Void> getOnOverflowed() {
    return onOverflowed;
  }

  public Handler<Void> getOnDrained() {
    return onDrained;
  }
}
