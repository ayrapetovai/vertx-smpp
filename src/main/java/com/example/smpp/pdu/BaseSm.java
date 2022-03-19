package com.example.smpp.pdu;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.example.smpp.types.RecoverablePduException;
import com.example.smpp.types.UnrecoverablePduException;
import com.example.smpp.types.Address;
import com.example.smpp.types.SmppInvalidArgumentException;
import com.example.smpp.util.ByteBufUtil;
import com.example.smpp.util.HexUtil;
import com.example.smpp.util.PduUtil;
import com.example.smpp.util.StringUtil;
import io.netty.buffer.ByteBuf;

/**
 * Base "short message" PDU as a super class for submit_sm, deliver_sm, and
 * data_sm.  Having a common base class they all inherit from makes it easier
 * to work with requests in a standard way, even though data_sm does NOT actually
 * support all of the same parameters.
 * 
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>) 
 */
public abstract class BaseSm<R extends PduResponse> extends PduRequest<R> {

    protected String serviceType;
    protected Address sourceAddress;
    protected Address destAddress;
    protected byte esmClass;
    private byte protocolId;                    // not present in data_sm
    private byte priority;                      // not present in data_sm
    private String scheduleDeliveryTime;        // not present in data_sm
    private String validityPeriod;              // not present in data_sm
    protected byte registeredDelivery;
    private byte replaceIfPresent;              // not present in data_sm
    protected byte dataCoding;
    private byte defaultMsgId;                  // not present in data_sm, not used in deliver_sm
    private byte[] shortMessage;                // not present in data_sm         

    public BaseSm(int commandId, String name) {
        super(commandId, name);
    }

    public int getShortMessageLength() {
        return (this.shortMessage == null ? 0 : this.shortMessage.length);
    }

    public byte[] getShortMessage() {
        return this.shortMessage;
    }

    public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
        if (value != null && value.length > 255) {
            throw new SmppInvalidArgumentException("A short message in a PDU can only be a max of 255 bytes [actual=" + value.length + "]; use optional parameter message_payload as an alternative");
        }
        this.shortMessage = value;
    }

    public byte getReplaceIfPresent() {
        return this.replaceIfPresent;
    }

    public void setReplaceIfPresent(byte value) {
        this.replaceIfPresent = value;
    }

    public byte getDataCoding() {
        return this.dataCoding;
    }

    public void setDataCoding(byte value) {
        this.dataCoding = value;
    }

    public byte getDefaultMsgId() {
        return this.defaultMsgId;
    }

    public void setDefaultMsgId(byte value) {
        this.defaultMsgId = value;
    }

    public byte getRegisteredDelivery() {
        return this.registeredDelivery;
    }

    public void setRegisteredDelivery(byte value) {
        this.registeredDelivery = value;
    }

    public String getValidityPeriod() {
        return this.validityPeriod;
    }

    public void setValidityPeriod(String value) {
        this.validityPeriod = value;
    }

    public String getScheduleDeliveryTime() {
        return this.scheduleDeliveryTime;
    }

    public void setScheduleDeliveryTime(String value) {
        this.scheduleDeliveryTime = value;
    }

    public byte getPriority() {
        return this.priority;
    }

    public void setPriority(byte value) {
        this.priority = value;
    }

    public byte getEsmClass() {
        return this.esmClass;
    }

    public void setEsmClass(byte value) {
        this.esmClass = value;
    }

    public byte getProtocolId() {
        return this.protocolId;
    }

    public void setProtocolId(byte value) {
        this.protocolId = value;
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String value) {
        this.serviceType = value;
    }

    public Address getSourceAddress() {
        return this.sourceAddress;
    }

    public void setSourceAddress(Address value) {
        this.sourceAddress = value;
    }

    public Address getDestAddress() {
        return this.destAddress;
    }

    public void setDestAddress(Address value) {
        this.destAddress = value;
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        this.serviceType = ByteBufUtil.readNullTerminatedString(buffer);
        this.sourceAddress = ByteBufUtil.readAddress(buffer);
        this.destAddress = ByteBufUtil.readAddress(buffer);
        this.esmClass = buffer.readByte();
        this.protocolId = buffer.readByte();
        this.priority = buffer.readByte();
        this.scheduleDeliveryTime = ByteBufUtil.readNullTerminatedString(buffer);
        this.validityPeriod = ByteBufUtil.readNullTerminatedString(buffer);
        this.registeredDelivery = buffer.readByte();
        this.replaceIfPresent = buffer.readByte();
        this.dataCoding = buffer.readByte();
        this.defaultMsgId = buffer.readByte();
        // this is always an unsigned version of the short message length
        short shortMessageLength = buffer.readUnsignedByte();
        this.shortMessage = new byte[shortMessageLength];
        buffer.readBytes(this.shortMessage);
    }

    @Override
    public int calculateByteSizeOfBody() {
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.serviceType);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.destAddress);
        bodyLength += 3;    // esmClass, priority, protocolId
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.scheduleDeliveryTime);
        bodyLength += PduUtil.calculateByteSizeOfNullTerminatedString(this.validityPeriod);
        bodyLength += 5;    // regDelivery, replace, dataCoding, defaultMsgId, messageLength bytes
        bodyLength += getShortMessageLength();
        return bodyLength;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        ByteBufUtil.writeNullTerminatedString(buffer, this.serviceType);
        ByteBufUtil.writeAddress(buffer, this.sourceAddress);
        ByteBufUtil.writeAddress(buffer, this.destAddress);
        buffer.writeByte(this.esmClass);
        buffer.writeByte(this.protocolId);
        buffer.writeByte(this.priority);
        ByteBufUtil.writeNullTerminatedString(buffer, this.scheduleDeliveryTime);
        ByteBufUtil.writeNullTerminatedString(buffer, this.validityPeriod);
        buffer.writeByte(this.registeredDelivery);
        buffer.writeByte(this.replaceIfPresent);
        buffer.writeByte(this.dataCoding);
        buffer.writeByte(this.defaultMsgId);
        buffer.writeByte((byte)getShortMessageLength());
        if (this.shortMessage != null) {
            buffer.writeBytes(this.shortMessage);
        }
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
        buffer.append("(serviceType [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.serviceType));
        buffer.append("] sourceAddr [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.sourceAddress));
        buffer.append("] destAddr [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.destAddress));

        buffer.append("] esmCls [0x");
        buffer.append(HexUtil.toHexString(this.esmClass));
        buffer.append("] regDlvry [0x");
        buffer.append(HexUtil.toHexString(this.registeredDelivery));
        // NOTE: skipped protocolId, priority, scheduledDlvryTime, validityPeriod,replace and defaultMsgId
        buffer.append("] dcs [0x");
        buffer.append(HexUtil.toHexString(this.dataCoding));
        buffer.append("] message [");
        HexUtil.appendHexString(buffer, this.shortMessage);
        buffer.append("])");
    }
}