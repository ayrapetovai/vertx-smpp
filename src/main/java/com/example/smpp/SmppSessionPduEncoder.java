package com.example.smpp;

import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class SmppSessionPduEncoder extends MessageToMessageEncoder<Pdu> {
  private final PduTranscoder transcoder;

  public SmppSessionPduEncoder(PduTranscoder transcoder) {
    this.transcoder = transcoder;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Pdu pdu, List<Object> out) throws Exception {
    ByteBuf buf = ctx.alloc().buffer(); // TODO pre allocate

    buf = transcoder.encode(pdu);

    if (buf != null) {
      out.add(buf);
    }
  }
}
