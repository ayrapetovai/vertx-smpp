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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_RECEIVER));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_CANCEL_SM));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DATA_SM));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_SUBMIT_SM));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DELIVER_SM));
        Assert.assertTrue(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_UNBIND));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_GENERIC_NACK));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_RECEIVER_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_CANCEL_SM_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DATA_SM_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_SUBMIT_SM_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_DELIVER_SM_RESP));
        Assert.assertFalse(PduUtil.isRequestCommandId(SmppConstants.CMD_ID_UNBIND_RESP));
    }

    @Test
    public void isResponseCommandId() {
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_RECEIVER));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_CANCEL_SM));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DATA_SM));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_SUBMIT_SM));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DELIVER_SM));
        Assert.assertFalse(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_UNBIND));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_GENERIC_NACK));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSMITTER_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_RECEIVER_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_BIND_TRANSCEIVER_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_CANCEL_SM_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DATA_SM_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_ENQUIRE_LINK_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_SUBMIT_SM_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_DELIVER_SM_RESP));
        Assert.assertTrue(PduUtil.isResponseCommandId(SmppConstants.CMD_ID_UNBIND_RESP));
    }

    @Test
    public void calculateByteSizeOfAddress() {
        Assert.assertEquals(3, PduUtil.calculateByteSizeOfAddress(null));
        Assert.assertEquals(3, PduUtil.calculateByteSizeOfAddress(new Address()));
        Assert.assertEquals(4, PduUtil.calculateByteSizeOfAddress(new Address((byte)0x01, (byte)0x01, "A")));
    }

    @Test
    public void calculateByteSizeOfNullTerminatedString() {
        Assert.assertEquals(1, PduUtil.calculateByteSizeOfNullTerminatedString(null));
        Assert.assertEquals(1, PduUtil.calculateByteSizeOfNullTerminatedString(""));
        Assert.assertEquals(2, PduUtil.calculateByteSizeOfNullTerminatedString("A"));
    }
}
