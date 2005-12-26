/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.jbi.messaging;

import java.io.IOException;
import java.io.ObjectInput;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.jbi.messaging.ExchangePacket;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;

import junit.framework.TestCase;

public class MessageExchangeTest extends TestCase {

    public static class TestMessageExchange extends MessageExchangeImpl {
        public TestMessageExchange() {
            super(new ExchangePacket(), STATES);
        }
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        }
        private static int[][] STATES = {
            { CAN_CONSUMER + CAN_OWNER + CAN_SET_IN_MSG + CAN_SEND + CAN_SEND_SYNC + CAN_STATUS_ACTIVE, -1, -1, -1 },
        };
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
