package com.example.smpp;

import com.cloudhopper.smpp.pdu.Pdu;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

public class EventBusPduCodec implements MessageCodec<Pdu, Pdu> {

  public static final String NAME = "localPduCodec";

  @Override
  public void encodeToWire(Buffer buffer, Pdu pdu) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public Pdu decodeFromWire(int pos, Buffer buffer) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public Pdu transform(Pdu pdu) {
    return pdu;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
