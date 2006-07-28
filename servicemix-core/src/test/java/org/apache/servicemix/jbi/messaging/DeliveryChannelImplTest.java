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

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SenderComponent;

public class DeliveryChannelImplTest extends TestCase {

    protected JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
    }
    
    protected void tearDown() throws Exception {
        container.shutDown();
    }
    
    public void testExchangeFactoryOnOpenChannel() throws Exception {
        // Retrieve a delivery channel
        TestComponent component = new TestComponent(null, null);
        container.activateComponent(new ActivationSpec("component", component));
        DeliveryChannel channel = component.getChannel();
        // test
        MessageExchangeFactory mef = channel.createExchangeFactory();
        assertNotNull(mef);
        assertNotNull(mef.createInOnlyExchange());
    }
    
    public void testExchangeFactoryOnClosedChannel() throws Exception {
        // Retrieve a delivery channel
        TestComponent component = new TestComponent(null, null);
        container.activateComponent(new ActivationSpec("component", component));
        DeliveryChannel channel = component.getChannel();
        // test
        channel.close();
        MessageExchangeFactory mef = channel.createExchangeFactory();
        assertNotNull(mef);
        try {
            mef.createInOnlyExchange();
            fail("Exchange creation should have failed (JBI: 5.5.2.1.4)");
        } catch (MessagingException e) {
            // expected
        }
    }
    
    public void testSendSyncOnSameComponent() throws Exception {
        // Retrieve a delivery channel
        TestComponent component = new TestComponent(new QName("service"), "endpoint");
        container.activateComponent(new ActivationSpec("component", component));
        final DeliveryChannel channel = component.getChannel();

        // Create another thread
        new Thread() {
            public void run() {
                try {
                    InOut me = (InOut) channel.accept(5000);
                    NormalizedMessage nm = me.createMessage();
                    nm.setContent(new StringSource("<response/>"));
                    me.setOutMessage(nm);
                    channel.sendSync(me);
                } catch (MessagingException e) {
                    e.printStackTrace();
                    fail("Exception caught: " + e);
                }
            }
        }.start();
        
        MessageExchangeFactory factory = channel.createExchangeFactoryForService(new QName("service"));
        InOut me = factory.createInOutExchange();
        NormalizedMessage nm = me.createMessage();
        nm.setContent(new StringSource("<request/>"));
        me.setInMessage(nm);
        channel.sendSync(me);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        me.setStatus(ExchangeStatus.DONE);
        channel.send(me);
    }
    
    public static class TestComponent extends ComponentSupport {
        public TestComponent(QName service, String endpoint) {
            super(service, endpoint);
        }
        public DeliveryChannel getChannel() throws MessagingException {
            return getContext().getDeliveryChannel();
        }
    }
    
}
