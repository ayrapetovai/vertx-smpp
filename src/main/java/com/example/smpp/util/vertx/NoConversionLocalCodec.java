package com.example.smpp.util.vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

// TODO в конце разработки удалить, если не понадобится (не должен)
//  вместо очередей надо использовать runOnContext
@Deprecated
public class NoConversionLocalCodec implements MessageCodec {
  public static final String CODEC_NAME = "NoConversionLocalCodec";

  @Override
  public void encodeToWire(Buffer buffer, Object o) {

  }

  @Override
  public Object decodeFromWire(int pos, Buffer buffer) {
    return null;
  }

  @Override
  public Object transform(Object o) {
    return o;
  }

  @Override
  public String name() {
    return CODEC_NAME;
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
