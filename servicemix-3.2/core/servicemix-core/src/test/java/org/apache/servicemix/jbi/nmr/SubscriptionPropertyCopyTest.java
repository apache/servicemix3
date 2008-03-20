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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.FlowProvider;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.Sender;
import org.apache.servicemix.tck.SenderComponent;

public class SubscriptionPropertyCopyTest extends TestCase {

    public void testStNull() throws Exception {
        runTest("st", null);
    }

    public void testStSt() throws Exception {
        runTest("st", "st");
    }

    public void testStSeda() throws Exception {
        runTest("st", "seda");
    }

    public void testSedaNull() throws Exception {
        runTest("seda", null);
    }

    public void testSedaSt() throws Exception {
        runTest("seda", "st");
    }

    public void testSedaSeda() throws Exception {
        runTest("seda", "seda");
    }

    private void runTest(String flowName, String subscriptionFlowName) throws Exception {
        JBIContainer container = new JBIContainer();
        try {
            if (subscriptionFlowName != null && !subscriptionFlowName.equals(flowName)) {
                container.getDefaultBroker().setFlows(new Flow[] { 
                                FlowProvider.getFlow(flowName), FlowProvider.getFlow(subscriptionFlowName) });
            } else {
                container.getDefaultBroker().setFlows(new Flow[] { 
                                FlowProvider.getFlow(flowName) });
            }
            if (subscriptionFlowName != null) {
                container.getDefaultBroker().getSubscriptionManager().setFlowName(subscriptionFlowName);
            }
            container.setEmbedded(true);
            container.init();
            container.start();

            Sender sender = new SenderComponent();
            ActivationSpec senderActivationSpec = new ActivationSpec("sender", sender);
            senderActivationSpec.setFailIfNoDestinationEndpoint(false);
            container.activateComponent(senderActivationSpec);

            ReceiverListener receiver1 = new ReceiverListener();
            container.activateComponent(createReceiverAS("receiver1", receiver1));

            ReceiverListener receiver2 = new ReceiverListener();
            container.activateComponent(createReceiverAS("receiver2", receiver2));

            sender.sendMessages(1);

            Thread.sleep(100);

            assertFalse(receiver1.isPropertySetOnExchange());
            assertFalse(receiver2.isPropertySetOnExchange());
            assertFalse(receiver1.isPropertySetOnMessage());
            assertFalse(receiver2.isPropertySetOnMessage());
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

    public static class ReceiverListener extends ReceiverComponent {
        private boolean propertySetOnExchange;

        private boolean propertySetOnMessage;

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            propertySetOnExchange = exchange.getProperty("testProperty") != null;
            exchange.setProperty("testProperty", "foo");
            NormalizedMessage in = exchange.getMessage("in");
            propertySetOnMessage = in.getProperty("testProperty") != null;
            in.setProperty("testProperty", "foo");
        }

        public boolean isPropertySetOnExchange() {
            return propertySetOnExchange;
        }

        public boolean isPropertySetOnMessage() {
            return propertySetOnMessage;
        }
    }
}
