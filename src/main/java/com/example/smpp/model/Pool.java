package com.example.smpp.model;

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

import com.example.smpp.session.SmppSession;
import io.vertx.core.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Pool {
  private long sessionCounter = 0;
  private final Map<Long, SmppSession> sessions = new HashMap<>();

  public <T extends SmppSession> T add(Function<Long, T> sessionCreator) {
    var id = sessionCounter++;
    var sess = sessionCreator.apply(id);
    sessions.put(id, sess);
    return sess;
  }

  public void forEach(Handler<SmppSession> handler) {
    sessions.values().forEach(handler::handle);
  }

  public int size() {
    return sessions.size();
  }

  public void remove(Long id) {
    sessions.remove(id);
  }
}
