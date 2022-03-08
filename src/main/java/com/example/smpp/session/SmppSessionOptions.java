package com.example.smpp.session;

import com.example.smpp.model.SmppBindType;

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
public class SmppSessionOptions implements ServerSessionConfigurator, ClientSessionConfigurator, SessionOptionsView {

  private Long id;
  private SmppBindType bindType;
  private String systemId;
  private String password;
  private String systemType;
  private String addressRange;  // per session: ton, npi, address

  private boolean dropAllOnUnbind;
  private boolean replyToUnbind;
  private long bindTimeout;
  private long unbindTimeout;
  private long requestExpiryTimeout;
  private int windowSize;
  private long windowWaitTimeout;
  private long windowMonitorInterval;
  private long writeTimeout;
  private boolean countersEnabled;
  private boolean logPdu;
  private boolean logBytes;

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
}
