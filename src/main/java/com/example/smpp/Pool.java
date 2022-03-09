package com.example.smpp;

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
