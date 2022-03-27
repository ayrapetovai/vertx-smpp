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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 * Assetions symplifyed by Artem Ayrapetov
 */
public class SmppUtilTest {

    @Test
    public void isUserDataHeaderIndicatorEnabled() {
        Assert.assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x00));
        Assert.assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x01));
        Assert.assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x90));
        Assert.assertFalse(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x80));
        Assert.assertTrue(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x40));
        Assert.assertTrue(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0x41));
        Assert.assertTrue(SmppUtil.isUserDataHeaderIndicatorEnabled((byte) 0xC0));
    }

    @Test
    public void isReplyPathEnabled() {
        Assert.assertFalse(SmppUtil.isReplyPathEnabled((byte) 0x00));
        Assert.assertFalse(SmppUtil.isReplyPathEnabled((byte) 0x01));
        Assert.assertTrue(SmppUtil.isReplyPathEnabled((byte) 0x90));
        Assert.assertTrue(SmppUtil.isReplyPathEnabled((byte) 0xC0));
        Assert.assertTrue(SmppUtil.isReplyPathEnabled((byte) 0x80));
        Assert.assertFalse(SmppUtil.isReplyPathEnabled((byte) 0x40));
        Assert.assertTrue(SmppUtil.isReplyPathEnabled((byte) 0x81));
    }

    @Test
    public void isMessageTypeSmscDeliveryReceipt() {
        Assert.assertFalse(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x00));
        Assert.assertTrue(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x04));
        // set both intermediate AND dlr
        Assert.assertTrue(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        Assert.assertFalse(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        Assert.assertTrue(SmppUtil.isMessageTypeSmscDeliveryReceipt((byte) 0x64));
    }

    @Test
    public void isMessageTypeIntermediateDeliveryReceipt() {
        Assert.assertFalse(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x00));
        Assert.assertFalse(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x04));
        // set both intermediate AND dlr
        Assert.assertTrue(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        Assert.assertTrue(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        Assert.assertTrue(SmppUtil.isMessageTypeIntermediateDeliveryReceipt((byte) 0x64));
    }

    @Test
    public void isMessageTypeEsmeDeliveryReceipt() {
        Assert.assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x00));
        Assert.assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x04));
        Assert.assertTrue(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x08));
        // set both intermediate AND dlr
        Assert.assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        Assert.assertTrue(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        Assert.assertFalse(SmppUtil.isMessageTypeEsmeDeliveryReceipt((byte) 0x64));
    }

    @Test
    public void isMessageTypeAnyDeliveryReceipt() {
        Assert.assertFalse(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x00));
        Assert.assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x04));
        Assert.assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x08));
        // set both intermediate AND dlr
        Assert.assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x24));
        // intermediate set, but esme receipt
        Assert.assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x28));
        // udh set & intermediate & dlr
        Assert.assertTrue(SmppUtil.isMessageTypeAnyDeliveryReceipt((byte) 0x64));
    }


    @Test
    public void isSmscDeliveryReceiptRequested() {
        Assert.assertFalse(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x00));
        Assert.assertTrue(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x01));
        Assert.assertTrue(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x21));
        Assert.assertFalse(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x20));
        Assert.assertFalse(SmppUtil.isSmscDeliveryReceiptRequested((byte) 0x02));
    }

    @Test
    public void isSmscDeliveryReceiptOnFailureRequested() {
        Assert.assertFalse(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x00));
        Assert.assertFalse(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x01));
        Assert.assertTrue(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x22));
        Assert.assertFalse(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x20));
        Assert.assertTrue(SmppUtil.isSmscDeliveryReceiptOnFailureRequested((byte) 0x02));
    }

    @Test
    public void isIntermediateReceiptRequested() {
        Assert.assertFalse(SmppUtil.isIntermediateReceiptRequested((byte) 0x00));
        Assert.assertFalse(SmppUtil.isIntermediateReceiptRequested((byte) 0x01));
        Assert.assertTrue(SmppUtil.isIntermediateReceiptRequested((byte) 0x12));
        // this is actually bit 4 not bit 5 (SMPP 3.4 specs originally had both bits mentioned)
        Assert.assertTrue(SmppUtil.isIntermediateReceiptRequested((byte) 0x10));
        Assert.assertFalse(SmppUtil.isIntermediateReceiptRequested((byte) 0x02));
    }

    @Test
    public void toInterfaceVersionString() {
        Assert.assertEquals("3.4", SmppUtil.toInterfaceVersionString((byte)0x34));
        Assert.assertEquals("0.3", SmppUtil.toInterfaceVersionString((byte)0x03));
    }

}
