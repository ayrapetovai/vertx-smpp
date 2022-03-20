package com.example.smpp.types;

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

import java.util.concurrent.TimeUnit;

public class SendPduRequestTimeoutException extends SendPduFailedException {

  private final long offeredAt;
  private final long expiresAt;

  public SendPduRequestTimeoutException(String message, long offeredAtNs, long expiresAtNs) {
    super(message);
    this.offeredAt = offeredAtNs;
    this.expiresAt = expiresAtNs;
  }

  public long getOfferedAt(TimeUnit desiredTimeUnit) {
    return desiredTimeUnit.convert(offeredAt, TimeUnit.NANOSECONDS);
  }

  public long getExpiresAt(TimeUnit desiredTimeUnit) {
    return desiredTimeUnit.convert(expiresAt, TimeUnit.NANOSECONDS);
  }
}
