package io.vertx.smpp.pdu;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2013 Cloudhopper by Twitter
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

import io.vertx.smpp.SmppConstants;
import io.vertx.smpp.types.RecoverablePduException;
import io.vertx.smpp.types.UnrecoverablePduException;
import io.netty.buffer.ByteBuf;

/**
 * SMPP cancel_sm_resp implementation.
 *
 * @author chris.matthews <idpromnut@gmail.com>
 */
public class CancelSmResp extends PduResponse {

    public CancelSmResp() {
        super(SmppConstants.CMD_ID_CANCEL_SM_RESP, "cancel_sm_resp");
    }

    @Override
    public void readBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        // nothing
    }

    @Override
    public int calculateByteSizeOfBody() {
        return 0;
    }

    @Override
    public void writeBody(ByteBuf buffer) throws UnrecoverablePduException, RecoverablePduException {
        // do nothing
    }

    @Override
    public void appendBodyToString(StringBuilder buffer) {
    }
}
