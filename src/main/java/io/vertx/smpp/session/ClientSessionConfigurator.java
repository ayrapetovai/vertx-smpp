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

public interface ClientSessionConfigurator extends SessionCallbacks {
  void setBindType(SmppBindType bindType);
  void setSystemId(String systemId);
  void setPassword(String password);
  void setSystemType(String systemType);
  void setAddressRange(Address addressRange);

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