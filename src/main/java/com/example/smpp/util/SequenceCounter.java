package com.example.smpp.util;

public class SequenceCounter {
  private int nextValue = 0;

  public synchronized int getAndInc() {
    var value = nextValue++;
    if (nextValue == Integer.MAX_VALUE) {
      nextValue = 0;
    }
    return value;
  }
}
