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

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * JMSCluster Test for SendSync
 */
public class SimpleClusterSendSyncTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(SimpleClusterSendSyncTest.class);

    protected SpringJBIContainer jbi;
    protected AbstractXmlApplicationContext context;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("org/apache/servicemix/jbi/nmr/flow/jms/broker.xml");
        jbi = (SpringJBIContainer) context.getBean("jbi");
        assertNotNull("JBI Container not found in spring!", jbi);

    }

    protected void tearDown() throws Exception {
        context.close();
    }

    public void testSendSync() throws Exception {
        AbstractXmlApplicationContext ctx = new ClassPathXmlApplicationContext("org/apache/servicemix/jbi/nmr/flow/jms/client.xml");
        try {
            ServiceMixClient client = (ServiceMixClient) ctx.getBean("client");
            Thread.sleep(2000);
            InOut exchange = client.createInOutExchange();
            exchange.setService(new QName("http://www.habuma.com/foo", "pingService"));
            NormalizedMessage in = exchange.getInMessage();
            in.setContent(new StringSource("<ping>Pinging you</ping>"));
            LOGGER.info("SENDING; exchange.status={}", exchange.getStatus());
            client.sendSync(exchange);
            assertNotNull(exchange.getOutMessage());
            LOGGER.info("GOT RESPONSE; exchange.out={}", new SourceTransformer().toString(exchange.getOutMessage().getContent()));
            client.done(exchange);
            // Wait for done to be delivered
            Thread.sleep(50);
        } finally {
            ctx.close();
        }
    }

}
