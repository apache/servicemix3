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

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.apache.servicemix.tck.TestSupport;
import org.apache.servicemix.tck.MessageList;

import javax.xml.namespace.QName;

public class OptionalAxisQueriesTest extends TestSupport {

    public void testOptionalAxisQuery() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "transformer");

       String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "    <prod:resolveItemXrefResponse " +
               "           xmlns:prod=\"http://foo.org/prod\" " +
               "           xmlns:xsi=\"http://foo.org/msi\"" +
               "           xmlns:ms=\"http://foo.org/ms\">" +
               "      <prod:response xsi:type=\"wn4:HashMap\">" +
               "        <ms:Keys>" +
               "          <ms:Item xsi:type=\"d:string\">DESCR</ms:Item>" +
               "          <ms:Item xsi:type=\"d:string\">LIST</ms:Item>" +
               "          <ms:Item xsi:type=\"d:string\">XREF</ms:Item>" +
               "          <ms:Item xsi:type=\"d:string\">MSTR</ms:Item>" +
               "        </ms:Keys>" +
               "        <ms:Values>" +
               "          <ms:Item xsi:type=\"d:string\">PPR,CPY,20#,84B,LGL,WE</ms:Item>" +
               "          <ms:Item xsi:type=\"d:double\">17.25</ms:Item>" +
               "          <ms:Item xsi:type=\"d:string\">EXP8514</ms:Item>" +
               "          <ms:Item xsi:type=\"d:string\">10040300</ms:Item>" +
               "        </ms:Values>" +
               "      </prod:response>" +
               "    </prod:resolveItemXrefResponse>";

        // send a custom xml message
        sendMessages(service, 1, message);
        assertMessagesReceived(1);

        MessageList messageList1 = assertMessagesReceived("service1", 1);
        MessageList messageList2 = assertMessagesReceived("service2", 1);
        MessageList messageList3 = assertMessagesReceived("service3", 1);
        MessageList messageList4 = assertMessagesReceived("service4", 1);
        MessageList messageList5 = assertMessagesReceived("service5", 1);
        MessageList messageList6 = assertMessagesReceived("service6", 1);

        assertMessageHeader(messageList1, 0, "foo", "PPR,CPY,20#,84B,LGL,WE");
        assertMessageHeader(messageList2, 0, "foo", "17.25");
        assertMessageHeader(messageList3, 0, "foo", "EXP8514");
        assertMessageHeader(messageList4, 0, "foo", "10040300");
        assertMessageHeader(messageList5, 0, "foo", new Integer(4));
        assertMessageHeader(messageList6, 0, "foo", "PPR,CPY,20#,84B,LGL,WE");

    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/xslt/optional-axis-example.xml");
    }

}
