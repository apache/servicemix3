/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
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
 */
package org.apache.servicemix.jbi.messaging;

import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow;

/**
 * @version $Revision$
 */
public class JmsFlowTransactionTest extends AbstractTransactionTest {

    protected Flow createFlow() {
    	return new JMSFlow();
    }

    public void testSyncSendSyncReceive() throws Exception {
        try {
            super.testSyncSendSyncReceive();
            fail("JMS flow does not handle transactions");
        } catch (Exception e) {
            // JMS flow does not handle transactions
        }
    }

    public void testSyncSendAsyncReceive() throws Exception {
        try {
            super.testSyncSendAsyncReceive();
            fail("JMS flow does not handle transactions");
        } catch (Exception e) {
            // JMS flow does not handle transactions
        }
    }

    public void testAsyncSendAsyncReceive() throws Exception {
        try {
            super.testAsyncSendAsyncReceive();
            fail("JMS flow does not handle transactions");
        } catch (Exception e) {
            // JMS flow does not handle transactions
        }
    }

    public void testAsyncSendSyncReceive() throws Exception {
        try {
            super.testAsyncSendSyncReceive();
            fail("JMS flow does not handle transactions");
        } catch (Exception e) {
            // JMS flow does not handle transactions
        }
    }

}
