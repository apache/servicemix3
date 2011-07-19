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

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class EmailTest extends TestSupport {

    public void testSendUsingMessageProperties() throws Exception {

        // START SNIPPET: email
        InOnly exchange = client.createInOnlyExchange();
        NormalizedMessage message = exchange.getInMessage();

        message.setProperty("org.apache.servicemix.email.to", "scm@servicemix.org");
        message.setProperty("org.apache.servicemix.email.from", "junit@servicemix.org");
        message.setProperty("org.apache.servicemix.email.subject", "Hello from JUnit!");
        message.setProperty("org.apache.servicemix.email.text", "Hi from test case: " + getName() + " running at: " + new Date());

        client.sendSync(exchange);
        // END SNIPPET: email

    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/email/example.xml");
    }

}
