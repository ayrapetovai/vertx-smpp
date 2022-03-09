package com.example.smpp.session;

import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.PduRequestContext;
import com.example.smpp.SmppSession;
import com.example.smpp.model.SmppBindType;
import io.vertx.core.Handler;

// blueprint
//+S  private Long id; // per session
//+S  private SmppBindType type;  // per session
//+S  private String systemId;  // per session
//+S  private String password;  // per session
//+S  private String systemType;  // per session
//+S  private Address addressRange;  // per session: ton, npi, address

// R  boolean dropAllOnUnbind = false; // per session? При получении unbind, не досылать resp и другие pdu, сразу убить сессию
// R  boolean replyToUnbind = true; //per session? В ответ на unbind посылать unbind_resp
// R  long bindTimeout; // per session? Время, за которое сервер выдаст bind_resp
// R  long unbindTimeout; // per session? Время, которе дается клиенту на отправку unbind_resp, по истечении все pdu в окне дропаются и соединение закрывается
// R  private long requestExpiryTimeout; // per session
// R  private int windowSize; // per session
// R  private long windowWaitTimeout; // per session
// R  private long windowMonitorInterval;// per session (request expire check interval)
// R  private long writeTimeout; // per session
// R  private boolean countersEnabled; // per session (metrics enabled)
// R  private LoggingOptions loggingOptions; // per session: log_pdu, log_bytes or not
public class SmppSessionOptions implements ServerSessionConfigurator, ClientSessionConfigurator {

  private Long id;
  private SmppBindType bindType = SmppBindType.TRANSCEIVER;
  private String systemId;
  private String password;
  private String systemType;
  private String addressRange;  // per session: ton, npi, address

  private boolean dropAllOnUnbind = false;
  private boolean replyToUnbind = true;
  private boolean sendUnbindOnClose = true;
  private boolean awaitUnbindResp = true;
  private long bindTimeout = 10000;
  private long unbindTimeout = 10000;
  private long requestExpiryTimeout = 10000;
  private int windowSize = 50;
  private long windowWaitTimeout = 5000;
  private long windowMonitorInterval = 10;
  private long writeTimeout = 2000;
  private boolean countersEnabled = false;
  private boolean logPdu = false;
  private boolean logBytes = false;

  // TODO: check on null or call this stubs, what is more performant?
  Handler<SmppSession> createdHandler = __ -> {};
  Handler<PduRequestContext<?>> requestHandler = __ -> {};
  Handler<PduResponse> unexpectedResponseHandler = __ -> {};
  Handler<SmppSession> closeHandler = __ -> {};
  Handler<SmppSession> unexpectedCloseHandler = __ -> {};

  @Override
  public void setId(Long id) {

  }

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
  public void setAddressRange(String addressRange) {
    this.addressRange = addressRange;
  }

  @Override
  public void setDropAllOnUnbind(boolean dropAllOnUnbind) {
    this.dropAllOnUnbind = dropAllOnUnbind;
  }

  @Override
  public void setReplyToUnbind(boolean replyToUnbind) {
    this.replyToUnbind = replyToUnbind;
  }

  @Override
  public void isSendUnbindOnClose(boolean sendUnbindOnClose) {
    this.sendUnbindOnClose = sendUnbindOnClose;
  }

  @Override
  public void isAwaitUnbindResp(boolean awaitUnbindResp) {
    this.awaitUnbindResp = awaitUnbindResp;
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
  public void setLogPdu(boolean logPdu) {
    this.logPdu = logPdu;
  }

  @Override
  public void setLogBytes(boolean logBytes) {
    this.logBytes = logBytes;
  }

  @Override
  public Long getId() {
    return id;
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
  public String getAddressRange() {
    return addressRange;
  }

  @Override
  public boolean getReplyToUnbind() {
    return replyToUnbind;
  }

  @Override
  public boolean getDropAllOnUnbind() {
    return dropAllOnUnbind;
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
  public boolean getLogPdu() {
    return logPdu;
  }

  @Override
  public boolean getLogBytes() {
    return logBytes;
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
  public void onUnexpectedResponse(Handler<PduResponse> unexpectedResponseHandler) {
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
  public Handler<SmppSession> getOnCreated() {
    return createdHandler;
  }

  @Override
  public Handler<PduRequestContext<?>> getOnRequest() {
    return requestHandler;
  }

  @Override
  public Handler<PduResponse> getOnUnexpectedResponse() {
    return unexpectedResponseHandler;
  }

  @Override
  public Handler<SmppSession> getOnClose() {
    return closeHandler;
  }

  @Override
  public Handler<SmppSession> getOnUnexpectedClose() {
    return unexpectedCloseHandler;
  }
}
