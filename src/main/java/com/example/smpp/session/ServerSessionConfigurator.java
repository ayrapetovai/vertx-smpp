package com.example.smpp.session;

public interface ServerSessionConfigurator extends SessionOptionsView, SessionCallbacks {
  void setDropAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
  void isSendUnbindOnClose(boolean sendUnbindOnClose);
  void isAwaitUnbindResp(boolean awaitUnbindResp);
  void setBindTimeout(long bindTimeout);
  void setUnbindTimeout(long unbindTimeout);
  void setRequestExpiryTimeout(long requestExpiryTimeout);
  void setWindowSize(int windowSize);
  void setWindowWaitTimeout(long windowWaitTimeout);
  void setWindowMonitorInterval(long windowMonitorInterval);
  void setWriteTimeout(long writeTimeout);
  void setCountersEnabled(boolean countersEnabled);
  // TODO удалить logPdu
  void setLogPdu(boolean logPdu);
  void setLogBytes(boolean logBytes);
}