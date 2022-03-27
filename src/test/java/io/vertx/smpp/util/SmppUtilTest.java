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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 * Assetions symplifyed by Artem Ayrapetov
 * junit5 by Artem Ayrapetov
 */
public class SmppUtilTest {

    @Test
    public void isUserDataHeaderIndicatorEnabled() {
        assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x00));
        assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x01));
        assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x90));
        assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x80));
        assertTrue(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x40));
        assertTrue(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x41));
        assertTrue(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0xC0));
    }

    @Test
    public void isReplyPathEnabled() {
        assertFalse(SmppUtil.isReplyPathEnabled((byte) 0x00));
        assertFalse(SmppUtil.isReplyPathEnabled((byte) 0x01));
        assertTrue(SmppUtil.isReplyPathEnabled((byte) 0x90));
        assertTrue(SmppUtil.isReplyPathEnabled((byte) 0xC0));
        assertTrue(SmppUtil.isReplyPathEnabled((byte) 0x80));
        assertFalse(SmppUtil.isReplyPathEnabled((byte) 0x40));
        assertTrue(SmppUtil.isReplyPathEnabled((byte) 0x81));
    }

    @Test
    public void isMessageTypeSmscDeliveryReceipt() {
        assertFalse(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x00));
        assertTrue(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x04));
        // set both intermediate AND dlr
        assertTrue(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        assertFalse(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        assertTrue(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x64));
    }

    @Test
    public void isMessageTypeIntermediateDeliveryReceipt() {
        assertFalse(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x00));
        assertFalse(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x04));
        // set both intermediate AND dlr
        assertTrue(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        assertTrue(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        assertTrue(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x64));
    }

    @Test
    public void isMessageTypeEsmeDeliveryReceipt() {
        assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x00));
        assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x04));
        assertTrue(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x08));
        // set both intermediate AND dlr
        assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        assertTrue(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x64));
    }

    @Test
    public void isMessageTypeAnyDeliveryReceipt() {
        assertFalse(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x00));
        assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x04));
        assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x08));
        // set both intermediate AND dlr
        assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x64));
    }


    @Test
    public void isSmscDeliveryReceiptRequested() {
        assertFalse(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x00));
        assertTrue(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x01));
        assertTrue(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x21));
        assertFalse(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x20));
        assertFalse(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x02));
    }

    @Test
    public void isSmscDeliveryReceiptOnFailureRequested() {
        assertFalse(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x00));
        assertFalse(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x01));
        assertTrue(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x22));
        assertFalse(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x20));
        assertTrue(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x02));
    }

    @Test
    public void isIntermediateReceiptRequested() {
        assertFalse(SmppUtil.isIntermediateReceiptRequested((byte) 0x00));
        assertFalse(SmppUtil.isIntermediateReceiptRequested((byte) 0x01));
        assertTrue(SmppUtil.isIntermediateReceiptRequested((byte) 0x12));
        // this is actually bit 4 not bit 5 (SMPP 3.4 specs originally had both bits mentioned)
        assertTrue(SmppUtil.isIntermediateReceiptRequested((byte) 0x10));
        assertFalse(SmppUtil.isIntermediateReceiptRequested((byte) 0x02));
    }

    @Test
    public void toInterfaceVersionString() {
        assertEquals("3.4", SmppUtil.toInterfaceVersionString((byte)0x34));
        assertEquals("0.3", SmppUtil.toInterfaceVersionString((byte)0x03));
    }

}
