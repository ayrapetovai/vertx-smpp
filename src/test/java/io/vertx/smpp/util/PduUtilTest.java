package io.vertx.smpp.util;

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

// third party imports


import io.vertx.smpp.SmppConstants;
import io.vertx.smpp.types.Address;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

// my imports

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 * Assetions symplifyed by Artem Ayrapetov
 */
public class PduUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(PduUtilTest.class);

    @Test
    public void isRequestCommandId() {
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_RECEIVER));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_CANCEL_SM));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DATA_SM));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_SUBMIT_SM));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DELIVER_SM));
        assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_UNBIND));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_GENERIC_NACK));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_RECEIVER_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_CANCEL_SM_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DATA_SM_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_SUBMIT_SM_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DELIVER_SM_RESP));
        assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_UNBIND_RESP));
    }

    @Test
    public void isResponseCommandId() {
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_RECEIVER));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_CANCEL_SM));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DATA_SM));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_SUBMIT_SM));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DELIVER_SM));
        assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_UNBIND));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_GENERIC_NACK));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_RECEIVER_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_CANCEL_SM_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DATA_SM_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_SUBMIT_SM_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DELIVER_SM_RESP));
        assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_UNBIND_RESP));
    }

    @Test
    public void calculateByteSizeOfAddress() {
        assertEquals(3, PduUtil.calculateByteSizeOfAddress(null));
        assertEquals(3, PduUtil.calculateByteSizeOfAddress(new Address()));
        assertEquals(4, PduUtil.calculateByteSizeOfAddress(new Address((byte)0x01, (byte)0x01, "A")));
    }

    @Test
    public void calculateByteSizeOfNullTerminatedString() {
        assertEquals(1, PduUtil.calculateByteSizeOfNullTerminatedString(null));
        assertEquals(1, PduUtil.calculateByteSizeOfNullTerminatedString(""));
        assertEquals(2, PduUtil.calculateByteSizeOfNullTerminatedString("A"));
    }
}
