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
package org.apache.servicemix.jbi.nmr;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import junit.framework.TestCase;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.FlowProvider;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;

public class SubscriptionTest extends TestCase {

    public void testStNullAsync() throws Exception {
        runTest("st", null, false);
    }

    public void testStStAsync() throws Exception {
        runTest("st", "st", false);
    }

    public void testStSedaAsync() throws Exception {
        runTest("st", "seda", false);
    }

    public void testSedaNullAsync() throws Exception {
        runTest("seda", null, false);
    }

    public void testSedaStAsync() throws Exception {
        runTest("seda", "st", false);
    }

    public void testSedaSedaAsync() throws Exception {
        runTest("seda", "seda", false);
    }

    public void testStNullSync() throws Exception {
        runTest("st", null, true);
    }

    public void testStStSync() throws Exception {
        runTest("st", "st", true);
    }

    public void testStSedaSync() throws Exception {
        runTest("st", "seda", true);
    }

    public void testSedaNullSync() throws Exception {
        runTest("seda", null, true);
    }

    public void testSedaStSync() throws Exception {
        runTest("seda", "st", true);
    }

    public void testSedaSedaSync() throws Exception {
        runTest("seda", "seda", true);
    }

    private void runTest(String flowName, String subscriptionFlowName, boolean sync) throws Exception {
        JBIContainer container = new JBIContainer();
        try {
            container.setEmbedded(true);
            if (subscriptionFlowName != null && !subscriptionFlowName.equals(flowName)) {
                container.getDefaultBroker().setFlows(new Flow[] { 
                                FlowProvider.getFlow(flowName), 
                                FlowProvider.getFlow(subscriptionFlowName) });
            } else {
                container.getDefaultBroker().setFlows(new Flow[] { 
                                FlowProvider.getFlow(flowName) });
            }
            if (subscriptionFlowName != null) {
                container.getDefaultBroker().getSubscriptionManager().setFlowName(subscriptionFlowName);
            }
            // TODO: check why the following line is enabled, there is
            // a 5 seconds pause when Management stuff is initialized
            // container.setCreateMBeanServer(true);
            container.init();
            container.start();

            SenderListener sender = new SenderListener();
            ActivationSpec senderActivationSpec = new ActivationSpec("sender", sender);
            senderActivationSpec.setFailIfNoDestinationEndpoint(false);
            container.activateComponent(senderActivationSpec);

            Receiver receiver1 = new ReceiverComponent();
            container.activateComponent(createReceiverAS("receiver1", receiver1));

            Receiver receiver2 = new ReceiverComponent();
            container.activateComponent(createReceiverAS("receiver2", receiver2));

            sender.sendMessages(1, sync);

            Thread.sleep(100);

            assertEquals(1, receiver1.getMessageList().getMessageCount());
            assertEquals(1, receiver2.getMessageList().getMessageCount());
            assertEquals(0, sender.responses.size());
        } finally {
            container.shutDown();
        }
    }

    private ActivationSpec createReceiverAS(String id, Object component) {
        ActivationSpec as = new ActivationSpec(id, component);
        SubscriptionSpec ss = new SubscriptionSpec();
        ss.setService(SenderComponent.SERVICE);
        as.setEndpoint(id);
        as.setSubscriptions(new SubscriptionSpec[] {ss });
        return as;
    }

    public static class SenderListener extends SenderComponent implements MessageExchangeListener {

        List responses = new CopyOnWriteArrayList();

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            responses.add(exchange);
        }

    }

}
