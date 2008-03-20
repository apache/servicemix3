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

import javax.jbi.JBIException;

import org.apache.servicemix.jbi.RuntimeJBIException;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.AsyncReceiverPojo;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * @version $Revision$
 */
public abstract class AbstractClusteredTransactionTest extends AbstractTransactionTest {

    protected JBIContainer receiverContainer;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        receiverContainer = createJbiContainer("receiverContainer");
        Thread.sleep(3000);
    }

    protected void tearDown() throws Exception {
        receiverContainer.shutDown();
        super.tearDown();
    }

    protected void runClusteredTest(final boolean syncSend, final boolean syncReceive) throws Exception {
        final SenderComponent sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));
        final Receiver receiver;
        if (syncReceive) {
            receiver = new ReceiverComponent();
        } else {
            receiver = new AsyncReceiverPojo();
        }

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver));
        Thread.sleep(1000);

        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                try {
                    sender.sendMessages(NUM_MESSAGES, syncSend);
                } catch (JBIException e) {
                    throw new RuntimeJBIException(e);
                }
                return null;
            }
        });
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }

    public void testClusteredSyncSendSyncReceive() throws Exception {
        try {
            runClusteredTest(true, true);
            fail("sendSync can not be used on clustered flows with external components");
        } catch (Exception e) {
            // ok
        }
    }

    public void testClusteredAsyncSendSyncReceive() throws Exception {
        runClusteredTest(false, true);
    }

    public void testClusteredSyncSendAsyncReceive() throws Exception {
        try {
            runClusteredTest(true, false);
            fail("sendSync can not be used on clustered flows with external components");
        } catch (Exception e) {
            // ok
        }
    }

    public void testClusteredAsyncSendAsyncReceive() throws Exception {
        runClusteredTest(false, false);
    }

}
