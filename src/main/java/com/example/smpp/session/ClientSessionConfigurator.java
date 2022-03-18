package com.example.smpp.session;

import com.example.smpp.model.SmppBindType;

public interface ClientSessionConfigurator extends SessionCallbacks {
  void setBindType(SmppBindType bindType);
  void setSystemId(String systemId);
  void setPassword(String password);
  void setSystemType(String systemType);
  void setAddressRange(String addressRange); // String -> ImmutableAddress

  void setDiscardAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
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