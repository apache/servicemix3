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
import java.util.Date;

/**
 * @version $Revision$
 */
public class XsltSplitTest extends TestSupport {

    public void testUseXsltAsRouter() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "transformer");

        sendMessages(service, 20);
        assertMessagesReceived(20);

        MessageList messageList1 = assertMessagesReceived("service1", 20);
        MessageList messageList2 = assertMessagesReceived("service2", 20);
        MessageList messageList3 = assertMessagesReceived("service3", 40);

        assertMessageXPath(messageList1, 0, "/cheese", "Edam");
        assertMessageXPath(messageList1, 1, "/cheese", "Edam");
        assertMessageXPath(messageList1, 19, "/cheese", "Edam");
        assertMessageXPath(messageList1, 0, "/cheese/@id", "1");
        assertMessageXPath(messageList1, 1, "/cheese/@id", "2");

        assertMessageXPath(messageList2, 0, "/beer", "Stella");
        assertMessageXPath(messageList2, 19, "/beer", "Stella");

        assertMessageXPath(messageList3, 0, "/lineitem", "Beer");
        assertMessageXPath(messageList3, 1, "/lineitem", "Food");
        assertMessageXPath(messageList3, 38, "/lineitem", "Beer");
        assertMessageXPath(messageList3, 39, "/lineitem", "Food");
    }

    protected String createMessageXmlText(int index) {
        return "<foo id='" + index + "' sent='" + new Date() + "'>"
            + "<cheese>Edam</cheese><beer>Stella</beer><lineitem>Beer</lineitem><lineitem>Food</lineitem></foo>";
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/xslt/split.xml");
    }

}
