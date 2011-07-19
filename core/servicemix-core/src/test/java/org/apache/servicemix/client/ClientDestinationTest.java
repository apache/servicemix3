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
package org.apache.servicemix.client;

import java.io.StringReader;
import java.util.Arrays;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.api.Destination;
import org.apache.servicemix.jbi.api.Message;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.tck.MessageList;
import org.apache.servicemix.tck.Receiver;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ClientDestinationTest extends TestCase {
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ClientDestinationTest.class);

    protected AbstractXmlApplicationContext context;
    protected ServiceMixClient client;
    protected MessageList messageList = new MessageList();

    public void testInOnlyMessage() throws Exception {
        // START SNIPPET: message
        Destination destination = client.createDestination("service:http://servicemix.org/cheese/receiver");
        Message message = destination.createInOnlyMessage();
        message.setProperty("name", "James");
        message.setBody("<hello>world</hello>");
        
        client.send(message);
        // END SNIPPET: message 
        messageList.assertMessagesReceived(1);
    }
    
    public void testInOnlyExchange() throws Exception {
        // START SNIPPET: inOnly
        Destination destination = client.createDestination("service:http://servicemix.org/cheese/receiver");
        InOnly exchange = destination.createInOnlyExchange();

        NormalizedMessage message = exchange.getInMessage();
        message.setProperty("name", "James");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        client.send(exchange);
        // END SNIPPET: inOnly

        messageList.assertMessagesReceived(1);
    }
    
    public void testInOutExchange() throws Exception {
        // START SNIPPET: inOut
        Destination destination = client.createDestination("service:http://servicemix.org/cheese/myService");
        InOut exchange = destination.createInOutExchange();
        
        NormalizedMessage request = exchange.getInMessage();
        request.setProperty("name", "James");
        request.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        client.sendSync(exchange);
        
        NormalizedMessage response = exchange.getOutMessage();
        // END SNIPPET: inOut
        
        assertNotNull("Should have returned a non-null response!", response);

        LOGGER.info("Received result: {}", response);
    }


    public void testInOnlyMessageUsingPOJOWithXStreamMarshaling() throws Exception {
        TestBean bean = new TestBean();
        bean.setName("James");
        bean.setLength(12);
        bean.getAddresses().addAll(Arrays.asList(new String[] {"London", "LA" }));

        Destination destination = client.createDestination("service:http://servicemix.org/cheese/receiver");
        Message message = destination.createInOnlyMessage();
        message.setProperty("name", "James");
        message.setBody(bean);

        client.send(message);

        messageList.assertMessagesReceived(1);
    }

    protected void setUp() throws Exception {
        context = createBeanFactory();
        client = (ServiceMixClient) getBean("client");
        messageList = createMessageList();
    }

    /*
     * protected MessageList createMessageList() throws Exception { MessageList
     * answer = new MessageList(); Destination consumer =
     * client.createEndpoint(destinationUri, messageList); return answer; }
     */

    protected MessageList createMessageList() throws Exception {
        SpringJBIContainer jbi = (SpringJBIContainer) getBean("jbi");
        Receiver receiver = (Receiver) jbi.getBean("receiver");
        assertNotNull("receiver not found in JBI container", receiver);
        return receiver.getMessageList();
    }

    protected void tearDown() throws Exception {
        super.tearDown();

        if (context != null) {
            context.close();
        }
    }

    protected Object getBean(String name) {
        Object answer = context.getBean(name);
        assertNotNull("Could not find object in Spring for key: " + name, answer);
        return answer;
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/client/example.xml");
    }

}
