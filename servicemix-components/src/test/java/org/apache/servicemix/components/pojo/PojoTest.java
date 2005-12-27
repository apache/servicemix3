/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.components.pojo;

import org.apache.servicemix.tck.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class PojoTest extends SpringTestSupport {

    public void testSendAndReceiveOfMessages() throws Exception {
        MySender sender = (MySender) getBean("sender");
        sender.sendMessages(messageCount);

        MyReceiver receiver = (MyReceiver) getBean("receiver");
        assertMessagesReceived(receiver.getMessageList(), messageCount);
    }

    public void testPerfSendAndReceiveOfMessages() throws Exception {
    	MySender sender = (MySender) getBean("sender");
    	MyReceiver receiver = (MyReceiver) getBean("receiver");
    	sender.sendMessages(100);
    	assertMessagesReceived(receiver.getMessageList(), 100);
    	receiver.getMessageList().flushMessages();

    	int messageCount = 500;
    	long start = System.currentTimeMillis();
    	sender.sendMessages(messageCount);
    	assertMessagesReceived(receiver.getMessageList(), messageCount);
    	long end = System.currentTimeMillis();

    	System.err.println((end - start) + " ms to send " + messageCount + " messages");
    }
    
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/pojo/example.xml");
    }
}
