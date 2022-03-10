package com.example.smpp.session;

public interface ServerSessionConfigurator extends SessionOptionsView, SessionCallbacks {
  void setSystemId(String systemId);
  void setDropAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
  void isSendUnbindOnClose(boolean sendUnbindOnClose); //TODO 'is' in setter?
  void isAwaitUnbindResp(boolean awaitUnbindResp);     // TODO 'is' in setter?
  void setBindTimeout(long bindTimeout);
  void setUnbindTimeout(long unbindTimeout);
  void setRequestExpiryTimeout(long requestExpiryTimeout);
  void setWindowSize(int windowSize);
  void setWindowWaitTimeout(long windowWaitTimeout);
  void setWindowMonitorInterval(long windowMonitorInterval);
  void setWriteTimeout(long writeTimeout);
  void setCountersEnabled(boolean countersEnabled);
  void setLogPduBody(boolean logBytes);
}