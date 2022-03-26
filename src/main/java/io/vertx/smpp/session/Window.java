package io.vertx.smpp.session;

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

import io.vertx.smpp.pdu.PduRequest;
import io.vertx.smpp.pdu.PduResponse;
import io.vertx.smpp.types.SendPduDiscardedException;
import io.vertx.smpp.futures.SendPduFuture;
import io.vertx.smpp.util.core.FlowControl;
import io.vertx.core.impl.ContextInternal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Window {
  public static class RequestRecord<T extends PduResponse> {
    final SendPduFuture<T> responsePromise;
    final int sequenceNumber;
    final long offeredAt;
    final long expiresAt;

    public RequestRecord(SendPduFuture<T> responsePromise, int sequenceNumber, long expiresAt) {
      this.offeredAt = System.nanoTime();
      this.expiresAt = expiresAt;
      this.sequenceNumber = sequenceNumber;
      this.responsePromise = responsePromise;
    }
  }

  private final Map<Integer, RequestRecord> cache = new ConcurrentHashMap<>();
  private final ContextInternal context;

  private boolean expirationInProgress = false;

  public Window(ContextInternal context) {
    this.context = context;
  }

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

  // TODO this procedure must not be O(n)
  public synchronized void purgeAllExpired(Consumer<RequestRecord> promiseHandler) {
    if (expirationInProgress) {
      return;
    }
    expirationInProgress = true;
    var it = cache.values().iterator();
    FlowControl
        .whileCondition(context, it::hasNext, () -> {
          var record = it.next();
          if (record != null) {
            if (record.expiresAt < System.currentTimeMillis() && record.responsePromise != null) {
              it.remove();
              promiseHandler.accept(record);
            }
          }
        })
        .onComplete(v -> {
              synchronized (Window.this) {
                expirationInProgress = false;
              }
            }
        );
  }

  public synchronized int size() {
    return cache.size();
  }
}
