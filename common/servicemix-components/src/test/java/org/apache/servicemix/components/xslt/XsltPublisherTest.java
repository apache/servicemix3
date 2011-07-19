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
package org.apache.servicemix.components.xslt;

import org.apache.servicemix.tck.MessageList;
import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

import javax.xml.namespace.QName;

/**
 * @version $Revision$
 */
public class XsltPublisherTest extends TestSupport {

    public void testUseXsltAsRouter() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "transformer");

        sendMessages(service, 20);
        assertMessagesReceived(20);

        MessageList messageList1 = assertMessagesReceived("service1", 20);
        MessageList messageList2 = assertMessagesReceived("service2", 20);
        MessageList messageList3 = assertMessagesReceived("service3", 20);

        assertMessageHeader(messageList1, 0, "foo", "hello world!");
        assertMessageHeader(messageList1, 1, "foo", "hello world!");
        assertMessageHeader(messageList1, 19, "foo", "hello world!");

        assertMessageHeader(messageList2, 0, "bar", "1");
        assertMessageHeader(messageList2, 1, "bar", "2");

        assertMessageHeader(messageList3, 0, "bar", "1");
        assertMessageHeader(messageList3, 1, "bar", "2");
        assertMessageHeader(messageList3, 0, "foo", "hello world!");
        assertMessageHeader(messageList3, 1, "foo", "hello world!");
        assertMessageHeader(messageList3, 19, "foo", "hello world!");
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/xslt/publish.xml");
    }

}
