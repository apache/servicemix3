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
package org.apache.servicemix.remoting;

import java.io.StringReader;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.servicemix.client.ServiceMixClient;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class RemoteServiceMixClientTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(RemoteServiceMixClientTest.class);

    protected AbstractXmlApplicationContext context;
    protected ServiceMixClient client;

    // Send methods
    // -------------------------------------------------------------------------
    public void testRemoteSend() throws Exception {
        InOut exchange = client.createInOutExchange();
        NormalizedMessage message = exchange.getInMessage();
        message.setProperty("name", "lufc");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));
        assertTrue(client.sendSync(exchange));
        LOGGER.info("OUT = {}", exchange.getOutMessage());
        //assertEquals(exchange.getInMessage().getContent(),exchange.getOutMessage().getContent());
    }

    protected void setUp() throws Exception {
        context = createBeanFactory();
        client = (ServiceMixClient) getBean("client");
        assertNotNull(getBean("jbi"));
    }

    protected Object getBean(String name) {
        Object answer = context.getBean(name);
        assertNotNull("Could not find object in Spring for key: " + name, answer);
        return answer;
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/remoting/example.xml");
    }

}
