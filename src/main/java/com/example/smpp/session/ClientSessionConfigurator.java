package com.example.smpp.session;

import com.example.smpp.model.SmppBindType;

public interface ClientSessionConfigurator {
  void setId(Long id);
  void setBindType(SmppBindType bindType);
  void setSystemId(String systemId);
  void setPassword(String password);
  void setSystemType(String systemType);
  void setAddressRange(String addressRange); // String -> ImmutableAddress

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