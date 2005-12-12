/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.components.email;

import java.util.Date;
import java.util.List;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class MimeMailTest extends TestSupport {

    public void testSendUsingMessageProperties() throws Exception {

        // START SNIPPET: email
        InOnly exchange = client.createInOnlyExchange();
        NormalizedMessage message = exchange.getInMessage();

        message.setProperty("org.servicemix.email.to", "scm@servicemix.org");
        message.setProperty("org.servicemix.email.from", "junit@servicemix.org");
        message.setProperty("org.servicemix.email.subject", "Hello from JUnit!");
        message.setProperty("org.servicemix.email.text", "Hi from test case: " + getName() + " running at: " + new Date());

        client.send(exchange);
        // END SNIPPET: email

        // lest find the test sender
        StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
        sender.assertMessagesReceived(1);
        List messages = sender.getMessages();
        assertEquals("message size: " + messages, 1, messages.size());

        MimeMessage mail = (MimeMessage) messages.get(0);

        System.out.println("Created message: " + message);

        assertEquals("subject", "Hello from JUnit!", mail.getSubject());
    }

    public void testUsingXPathExpressionsInEmail() throws Exception {

        // START SNIPPET: xpath
        QName xpathSender = new QName("http://servicemix.org/cheese/", "emailSenderWithExpressions");
        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(xpathSender);

        InOnly exchange = client.createInOnlyExchange(resolver);

        Source source = getSourceFromClassPath("request.xml");
        exchange.getInMessage().setContent(source);

        client.send(exchange);
        // END SNIPPET: xpath

        // lest find the test sender
        StubJavaMailSender sender = (StubJavaMailSender) getBean("javaMailSender");
        sender.assertMessagesReceived(1);
        List messages = sender.getMessages();
        assertEquals("message size: " + messages, 1, messages.size());

        MimeMessage message = (MimeMessage) messages.get(0);

        System.out.println("Created message: " + message);

        assertEquals("subject", "Drink a beer James", message.getSubject());
    }



    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/servicemix/components/email/mimeMail.xml");
    }
}
