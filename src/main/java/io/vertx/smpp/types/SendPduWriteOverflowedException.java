package io.vertx.smpp.types;

public class SendPduWriteOverflowedException extends SendPduFailedException {

  private final long bytesBeforeWritable;

  public SendPduWriteOverflowedException(String message, long bytesBeforeWritable) {
    super(message);
    this.bytesBeforeWritable = bytesBeforeWritable;
  }

  public long getBytesBeforeWritable() {
    return bytesBeforeWritable;
  }
}
