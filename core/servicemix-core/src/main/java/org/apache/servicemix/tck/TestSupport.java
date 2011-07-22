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
package org.apache.servicemix.tck;

import java.io.IOException;
import java.util.Date;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;

/**
 * @version $Revision$
 */
public abstract class TestSupport extends SpringTestSupport {
    protected ServiceMixClient client;

    protected Receiver receiver;

    protected void setUp() throws Exception {
        super.setUp();
        client = (ServiceMixClient) getBean("client");
        SpringJBIContainer jbi = (SpringJBIContainer) getBean("jbi");
        receiver = (Receiver) jbi.getBean("receiver");
    }

    /**
     * Sends messages to the given service and asserts that the receiver gets
     * them all
     * 
     * @param service
     * @throws javax.jbi.messaging.MessagingException
     */
    protected void assertSendAndReceiveMessages(QName service) throws Exception {
        sendMessages(service, messageCount);
        assertMessagesReceived(messageCount);
    }

    protected void sendMessages(QName service, int messageCount) throws Exception {
        sendMessages(service, messageCount, true, null);
    }

    protected void sendMessages(QName service, int messageCount, String message) throws Exception {
        sendMessages(service, messageCount, true, message);
    }

    /**
     * Sends the given number of messages to the given service
     * 
     * @param service
     * @throws javax.jbi.messaging.MessagingException
     */
    protected void sendMessages(QName service, int messageCount, boolean sync, String msg) throws Exception {
        for (int i = 1; i <= messageCount; i++) {
            InOnly exchange = client.createInOnlyExchange();

            NormalizedMessage message = exchange.getInMessage();
            message.setProperty("name", "James");
            message.setProperty("id", new Integer(i));
            message.setProperty("idText", "" + i);
            if (msg != null && msg.length() > 0) {
                message.setContent(new StringSource(msg));
            } else {
                message.setContent(new StringSource(createMessageXmlText(i)));
            }

            exchange.setService(service);
            if (sync) {
                client.sendSync(exchange);
            } else {
                client.send(exchange);
            }

            // lets assert that we have no failure
            Exception error = exchange.getError();
            if (error != null) {
                throw error;
            }

            Fault fault = exchange.getFault();
            assertEquals("Should have no fault!", null, fault);
        }
    }

    protected String createMessageXmlText(int index) {
        return "<sample id='" + index + "' sent='" + new Date() + "'>hello world!</sample>";
    }

    protected void assertMessagesReceived() throws Exception {
        assertMessagesReceived(messageCount);
    }

    protected void assertMessagesReceived(int messageCount) throws Exception {
        assertNotNull("receiver not found in JBI container", receiver);

        MessageList messageList = receiver.getMessageList();
        assertMessagesReceived(messageList, messageCount);
    }

    protected MessageList assertMessagesReceived(String receiverName, int messageCount) throws Exception {
        Receiver rcv = (Receiver) getBean(receiverName);
        assertNotNull("receiver: " + receiverName + " not found in JBI container", rcv);

        MessageList messageList = rcv.getMessageList();
        assertMessagesReceived(messageList, messageCount);
        return messageList;
    }

    /**
     * Performs a request using the given file from the classpath as the request
     * body and return the answer
     * 
     * @param serviceName
     * @param fileOnClassPath
     * @return
     * @throws JBIException
     */
    protected Object requestServiceWithFileRequest(QName serviceName, String fileOnClassPath) throws Exception {
        Source content = getSourceFromClassPath(fileOnClassPath);
        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(serviceName);
        Object answer = client.request(resolver, null, null, content);
        if (answer instanceof Source) {
            answer = transformer.toDOMNode((Source) answer);
        }
        return answer;
    }

    /**
     * Performs a request using the given file from the classpath as the request
     * body and return the answer
     * 
     * @param serviceName
     * @param fileOnClassPath
     * @return
     * @throws JBIException
     */
    protected void sendServiceWithFileRequest(QName serviceName, String fileOnClassPath) throws Exception {
        Source content = getSourceFromClassPath(fileOnClassPath);
        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(serviceName);
        client.send(resolver, null, null, content);
    }

    protected void assertMessageHeader(MessageList messageList, int index, String propertyName, Object expectedValue) {
        NormalizedMessage message = (NormalizedMessage) messageList.getMessages().get(index);
        assertNotNull("Message: " + index + " is null!", message);

        Object value = message.getProperty(propertyName);
        assertEquals("property: " + propertyName, expectedValue, value);
    }

    protected void assertMessageBody(MessageList messageList, int index, String expectedXml) throws TransformerException {
        NormalizedMessage message = (NormalizedMessage) messageList.getMessages().get(index);
        assertNotNull("Message: " + index + " is null!", message);

        Source content = message.getContent();
        assertNotNull("Message content: " + index + " is null!", content);
        String value = transformer.toString(content);

        assertEquals("message XML for: " + index, expectedXml, value);
    }

    protected void assertMessageXPath(MessageList messageList, int index, String xpath, String expectedValue) throws TransformerException,
                    ParserConfigurationException, IOException, SAXException {
        NormalizedMessage message = (NormalizedMessage) messageList.getMessages().get(index);
        assertNotNull("Message: " + index + " is null!", message);

        Source content = message.getContent();
        assertNotNull("Message content: " + index + " is null!", content);
        Node node = transformer.toDOMNode(content);

        String value = textValueOfXPath(node, xpath);
        String xmlText = transformer.toString(node);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Message: " + index + " received XML: " + xmlText);
        }

        assertEquals("message XML: " + index + " for xpath: " + xpath + " body was: " + xmlText, expectedValue, value);
    }
}
