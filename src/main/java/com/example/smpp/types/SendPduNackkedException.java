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

import com.example.smpp.model.SendPduExceptionType;

public class SendPduNackkedException extends SendPduFailedException {

  private final String resultMessage;
  private final int status;

  public SendPduNackkedException(String message, String resultMessage, int status) {
    super(message);
    this.resultMessage = resultMessage;
    this.status = status;
  }

  public String getResultMessage() {
    return resultMessage;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public SendPduExceptionType getType() {
    return SendPduExceptionType.GENERIC_NACK_RECEIVED;
  }
}
