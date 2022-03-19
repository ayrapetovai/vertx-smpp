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

import com.example.smpp.SmppConstants;
import com.example.smpp.types.Address;
import com.example.smpp.types.RecoverablePduException;
import com.example.smpp.types.UnrecoverablePduException;
import com.example.smpp.util.ByteBufUtil;

import com.example.smpp.util.PduUtil;
import com.example.smpp.util.StringUtil;
import io.netty.buffer.ByteBuf;

public class AlertNotification extends Pdu {

    protected Address sourceAddress;
    protected Address esmeAddress;

    public AlertNotification(){
        super( SmppConstants.CMD_ID_ALERT_NOTIFICATION, "alert_notification", true );
    }

    public Address getSourceAddress() {
        return this.sourceAddress;
    }

    public void setSourceAddress(Address value) {
        this.sourceAddress = value;
    }

    public Address getEsmeAddress() {
        return this.esmeAddress;
    }

    public void setEsmeAddress(Address value) {
        this.esmeAddress = value;
    }

    @Override
    protected int calculateByteSizeOfBody(){
        int bodyLength = 0;
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.sourceAddress);
        bodyLength += PduUtil.calculateByteSizeOfAddress(this.esmeAddress);
        return bodyLength;
    }

    @Override
    public void readBody( ByteBuf buffer ) throws UnrecoverablePduException, RecoverablePduException{
        this.sourceAddress = ByteBufUtil.readAddress(buffer);
        this.esmeAddress = ByteBufUtil.readAddress(buffer);
    }

    @Override
    public void writeBody( ByteBuf buffer ) throws UnrecoverablePduException, RecoverablePduException{
        ByteBufUtil.writeAddress(buffer, this.sourceAddress);
        ByteBufUtil.writeAddress(buffer, this.esmeAddress);
    }

    @Override
    protected void appendBodyToString( StringBuilder buffer ){
        buffer.append("( sourceAddr [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.sourceAddress));
        buffer.append("] esmeAddr [");
        buffer.append(StringUtil.toStringWithNullAsEmpty(this.esmeAddress));
        buffer.append("])");
    }

}
