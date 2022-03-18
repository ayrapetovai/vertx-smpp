package com.example.smpp.util.core;

// TODO arena of objects?

/**
 * This class was made to replace AtomicReference to get rid of memory fence operations.
 * Garbage collector handbook
 * @param <T>
 */
public class Reference<T> {
  private T value;

  public T get() {
    return value;
  }

  public void set(T value) {
    this.value = value;
  }
}
