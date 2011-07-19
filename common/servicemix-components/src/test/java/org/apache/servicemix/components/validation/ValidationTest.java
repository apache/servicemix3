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
package org.apache.servicemix.components.validation;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ValidationTest extends SpringTestSupport {

    private static transient Logger logger = LoggerFactory.getLogger(ValidationTest.class);

    protected ServiceMixClient client;
    protected Receiver receiver;

	protected void setUp() throws Exception {
		super.setUp();
        SpringJBIContainer jbi = (SpringJBIContainer) getBean("jbi");
        receiver = (Receiver) jbi.getBean("receiver");
    }

    public void testValidMessage() throws Exception {
    	client = (ServiceMixClient) getBean("defaultErrorHandlerClient");
    	
        InOut exchange = client.createInOutExchange();
        exchange.getInMessage().setContent(getSourceFromClassPath("requestValid.xml"));
        client.sendSync(exchange);

        NormalizedMessage out = exchange.getOutMessage();
        Fault fault = exchange.getFault();
        Exception error = exchange.getError();

        assertEquals("error", null, error);
        assertEquals("fault", null, fault);

        assertNotNull("Should have an out message", out);
    }

    public void testInvalidMessage() throws Exception {
    	client = (ServiceMixClient) getBean("defaultErrorHandlerClient");

    	InOut exchange = client.createInOutExchange();
        exchange.getInMessage().setContent(getSourceFromClassPath("requestInvalid.xml"));
        client.sendSync(exchange);

        NormalizedMessage out = exchange.getOutMessage();
        Fault fault = exchange.getFault();
        Exception error = exchange.getError();

        assertEquals("out", null, out);
        assertNotNull("Should have a fault", fault);

        logger.info("error is: {}", error);

        logger.info("Fault is...");
        logger.info(transformer.toString(fault.getContent()));

        // TODO?
        //assertEquals("error", null, error);
    }

    public void testInvalidMessageWithMessageAwareErrorHandler() throws Exception {
    	client = (ServiceMixClient) getBean("messageAwareErrorHandlerClient");
    	
    	InOut exchange = client.createInOutExchange();
        exchange.getInMessage().setContent(getSourceFromClassPath("requestInvalid.xml"));
        client.sendSync(exchange);

        NormalizedMessage out = exchange.getOutMessage();
        Fault fault = exchange.getFault();
        Exception error = exchange.getError();

        assertEquals("out", null, out);
        assertNotNull("Should have a fault", fault);

        logger.info("error is: {}", error);

        logger.info("Fault is...");
        logger.info(transformer.toString(fault.getContent()));

        // TODO?
        //assertEquals("error", null, error);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/validation/example.xml");
    }

}
