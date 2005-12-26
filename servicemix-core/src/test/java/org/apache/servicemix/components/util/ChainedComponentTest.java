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
package org.apache.servicemix.components.util;

import org.apache.servicemix.examples.SpringTestSupport;
import org.apache.servicemix.jbi.config.DebugClassPathXmlApplicationContext;
import org.apache.servicemix.tck.MessageList;
import org.springframework.context.support.AbstractXmlApplicationContext;

import javax.jbi.messaging.NormalizedMessage;

import java.util.Iterator;


public class ChainedComponentTest extends SpringTestSupport {

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        super.testSendingAndReceivingMessagesUsingSpring();
        MessageList messageList = getReceivedMessageList();
        for (Iterator iter = messageList.getMessages().iterator(); iter.hasNext();) {
            NormalizedMessage msg = (NormalizedMessage) iter.next();
            assertNotNull(msg.getProperty("prop1"));
            assertNotNull(msg.getProperty("prop2"));
        } 
    }
    
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new DebugClassPathXmlApplicationContext("org/apache/servicemix/components/util/chained-router.xml");
    }

}
