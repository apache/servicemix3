/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jbi.messaging;

import java.io.IOException;
import java.io.ObjectInput;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;

import junit.framework.TestCase;

public class MessageExchangeTest extends TestCase {

    public static class TestMessageExchange extends MessageExchangeImpl {
        private static final long serialVersionUID = 5572313276570983400L;
        private static final int[][] STATES = {
            {CAN_CONSUMER + CAN_OWNER + CAN_SET_IN_MSG + CAN_SEND + CAN_STATUS_ACTIVE, -1, -1, -1 },
        };
        public TestMessageExchange() {
            super(new ExchangePacket(), STATES);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        }
    }
    
    public void testErrorStatus() throws Exception {
        MessageExchange me = new TestMessageExchange();
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertNull(me.getError());
        me.setError(new Exception());
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        assertNotNull(me.getError());
    }
    
}
