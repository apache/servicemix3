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
package org.apache.servicemix.examples;

import javax.jbi.component.Component;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.ComponentAdaptor;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.tck.MessageList;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.Sender;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * A base class for spring related test cases
 *
 * @version $Revision$
 */
public abstract class AbstractSpringTestSupport extends TestCase {
    protected SpringJBIContainer jbi;
    protected AbstractXmlApplicationContext context;
    protected int messageCount = 1;

    protected void setUp() throws Exception {

        context = createBeanFactory();
        //context.setXmlValidating(false);

        // lets force the JBI container to be constructed first
        jbi = (SpringJBIContainer) context.getBean("jbi");
        assertNotNull("JBI Container not found in spring!", jbi);

    }
    
    protected void tearDown() throws Exception {
        if (context != null) {
            context.close();
        }
    }

    protected abstract AbstractXmlApplicationContext createBeanFactory();

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        Sender sender = getSender();
        assertNotNull(sender);
        assertNotNull(getReceiver());

        sender.sendMessages(messageCount);

        MessageList messageList = getReceivedMessageList();
        messageList.assertMessagesReceived(messageCount);
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

    protected Object getBean(String name) {
        Object value = jbi.getBean(name);
        if (value == null) {
            value = context.getBean(name);
        }
        assertNotNull(name + " not found in JBI container!", value);
        return value;
    }
}
