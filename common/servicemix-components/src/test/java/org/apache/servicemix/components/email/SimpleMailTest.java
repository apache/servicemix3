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
package org.apache.servicemix.components.email;

import java.util.Date;
import java.util.List;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.mail.SimpleMailMessage;

/**
 * @version $Revision$
 */
public class SimpleMailTest extends TestSupport {

    private static final transient Logger logger = LoggerFactory.getLogger(SimpleMailTest.class);

    public void testUsingXPathExpressionsInSimpleEmail() throws Exception {

        // START SNIPPET: xpathSimple
        QName xpathSender = new QName("http://servicemix.org/cheese/", "simpleMailSenderWithExpressions");
        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(xpathSender);

        InOnly exchange = client.createInOnlyExchange(resolver);

        Source source = getSourceFromClassPath("request.xml");
        exchange.getInMessage().setContent(source);

        client.send(exchange);
        // END SNIPPET: xpathSimple

        // lest find the test sender
        StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
        sender.assertMessagesReceived(1);

        List messages = sender.getMessages();
        assertEquals("message size: " + messages, 1, messages.size());

        SimpleMailMessage message = (SimpleMailMessage) messages.get(0);

        logger.info("Created message: {}", message);

        assertEquals("text", "Hello there James how are you today?", message.getText());
        assertEquals("from", "james@nowhere.com", message.getFrom());
    }

    public void testUsingPropertyExpressionsInSimpleEmail() throws Exception {
        // START SNIPPET: xpathSimple
        QName xpathSender = new QName("http://servicemix.org/cheese/", "simpleMailSenderWithPropertyExpressions");
        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(xpathSender);

        InOnly exchange = client.createInOnlyExchange(resolver);
        NormalizedMessage message = exchange.getInMessage();

        message.setProperty("to", "scm@servicemix.org");
        message.setProperty("from", "junit@servicemix.org");
        message.setProperty("text", "Hi from test case: " + getName() + " running at: " + new Date());

        client.send(exchange);
        // END SNIPPET: xpathSimple

        // lest find the test sender
        StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
        sender.assertMessagesReceived(1);

        List messages = sender.getMessages();

        assertEquals("message size: " + messages, 1, messages.size());

        SimpleMailMessage mailMessage = (SimpleMailMessage) messages.get(0);

        logger.info("Created message: {}", mailMessage);

        assertEquals("subject", "Subject came from expression", mailMessage.getSubject());
        assertEquals("from", "junit@servicemix.org", mailMessage.getFrom());
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/email/simpleMail.xml");
    }

}
