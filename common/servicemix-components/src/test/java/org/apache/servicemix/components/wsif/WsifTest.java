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
package org.apache.servicemix.components.wsif;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class WsifTest extends TestSupport {

    private static transient Logger logger = LoggerFactory.getLogger(WsifTest.class);

    QName serviceName = new QName("http://servicemix.org/cheese/", "checkAvailability");

    public static final int MESSAGES = 2;
    
    public void testUsingXMLMessaging() throws Exception {
        for (int i = 0; i < MESSAGES; i++) {
            String file = "request.xml";
    
            Object answer = requestServiceWithFileRequest(serviceName, file);
            assertTrue("Shoud return a DOM Node: " + answer, answer instanceof Node);
            Node node = (Node) answer;
            logger.info(transformer.toString(node));
    
            String text = textValueOfXPath(node, "/*/*[local-name()='part']").trim();
    
            logger.info("Found value: {}", text);
    
            assertTrue("price text should not be empty", text.length() > 0);
        }
    }

    public void testUsingWSIFStyleJBI() throws Exception {
        for (int i = 0; i < MESSAGES; i++) {
            // START SNIPPET: wsif
            InOut exchange = client.createInOutExchange();
    
            exchange.getInMessage().setProperty("zipCode", "10505");
            client.sendSync(exchange);
    
            NormalizedMessage out = exchange.getOutMessage();
            String result = (String) out.getProperty("result");
    
            logger.info("Found value: {}", result);
            // END SNIPPET: wsif
    
            assertEquals("should have no fault", null, exchange.getFault());
            Exception error = exchange.getError();
            if (error != null) {
                throw error;
            }
            assertEquals("should have no error", null, error);
            assertNotNull("must have an output message!", out);
    
            assertTrue("price text should not be empty", result.length() > 0);
        }
    }

    public void testUsingWSIFStyleJBIWithEarlyErrorHandling() throws Exception {
        for (int i = 0; i < MESSAGES; i++) {
            InOut exchange = client.createInOutExchange();
    
            exchange.getInMessage().setProperty("zipCode", "10505");
            client.sendSync(exchange);
    
            Exception error = exchange.getError();
            if (error != null) {
                throw error;
            }
    
            assertEquals("should have no fault", null, exchange.getFault());
            assertEquals("should have no error", null, error);
    
            NormalizedMessage out = exchange.getOutMessage();
            assertNotNull("must have an output message!", out);
    
            String result = (String) out.getProperty("result");
    
            logger.info("Found value: {}", result);
    
            assertTrue("price text should not be empty", result.length() > 0);
        }
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/wsif/example.xml");
    }

}
