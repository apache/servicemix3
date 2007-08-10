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
import org.apache.servicemix.jbi.nmr.flow.seda.SedaFlow;

/**
 * @version $Revision$
 */
public class SedaFlowTransactionTest extends AbstractTransactionTest {

    protected Flow createFlow() {
        return new SedaFlow();
    }

    public void testAsyncSendSyncReceive() throws Exception {
        try {
            super.testAsyncSendSyncReceive();
            fail("Seda flow does not handle asynchronous transactional exchanges");
        } catch (Exception e) {
            // Seda flow does not handle asynchronous transactional exchanges
        }
    }

    public void testAsyncSendAsyncReceive() throws Exception {
        try {
            super.testAsyncSendAsyncReceive();
            fail("Seda flow does not handle asynchronous transactional exchanges");
        } catch (Exception e) {
            // Seda flow does not handle asynchronous transactional exchanges
        }
    }

}
