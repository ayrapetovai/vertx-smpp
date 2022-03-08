package com.example.smpp;

import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SmppSessionPduEncoder extends MessageToMessageEncoder<Pdu> {
  private static final Logger logger = LoggerFactory.getLogger("com.cloudhopper.smpp.channel.SmppSessionPduEncoder");
  private final PduTranscoder transcoder;

  public SmppSessionPduEncoder(PduTranscoder transcoder) {
    this.transcoder = transcoder;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Pdu pdu, List<Object> out) throws Exception {
    ByteBuf buf = transcoder.encode(pdu);
    logger.debug("Encoded PDU: {}", pdu);

    if (buf != null) {
      out.add(buf);
    }
  }
}
