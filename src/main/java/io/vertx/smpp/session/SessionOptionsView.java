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
  boolean getCountersEnabled();
  boolean getLogPduBody();

  void setDiscardAllOnUnbind(boolean dropAllOnUnbind);
  void setReplyToUnbind(boolean replyToUnbind);
  void setSendUnbindOnClose(boolean sendUnbindOnClose);
  void setAwaitUnbindResp(boolean awaitUnbindResp);
  void setDiscardTimeout(long drainTimeout);
//  void setBindTimeout(long bindTimeout); // cannot change, bind was done already
  void setUnbindTimeout(long unbindTimeout);
  void setRequestExpiryTimeout(long requestExpiryTimeout);
//  void setWindowSize(int windowSize); // the window size cannot be changed after connection is established
  void setWindowWaitTimeout(long windowWaitTimeout);
  void setWindowMonitorInterval(long windowMonitorInterval);
//  void setWriteTimeout(long writeTimeout); // cannot be changed after connection is established, value was passed to the channel
  void setCountersEnabled(boolean countersEnabled);
  void setLogPduBody(boolean logPduBody);

}