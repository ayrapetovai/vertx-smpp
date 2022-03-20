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

public class SendPduWindowTimeoutException extends SendPduFailedException {

  private final int windowSize;

  public SendPduWindowTimeoutException(String message, int windowSize) {
    super(message);
    this.windowSize = windowSize;
  }

  public int getWindowSize() {
    return windowSize;
  }
}
