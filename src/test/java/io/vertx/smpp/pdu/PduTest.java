package io.vertx.smpp.pdu;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

// my imports

/**
 *
 * @author joelauer (twitter: @jjlauer or <a href="http://twitter.com/jjlauer" target=window>http://twitter.com/jjlauer</a>)
 * Assetions symplifyed by Artem Ayrapetov
 * junit5 by Artem Ayrapetov
 */
public class PduTest {
    private static final Logger logger = LoggerFactory.getLogger(PduTest.class);
    
    @Test
    public void hasSequenceNumberAssigned() {
        Pdu pdu0 = new EnquireLink();

        assertFalse(pdu0.hasSequenceNumberAssigned());

        pdu0.setSequenceNumber(0);

        assertTrue(pdu0.hasSequenceNumberAssigned());

        pdu0.removeSequenceNumber();

        assertFalse(pdu0.hasSequenceNumberAssigned());
        assertEquals(0, pdu0.getSequenceNumber());
    }

    @Test
    public void hasCommandLengthCalculatedAndSet() {
        Pdu pdu0 = new EnquireLink();

        assertFalse(pdu0.hasCommandLengthCalculated());
        assertEquals(16, pdu0.calculateAndSetCommandLength());
        assertTrue(pdu0.hasCommandLengthCalculated());
        assertEquals(16, pdu0.getCommandLength());
    }
}
