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

import com.example.smpp.pdu.PduRequest;
import com.example.smpp.pdu.PduResponse;
import com.example.smpp.session.SmppSession;

public class PduRequestContext<T extends PduResponse> {
  private final PduRequest<T> request;
  private final SmppSession session;

  public PduRequestContext(PduRequest<T> request, SmppSession session) {
    this.request = request;
    this.session = session;
  }

  public PduRequest<T> getRequest() {
    return request;
  }

  public SmppSession getSession() {
    return session;
  }

  @Override
  public String toString() {
    return request.toString();
  }
}
