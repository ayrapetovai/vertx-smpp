package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduResponse;
import io.vertx.core.Promise;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Window<T extends PduResponse> {
  public static class RequestRecord<T extends PduResponse> implements Delayed {
    final Promise<T> responsePromise;
    final int sequenceNumber;
    final long expiresAt;

    public RequestRecord(Promise<T> responsePromise, int sequenceNumber, long expiresAt) {
      this.expiresAt = expiresAt;
      this.sequenceNumber = sequenceNumber;
      this.responsePromise = responsePromise;
    }

    @Override
    public long getDelay(TimeUnit unit) {
      return unit.convert(expiresAt - System.currentTimeMillis(), MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
      long diff = getDelay(MILLISECONDS) - other.getDelay(MILLISECONDS);
      return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }
  }

  private final Map<Integer, RequestRecord<T>> cache = new HashMap<>();

  public synchronized <T extends PduResponse> Promise<T> offer(Integer seqNum, long expiresAt) {
    var promise = Promise.<T>promise();
    var record = new RequestRecord(promise, seqNum, expiresAt);
    var sameSeqPromise = cache.put(seqNum, record);
    if (sameSeqPromise != null) {
      sameSeqPromise.responsePromise
        .fail("same sequence id was used for new request");
    }
    return promise;
  }

  public synchronized Promise<T> complement(Integer seqNum) {
    var promiseOfRes = cache.remove(seqNum);
    if (promiseOfRes != null) {
      return promiseOfRes.responsePromise;
    } else {
      return null;
    }
  }

  public synchronized void forAllExpired(Consumer<RequestRecord<T>> promiseHandler) {
    var now = System.currentTimeMillis();
    for (var record: cache.values()) {
      if (record.expiresAt < now && record.responsePromise != null) {
        promiseHandler.accept(record);
      }
    }
  }
}
