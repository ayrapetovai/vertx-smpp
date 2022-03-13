package com.example.smpp;

import com.cloudhopper.smpp.pdu.PduResponse;
import io.vertx.core.Promise;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Window<T extends PduResponse> {
  public static class RequestRecord<T extends PduResponse> {
    final Promise<T> responsePromise;
    final int sequenceNumber;
    final long expiresAt;

    public RequestRecord(Promise<T> responsePromise, int sequenceNumber, long expiresAt) {
      this.expiresAt = expiresAt;
      this.sequenceNumber = sequenceNumber;
      this.responsePromise = responsePromise;
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

  public synchronized void purgeAllExpired(Consumer<RequestRecord<T>> promiseHandler) {
    var now = System.currentTimeMillis();
    var it = cache.values().iterator();
    while (it.hasNext()) {
      var record = it.next();
      if (record.expiresAt < now && record.responsePromise != null) {
        it.remove();
        promiseHandler.accept(record);
      }
    }
  }
}
