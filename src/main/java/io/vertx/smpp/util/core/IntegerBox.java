package io.vertx.smpp.util.core;

/**
 * This class was made to replace AtomicInteger to get rid of memory fence operations.
 */
public class IntegerBox {
  private int value;

  public IntegerBox(int value) {
    this.value = value;
  }

  public int get() {
    return value;
  }

  public void set(int value) {
    this.value = value;
  }

  public int getAndIncrement() {
    return value++;
  }
}
