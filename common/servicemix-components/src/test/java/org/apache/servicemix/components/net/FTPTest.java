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
package org.apache.servicemix.components.net;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

/**
 * Warning: this test case requires an FTP server running on localhost with user / password
 * of [servicemix/rocks].
 * <p/>
 * For details of how to set this up on OS X see
 * <a href="http://www.ldml.com/services/support/macosx/ftpUserCreate.html">this tutorial</a>
 *
 * @version $Revision$
 */
public class FTPTest extends TestSupport {

    public void testSendUsingMessageContentAndUniquelyGeneratedName() throws Exception {

        // START SNIPPET: content
        InOnly exchange = client.createInOnlyExchange();
        NormalizedMessage message = exchange.getInMessage();

        message.setContent(new StringSource("<hello>world!</hello>"));

        client.sendSync(exchange);
        // END SNIPPET: content
    }

    public void testSendUsingMessageProperties() throws Exception {

        // START SNIPPET: properties
        InOnly exchange = client.createInOnlyExchange();
        NormalizedMessage message = exchange.getInMessage();

        message.setProperty("org.apache.servicemix.file.name", "cheese.txt");
        message.setProperty("org.apache.servicemix.file.content", "Hello World!");

        client.sendSync(exchange);
        // END SNIPPET: properties
    }

    public void testSendUsingMessageContentAndExpressionName() throws Exception {

        QName service = new QName("http://servicemix.org/cheese/", "ftpSenderWithExpression");
        ServiceNameEndpointResolver resolver = new ServiceNameEndpointResolver(service);

        InOnly exchange = client.createInOnlyExchange(resolver);
        NormalizedMessage message = exchange.getInMessage();

        message.setContent(new StringSource("<order id='abc123'><customer>Duff Beer</customer><value>599.99</value></order>"));

        client.sendSync(exchange);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/net/ftp.xml");
    }

}
