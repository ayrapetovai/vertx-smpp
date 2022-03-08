package com.example.smpp;

import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SmppSessionPduDecoder extends ByteToMessageDecoder {
  private static final Logger logger = LoggerFactory.getLogger("com.cloudhopper.smpp.channel.SmppSessionPduDecoder");

  private final PduTranscoder transcoder;

  public SmppSessionPduDecoder(PduTranscoder transcoder) {
    this.transcoder = transcoder;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Pdu pdu = transcoder.decode(in);
    if (logger.isDebugEnabled()) {
      logger.debug("Decoded PDU: {}", pdu);
    }

    if (pdu != null)
      out.add(pdu);
  }
}