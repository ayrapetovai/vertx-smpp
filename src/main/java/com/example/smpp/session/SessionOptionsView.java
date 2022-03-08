package com.example.smpp.session;

import com.example.smpp.model.SmppBindType;

public interface SessionOptionsView extends SessionCallbacksView {
  Long getId();
  SmppBindType getBindType();
  String getSystemId();
  String getPassword();
  String getSystemType();
  String getAddressRange(); // String -> ImmutableAddress

  boolean getReplyToUnbind();
  boolean getDropAllOnUnbind();
  boolean isSendUnbindOnClose();
  boolean isAwaitUnbindResp();
  long getBindTimeout();
  long getUnbindTimeout();
  long getRequestExpiryTimeout();
  int getWindowSize();
  long getWindowWaitTimeout();
  long getWindowMonitorInterval();
  long getWriteTimeout();
  boolean getCountersEnabled();
  boolean getLogPdu();
  boolean getLogBytes();
}