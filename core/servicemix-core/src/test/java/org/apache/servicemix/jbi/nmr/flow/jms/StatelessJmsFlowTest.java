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
package org.apache.servicemix.jbi.nmr.flow.jms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;

public class StatelessJmsFlowTest extends TestCase {

    protected static final int ACTIVEMQ_PORT = Integer.parseInt(System.getProperty("activemq.port"));
    protected static final String ACTIVEMQ_URL = "tcp://localhost:" + ACTIVEMQ_PORT;

    protected JBIContainer jbi1;
    protected JBIContainer jbi2;
    protected BrokerService broker;

    protected void setUp() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector(ACTIVEMQ_URL);
        broker.start();

        jbi1 = createContainer("jbi1");
        jbi2 = createContainer("jbi2");
    }

    protected void tearDown() throws Exception {
        jbi1.shutDown();
        jbi2.shutDown();
        broker.stop();
    }

    protected JBIContainer createContainer(String name) throws Exception {
        JBIContainer container = new JBIContainer();
        container.setName(name);
        container.setFlowName("jms?jmsURL=" + ACTIVEMQ_URL);
        container.setUseMBeanServer(false);
        container.setEmbedded(true);
        container.init();
        container.start();
        return container;
    }

    protected StatelessEcho activateProvider(JBIContainer container, boolean stateless) throws Exception {
        StatelessEcho echo = new StatelessEcho(stateless);
        container.activateComponent(echo, "echo");
        return echo;
    }

    protected StatelessSender activateConsumer(JBIContainer container) throws Exception {
        StatelessSender sender = new StatelessSender();
        container.activateComponent(sender, "sender");
        return sender;
    }

    public void testStatelessConsumer() throws Exception {
        activateProvider(jbi1, false);
        activateProvider(jbi2, false);
        StatelessSender sender1 = activateConsumer(jbi1);
        StatelessSender sender2 = activateConsumer(jbi2);

        sender1.sendMessages(100, true);

        int n1 = 0;
        int n2 = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            n1 = sender1.outIds.size();
            n2 = sender2.outIds.size();
            if (n1 + n2 == 100) {
                break;
            }
        }
        assertTrue(n1 != 0);
        assertTrue(n2 != 0);
        assertTrue(n1 + n2 == 100);
    }

    public void testStatefullConsumer() throws Exception {
        activateProvider(jbi1, false);
        activateProvider(jbi2, false);
        StatelessSender sender1 = activateConsumer(jbi1);
        StatelessSender sender2 = activateConsumer(jbi2);

        sender1.sendMessages(100, false);

        int n1 = 0;
        int n2 = 0;
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            n1 = sender1.outIds.size();
            n2 = sender2.outIds.size();
            if (n1 + n2 == 100) {
                break;
            }
        }
        assertTrue(n1 != 0);
        assertTrue(n2 == 0);
        assertTrue(n1 + n2 == 100);
    }

    public void testStatelessProvider() throws Exception {
        StatelessEcho echo1 = activateProvider(jbi1, true);
        StatelessEcho echo2 = activateProvider(jbi2, true);
        StatelessSender sender1 = activateConsumer(jbi1);
        activateConsumer(jbi2);

        sender1.sendMessages(100, false);

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            if (echo1.doneIds.size() + echo2.doneIds.size() == 100) {
                break;
            }
        }
        assertTrue(echo1.doneIds.size() + echo2.doneIds.size() == 100);

        // Check that the echo1 component received
        // DONE status for exchanges it did not handle
        // the first time.
        // Do not bother testing for echo2, as it will
        // be automatically true.
        Set doneIds1 = new HashSet();
        doneIds1.addAll(echo1.doneIds);
        doneIds1.removeAll(echo1.inIds);
        assertTrue(doneIds1.size() > 0);
    }

    public void testStatefullProvider() throws Exception {
        StatelessEcho echo1 = activateProvider(jbi1, false);
        StatelessEcho echo2 = activateProvider(jbi2, false);
        StatelessSender sender1 = activateConsumer(jbi1);
        activateConsumer(jbi2);

        sender1.sendMessages(100, false);

        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            if (echo1.doneIds.size() + echo2.doneIds.size() == 100) {
                break;
            }
        }
        assertTrue(echo1.doneIds.size() + echo2.doneIds.size() == 100);

        // Check that the echo1 component received
        // DONE status for exchanges it did not handle
        // the first time.
        // Do not bother testing for echo2, as it will
        // be automatically true.
        Set doneIds1 = new HashSet();
        doneIds1.addAll(echo1.doneIds);
        doneIds1.removeAll(echo1.inIds);
        assertTrue(doneIds1.size() == 0);
    }

    public static class StatelessSender extends ComponentSupport implements MessageExchangeListener {

        public static final QName SERVICE = new QName("sender");
        public static final String ENDPOINT = "ep";

        List outIds = new CopyOnWriteArrayList();

        public StatelessSender() {
            super(SERVICE, ENDPOINT);
        }

        public void sendMessages(int nb, boolean stateless) throws Exception {
            for (int i = 0; i < nb; i++) {
                MessageExchangeFactory mef = getDeliveryChannel().createExchangeFactory();
                InOut me = mef.createInOutExchange();
                me.setService(new QName("echo"));
                if (stateless) {
                    me.setProperty(JbiConstants.STATELESS_CONSUMER, Boolean.TRUE);
                }
                me.setInMessage(me.createMessage());
                me.getInMessage().setContent(new StringSource("<hello/>"));
                getDeliveryChannel().send(me);

            }
        }

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            outIds.add(exchange.getExchangeId());
            done(exchange);
        }
    }

    public static class StatelessEcho extends ComponentSupport implements MessageExchangeListener {

        boolean stateless;
        List inIds = new CopyOnWriteArrayList();
        List doneIds = new CopyOnWriteArrayList();

        public StatelessEcho(boolean stateless) {
            setService(new QName("echo"));
            setEndpoint("ep");
            this.stateless = stateless;
        }

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            if (exchange.getStatus() == ExchangeStatus.DONE) {
                doneIds.add(exchange.getExchangeId());
            } else {
                inIds.add(exchange.getExchangeId());
                if (stateless) {
                    exchange.setProperty(JbiConstants.STATELESS_PROVIDER, Boolean.TRUE);
                }
                NormalizedMessage out = exchange.createMessage();
                out.setContent(new StringSource("<world/>"));
                answer(exchange, out);
            }
        }
    }

}
