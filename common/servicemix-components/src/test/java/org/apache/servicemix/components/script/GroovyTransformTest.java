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
package org.apache.servicemix.components.script;

import java.util.List;

import javax.jbi.component.Component;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.components.util.ComponentAdaptor;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.tck.MessageList;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.Sender;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class GroovyTransformTest extends org.apache.servicemix.tck.SpringTestSupport {

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        Sender sender = getSender();

        sender.sendMessages(messageCount);

        MessageList messageList = getReceivedMessageList();
        messageList.assertMessagesReceived(messageCount);

        // lets now look into the received messages
        List list = messageList.getMessages();
        NormalizedMessage message = (NormalizedMessage) list.get(0);
        Object property = message.getProperty("foo");
        assertEquals("Message 'foo' header", "hello", property);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/script/groovy-transform.xml");
    }

    protected Sender getSender() {
        Object cmp = getComponent("sender");
        if (cmp instanceof ComponentAdaptor) {
            cmp = ((ComponentAdaptor) cmp).getLifeCycle();
        }
        return (Sender) cmp;
    }

    protected Receiver getReceiver() {
        Object cmp = getComponent("receiver");
        if (cmp instanceof ComponentAdaptor) {
            cmp = ((ComponentAdaptor) cmp).getLifeCycle();
        }
        return (Receiver) cmp;
    }

    protected MessageList getReceivedMessageList() {
        return getReceiver().getMessageList();
    }
    
    protected Component getComponent(String name) {
        ComponentMBeanImpl lcc = jbi.getComponent(name);
        return lcc != null ? lcc.getComponent() : null;
    }

}
