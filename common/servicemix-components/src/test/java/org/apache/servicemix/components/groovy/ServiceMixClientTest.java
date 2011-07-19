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
package org.apache.servicemix.components.groovy;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.api.EndpointResolver;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.tck.Receiver;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ServiceMixClientTest extends TestCase {

    private static transient Logger logger = LoggerFactory.getLogger(ServiceMixClientTest.class);

    protected AbstractXmlApplicationContext context;
    protected SpringJBIContainer container;
    protected ServiceMixClient client;
    protected Receiver receiver;

    protected SourceTransformer transformer = new SourceTransformer();


    // Send methods
    //-------------------------------------------------------------------------
    public void testSendUsingJbiAPIs() throws Exception {

        InOnly exchange = client.createInOnlyExchange();

        NormalizedMessage message = exchange.getInMessage();
        message.setProperty("name", "James");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        QName service = new QName("http://servicemix.org/cheese/", "receiver");
        exchange.setService(service);
        client.send(exchange);

        receiver.getMessageList().assertMessagesReceived(1);
    }

    public void testSendWithErrorUsingJbiAPIs() throws Exception {

        MessageExchange exchange = client.createInOnlyExchange();

        NormalizedMessage message = exchange.getMessage("in");
        message.setProperty("name", "James");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        activateComponent(new ReturnErrorComponent(), "error");
        
        QName service = new QName("error");
        exchange.setService(service);
        client.send(exchange);
        
        exchange = client.receive();
        assertEquals(ExchangeStatus.ERROR, exchange.getStatus());
    }

    public void testSendWithFaultUsingJbiAPIs() throws Exception {

        MessageExchange exchange = client.createRobustInOnlyExchange();

        NormalizedMessage message = exchange.getMessage("in");
        message.setProperty("name", "James");
        message.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        activateComponent(new ReturnFaultComponent(), "fault");
        
        QName service = new QName("fault");
        exchange.setService(service);
        client.send(exchange);
        
        exchange = client.receive();
        assertEquals(ExchangeStatus.ACTIVE, exchange.getStatus());
        assertNotNull(exchange.getFault());
        client.done(exchange);
    }

    public void testSendUsingMapAndPOJOsByServiceName() throws Exception {

        Map properties = new HashMap();
        properties.put("name", "James");

        QName service = new QName("http://servicemix.org/cheese/", "receiver");
        EndpointResolver resolver = client.createResolverForService(service);
        client.send(resolver, null, properties, "<hello>world</hello>");

        receiver.getMessageList().assertMessagesReceived(1);
    }

    public void testSendUsingMapAndPOJOsUsingContainerRouting() throws Exception {

        ServiceMixClient clientNoRouting = (ServiceMixClient) context.getBean("clientWithRouting");

        Map properties = new HashMap();
        properties.put("name", "James");

        clientNoRouting.send(null, null, properties, "<hello>world</hello>");

        receiver.getMessageList().assertMessagesReceived(1);
    }

    public void testSendUsingMapAndPOJOsUsingContainerRoutingWithNoConfiguration() throws Exception {


        try {
            Map properties = new HashMap();
            properties.put("name", "James");

            client.send(null, null, properties, "<hello>world</hello>");
            fail("Should have thrown an exception as we have not wired in any container routing information to this client");
        }
        catch (JBIException e) {
            logger.info("Caught expected exception as we have specified no endpoint resolver: ", e);
            assertNotNull(e);
        }
    }


    // Request methods
    //-------------------------------------------------------------------------
    public void testRequestUsingJbiAPIsByServiceName() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "myService");
        assertRequestUsingJBIAPIs(service);
    }

    public void testRequestUsingMapAndPOJOsByServiceName() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "myService");
        assertRequestUsingMapAndPOJOByServiceName(service);
    }

    public void testRequestUsingPOJOWithXStreamMarshaling() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "myService");

        ServiceMixClient client = (ServiceMixClient) context.getBean("clientWithXStream");

        Map properties = new HashMap();
        properties.put("name", "James");

        EndpointResolver resolver = client.createResolverForService(service);
        TestBean bean = new TestBean();
        bean.setName("James");
        bean.setLength(12);
        bean.getAddresses().addAll(Arrays.asList(new String[] {"London", "LA"}));

        Object response = client.request(resolver, null, properties, bean);

        assertNotNull("Should have returned a non-null response!", response);

        logger.info("Received result: {}", response);
    }



    // Implementation methods
    //-------------------------------------------------------------------------
    protected void assertRequestUsingJBIAPIs(QName service) throws Exception {
        InOut exchange = client.createInOutExchange();

        NormalizedMessage inMessage = exchange.getInMessage();
        inMessage.setProperty("name", "James");
        inMessage.setContent(new StreamSource(new StringReader("<hello>world</hello>")));

        exchange.setService(service);
        boolean answer = client.sendSync(exchange);
        assertTrue("Should have successed", answer);

        NormalizedMessage outMessage = exchange.getOutMessage();
        assertNotNull("outMessage is null!", outMessage);

        assertEquals("foo header", "hello", outMessage.getProperty("foo"));
        logger.info("Received result: {}", outMessage.getContent());
        logger.info("XML is: {}", transformer.toString(outMessage.getContent()));
    }

    protected void assertRequestUsingMapAndPOJOByServiceName(QName service) throws Exception {
        Map properties = new HashMap();
        properties.put("name", "James");

        EndpointResolver resolver = (EndpointResolver)client.createResolverForService(service);
        Object response = client.request(resolver, null, properties, "<hello>world</hello>");

        assertNotNull("Should have returned a non-null response!", response);
        
        logger.info("Received result: {}", response);
    }

    protected void setUp() throws Exception {
        context = createBeanFactory();
        //context.setXmlValidating(false);

        client = (ServiceMixClient) getBean("client");

        // TODO
        //receiver = (Receiver) getBean("receiver");

        container = (SpringJBIContainer) getBean("jbi");
        receiver = (Receiver) container.getBean("receiver");
        assertNotNull("receiver not found in JBI container", receiver);
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
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/groovy/example.xml");

    }

    protected void activateComponent(ComponentSupport comp, String name) throws Exception {
        comp.setService(new QName(name));
        comp.setEndpoint("endpoint");
        container.activateComponent(comp, name);
    }
    
    public static class ReturnErrorComponent extends ComponentSupport implements MessageExchangeListener {

        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                fail(exchange, new Exception());
            }
        }
    }

    public static class ReturnFaultComponent extends ComponentSupport implements MessageExchangeListener {
        
        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                Fault fault = exchange.createFault();
                fault.setContent(new StringSource("<fault/>"));
                fail(exchange, fault);
            }
        }
    }
    
}
