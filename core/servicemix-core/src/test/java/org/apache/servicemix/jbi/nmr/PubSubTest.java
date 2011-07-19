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
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.resolver.SubscriptionFilter;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PubSubTest extends TestCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PubSubTest.class);

    private SenderComponent sender;

    private JBIContainer container;

    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setEmbedded(true);
        container.setFlowName("seda");
        container.init();
        container.start();

        sender = new SenderComponent();
        ActivationSpec as = new ActivationSpec("source", sender);
        as.setService(new QName("http://www.test.com", "source"));
        as.setFailIfNoDestinationEndpoint(false);
        container.activateComponent(as);
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }

    public void testPubSub() throws Exception {
        ReceiverComponent recListener = new ReceiverComponent();
        container.activateComponent(createReceiverAS("receiver", recListener));
        sender.sendMessages(1);
        recListener.getMessageList().assertMessagesReceived(1);
    }

    public void testPubSubFiltered() throws Exception {
        ReceiverComponent recListener = new ReceiverComponent();
        container.activateComponent(createReceiverASFiltered("receiver", recListener));
        sender.sendMessages(1, false);
        recListener.getMessageList().assertMessagesReceived(1);
    }

    private ActivationSpec createReceiverAS(String id, Object component) {
        ActivationSpec as = new ActivationSpec(id, component);
        SubscriptionSpec ss = new SubscriptionSpec();
        ss.setService(new QName("http://www.test.com", "source"));
        as.setSubscriptions(new SubscriptionSpec[] {ss });
        as.setFailIfNoDestinationEndpoint(false);
        return as;
    }

    private ActivationSpec createReceiverASFiltered(String id, Object component) {
        ActivationSpec as = new ActivationSpec(id, component);
        SubscriptionSpec ss = new SubscriptionSpec();
        ss.setService(new QName("http://www.test.com", "source"));
        ss.setFilter(new Filter());
        as.setSubscriptions(new SubscriptionSpec[] {ss });
        as.setFailIfNoDestinationEndpoint(false);
        return as;
    }

    public static class Filter implements SubscriptionFilter {

        public boolean matches(MessageExchange arg0) {
            LOGGER.info("Matches");
            return true;
        }

    }

}
