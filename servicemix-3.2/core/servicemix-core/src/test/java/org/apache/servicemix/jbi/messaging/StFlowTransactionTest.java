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

import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.st.STFlow;

/**
 * @version $Revision$
 */
public class StFlowTransactionTest extends AbstractTransactionTest {

    protected Flow createFlow() {
        return new STFlow();
    }

    public void testAsyncSendSyncReceive() throws Exception {
        try {
            super.testAsyncSendSyncReceive();
            fail("ST flow does not handle asynchronous transactional exchanges");
        } catch (Exception e) {
            // ST flow does not handle asynchronous transactional exchanges
        }
    }

    public void testAsyncSendAsyncReceive() throws Exception {
        try {
            super.testAsyncSendAsyncReceive();
            fail("ST flow does not handle asynchronous transactional exchanges");
        } catch (Exception e) {
            // ST flow does not handle asynchronous transactional exchanges
        }
    }

    public void testSyncSendAsyncReceive() throws Exception {
        try {
            super.testSyncSendAsyncReceive();
            fail("ST flow does not handle synchronous transactional exchanges");
        } catch (Exception e) {
            // ST flow does not handle synchronous transactional exchanges
        }
    }

    public void testSyncSendSyncReceive() throws Exception {
        try {
            super.testSyncSendSyncReceive();
            fail("ST flow does not handle synchronous transactional exchanges");
        } catch (Exception e) {
            // ST flow does not handle synchronous transactional exchanges
        }
    }
}
