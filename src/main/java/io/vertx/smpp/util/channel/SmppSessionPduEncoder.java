package io.vertx.smpp.util.channel;

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

import io.vertx.smpp.pdu.Pdu;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SmppSessionPduEncoder extends MessageToMessageEncoder<Pdu> {
  private static final Logger logger = LoggerFactory.getLogger(SmppSessionPduEncoder.class);
  private final PduTranscoder transcoder;

  public SmppSessionPduEncoder(PduTranscoder transcoder) {
    this.transcoder = transcoder;
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, Pdu pdu, List<Object> out) throws Exception {
    ByteBuf buf = transcoder.encode(pdu);
    if (logger.isDebugEnabled()) {
      logger.debug("Encoded PDU: {}", pdu);
    }

    if (buf != null) {
      out.add(buf);
    }
  }
}
