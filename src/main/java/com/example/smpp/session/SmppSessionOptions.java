package com.example.smpp.session;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.type.Address;
import com.example.smpp.types.PduRequestContext;
import com.example.smpp.types.PduResponseContext;
import com.example.smpp.types.BindInfo;
import com.example.smpp.model.SmppBindType;
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
  private long windowWaitTimeout = 5000;
  private long windowMonitorInterval = 10;
  private long writeTimeout = 0;
  private boolean countersEnabled = false;
  private boolean logPduBody = false;

  Handler<SmppSession> createdHandler = __ -> {};
  Handler<PduRequestContext<?>> requestHandler = __ -> {};
  Handler<PduResponseContext> unexpectedResponseHandler = __ -> {};
  Handler<SmppSession> closeHandler = __ -> {};
  Handler<SmppSession> unexpectedCloseHandler = __ -> {};
  // TODO Function<BindInfo, BindRespStatusCode> onBindReceived
  Function<BindInfo, Integer> onBindReceived = __ -> SmppConstants.STATUS_OK;
  Handler<PduRequestContext<?>> onForbiddenRequest = __ -> {};
  Handler<PduResponseContext> onForbiddenResponse = __ -> {};

  @Override
  public void setBindType(SmppBindType bindType) {
    this.bindType = bindType;
  }

  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  @Override
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public void setSystemType(String systemType) {
    this.systemType = systemType;
  }

  @Override
  public void setAddressRange(Address addressRange) {
    this.addressRange = addressRange;
  }

  @Override
  public void setDiscardAllOnUnbind(boolean discardAllOnUnbind) {
    this.discardAllOnUnbind = discardAllOnUnbind;
  }

  @Override
  public void setReplyToUnbind(boolean replyToUnbind) {
    this.replyToUnbind = replyToUnbind;
  }

  @Override
  public void setSendUnbindOnClose(boolean sendUnbindOnClose) {
    this.sendUnbindOnClose = sendUnbindOnClose;
  }

  @Override
  public void setAwaitUnbindResp(boolean awaitUnbindResp) {
    this.awaitUnbindResp = awaitUnbindResp;
  }

  @Override
  public void setDiscardTimeout(long discardTimeout) {
    this.discardTimeout = discardTimeout;
  }

  @Override
  public void setBindTimeout(long bindTimeout) {
    this.bindTimeout = bindTimeout;
  }

  @Override
  public void setUnbindTimeout(long unbindTimeout) {
    this.unbindTimeout = unbindTimeout;
  }

  @Override
  public void setRequestExpiryTimeout(long requestExpiryTimeout) {
    this.requestExpiryTimeout = requestExpiryTimeout;
  }

  @Override
  public void setWindowSize(int windowSize) {
    this.windowSize = windowSize;
  }

  @Override
  public void setWindowWaitTimeout(long windowWaitTimeout) {
    this.windowWaitTimeout = windowWaitTimeout;
  }

  @Override
  public void setWindowMonitorInterval(long windowMonitorInterval) {
    this.windowMonitorInterval = windowMonitorInterval;
  }

  @Override
  public void setWriteTimeout(long writeTimeout) {
    this.writeTimeout = writeTimeout;
  }

  @Override
  public void setCountersEnabled(boolean countersEnabled) {
    this.countersEnabled = countersEnabled;
  }

  @Override
  public void setLogPduBody(boolean logPduBody) {
    this.logPduBody = logPduBody;
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
  public boolean getCountersEnabled() {
    return countersEnabled;
  }

  @Override
  public boolean getLogPduBody() {
    return logPduBody;
  }

  @Override
  public void onCreated(Handler<SmppSession> createdHandler) {
    this.createdHandler = createdHandler;
  }

  @Override
  public void onRequest(Handler<PduRequestContext<?>> requestHandler) {
    this.requestHandler = requestHandler;
  }

  @Override
  public void onUnexpectedResponse(Handler<PduResponseContext> unexpectedResponseHandler) {
    this.unexpectedResponseHandler = unexpectedResponseHandler;
  }

  @Override
  public void onClose(Handler<SmppSession> closeHandler) {
    this.closeHandler = closeHandler;
  }

  @Override
  public void onUnexpectedClose(Handler<SmppSession> unexpectedCloseHandler) {
    this.unexpectedCloseHandler = unexpectedCloseHandler;
  }

  @Override
  public void onBindReceived(Function<BindInfo, Integer> onBindReceived) {
    this.onBindReceived = onBindReceived;
  }

  @Override
  public void onForbiddenRequest(Handler<PduRequestContext<?>> onForbiddenRequest) {
    this.onForbiddenRequest = onForbiddenRequest;
  }

  @Override
  public void onForbiddenResponse(Handler<PduResponseContext> onForbiddenResponse) {
    this.onForbiddenResponse = onForbiddenResponse;
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
    return closeHandler;
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
}
