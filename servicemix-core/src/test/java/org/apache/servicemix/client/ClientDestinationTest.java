/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.client.TestBean;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.tck.MessageList;
import org.apache.servicemix.tck.Receiver;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

import javax.jbi.JBIException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.stream.StreamSource;

import java.io.StringReader;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class ClientDestinationTest extends TestCase {
    private static final transient Log log = LogFactory.getLog(ClientDestinationTest.class);

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

        log.info("Received result: " + response);
    }


    /*
    public void testSendUsingMessage() throws Exception {
        Message message = inOnlyDestination.createMessage();
        message.setProperty("name", "James");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        inOnlyDestination.send(message);    

        messageList.assertMessagesReceived(1);
    }

    public void testSendUsingMessageUsingPOJOWithXStreamMarshaling() throws Exception {
        TestBean bean = new TestBean();
        bean.setName("James");
        bean.setLength(12);
        bean.getAddresses().addAll(Arrays.asList(new String[] { "London", "LA" }));

        Message message = inOnlyDestination.createMessage(bean);
        message.setProperty("name", "James");

        inOnlyDestination.send(message);

        messageList.assertMessagesReceived(1);
    }

    public void testRequestResponse() throws Exception {
        InOut exchange = inOutDestination.createInOutExchange();
        Message request = inOutDestination.createMessage();
        exchange.setInMessage(request);
        Message response = inOutDestination.invoke(exchange);

        assertNotNull("Should have returned a non-null response!", response);

        log.info("Received result: " + response);
    }
    */

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
