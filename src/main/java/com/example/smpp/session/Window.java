package com.example.smpp.session;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.example.smpp.types.SendPduDiscardedException;
import com.example.smpp.futures.SendPduFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Window {
  public static class RequestRecord<T extends PduResponse> {
    final SendPduFuture<T> responsePromise;
    final int sequenceNumber;
    final long expiresAt;

    public RequestRecord(SendPduFuture<T> responsePromise, int sequenceNumber, long expiresAt) {
      this.expiresAt = expiresAt;
      this.sequenceNumber = sequenceNumber;
      this.responsePromise = responsePromise;
    }
  }

  private final Map<Integer, RequestRecord> cache = new HashMap<>();

  public synchronized <T extends PduResponse> void offer(PduRequest<T> req, SendPduFuture<T> promise, long expiresAt) {
    var seqNum = req.getSequenceNumber();
    var record = new RequestRecord<>(promise, seqNum, expiresAt);
    var sameSeqPromise = cache.put(seqNum, record);
    if (sameSeqPromise != null) {
      sameSeqPromise.responsePromise
        .fail("same sequence id was used for new request");
    }
  }

  public synchronized SendPduFuture<PduResponse> complement(Integer seqNum) {
    var promiseOfRes = cache.remove(seqNum);
    if (promiseOfRes != null) {
      return promiseOfRes.responsePromise;
    } else {
      return null;
    }
  }

  public synchronized void discardAll() {
    var it = cache.values().iterator();
    while (it.hasNext()) {
      var record = it.next();
      record.responsePromise.tryFail(new SendPduDiscardedException("request discarded on close"));
      it.remove();
    }
  }

  public synchronized void purgeAllExpired(Consumer<RequestRecord> promiseHandler) {
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

  public synchronized int size() {
    return cache.size();
  }
}
