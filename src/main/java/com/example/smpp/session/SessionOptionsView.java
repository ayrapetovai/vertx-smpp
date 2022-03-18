package com.example.smpp.session;

import com.example.smpp.model.SmppBindType;

public interface SessionOptionsView {
  SmppBindType getBindType();
  String getSystemId();
  String getPassword();
  String getSystemType();
  String getAddressRange(); // String -> ImmutableAddress

  boolean getReplyToUnbind();
  boolean isDiscardAllOnUnbind();
  boolean isSendUnbindOnClose();
  boolean isAwaitUnbindResp();
  long getDiscardTimeout();
  long getBindTimeout();
  long getUnbindTimeout();
  long getRequestExpiryTimeout();
  int getWindowSize();
  long getWindowWaitTimeout();
  long getWindowMonitorInterval();
  long getWriteTimeout();
  boolean getCountersEnabled();
  boolean getLogPduBody();

  void setDiscardAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
  void setSendUnbindOnClose(boolean sendUnbindOnClose);
  void setAwaitUnbindResp(boolean awaitUnbindResp);
  void setDiscardTimeout(long drainTimeout);
//  void setBindTimeout(long bindTimeout); // нельзя изменить после подключения, бинд уже прошел
  void setUnbindTimeout(long unbindTimeout);
  void setRequestExpiryTimeout(long requestExpiryTimeout);
//  void setWindowSize(int windowSize); // нельзя изменить после подключения, значение уже передано симафор
  void setWindowWaitTimeout(long windowWaitTimeout);
  void setWindowMonitorInterval(long windowMonitorInterval);
//  void setWriteTimeout(long writeTimeout); // нельзя изменить после подключения, значение уже передано в слушатель канала
  void setCountersEnabled(boolean countersEnabled);
  void setLogPduBody(boolean logPduBody);

}