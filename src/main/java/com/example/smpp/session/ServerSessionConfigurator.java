package com.example.smpp.session;

public interface ServerSessionConfigurator {
  void setDropAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
  void setBindTimeout(long bindTimeout);
  void setUnbindTimeout(long unbindTimeout);
  void setRequestExpiryTimeout(long requestExpiryTimeout);
  void setWindowSize(int windowSize);
  void setWindowWaitTimeout(long windowWaitTimeout);
  void setWindowMonitorInterval(long windowMonitorInterval);
  void setWriteTimeout(long writeTimeout);
  void setCountersEnabled(boolean countersEnabled);
  void setLogPdu(boolean logPdu);
  void setLogBytes(boolean logBytes);
}