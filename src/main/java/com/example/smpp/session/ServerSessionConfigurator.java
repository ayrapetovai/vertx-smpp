package com.example.smpp.session;

public interface ServerSessionConfigurator extends SessionCallbacks {
  void setSystemId(String systemId);
  void setDropAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
  void setSendUnbindOnClose(boolean sendUnbindOnClose);
  void setAwaitUnbindResp(boolean awaitUnbindResp);
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