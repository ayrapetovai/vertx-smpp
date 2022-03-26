package io.vertx.smpp.session;

//   Copyright 2022 Artem Ayrapetov
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

import io.vertx.smpp.model.SmppBindType;
import io.vertx.smpp.types.Address;

public interface SessionOptionsView {
  SmppBindType getBindType();
  String getSystemId();
  String getPassword();
  String getSystemType();
  Address getAddressRange();

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
  int getWriteQueueSize();
  long getOverflowMonitorInterval();
  boolean getCountersEnabled();
  boolean getLogPduBody();

  SessionOptionsView setDiscardAllOnUnbind(boolean dropAllOnUnbind);
  SessionOptionsView setReplyToUnbind(boolean replyToUnbind);
  SessionOptionsView setSendUnbindOnClose(boolean sendUnbindOnClose);
  SessionOptionsView setAwaitUnbindResp(boolean awaitUnbindResp);
  SessionOptionsView setDiscardTimeout(long drainTimeout);
//  SessionOptionsView setBindTimeout(long bindTimeout); // cannot change, bind was done already
  SessionOptionsView setUnbindTimeout(long unbindTimeout);
  SessionOptionsView setRequestExpiryTimeout(long requestExpiryTimeout);
//  SessionOptionsView setWindowSize(int windowSize); // the window size cannot be changed after connection is established
  SessionOptionsView setWindowWaitTimeout(long windowWaitTimeout);
  SessionOptionsView setWindowMonitorInterval(long windowMonitorInterval);
//  SessionOptionsView setWriteTimeout(long writeTimeout); // cannot be changed after connection is established, value was passed to the channel
  SessionOptionsView setWriteQueueSize(int writeQueueSize);
  SessionOptionsView setOverflowMonitorInterval(long overflowMonitorInterval);
  SessionOptionsView setCountersEnabled(boolean countersEnabled);
  SessionOptionsView setLogPduBody(boolean logPduBody);

}