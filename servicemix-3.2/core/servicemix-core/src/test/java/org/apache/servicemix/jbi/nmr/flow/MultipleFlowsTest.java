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
package org.apache.servicemix.jbi.nmr.flow;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.Sender;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class MultipleFlowsTest extends TestCase {

    private SpringJBIContainer localContainer;
    private SpringJBIContainer remoteContainer;
    
    private Sender localSender;
    private Sender remoteSender;
    private Sender clusteredSender;
    
    private Receiver localReceiver;
    private Receiver remoteReceiver;
    private Receiver clusteredReceiver1;
    private Receiver clusteredReceiver2;
    
    private AbstractXmlApplicationContext context;
    
    private int messageCount = 100;
    
    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("org/apache/servicemix/jbi/nmr/flow/multiple-flows.xml");
        localContainer = (SpringJBIContainer) context.getBean("local");
        remoteContainer = (SpringJBIContainer) context.getBean("remote");
        localSender = (Sender) localContainer.getBean("localSender");
        remoteSender = (Sender) localContainer.getBean("remoteSender");
        clusteredSender = (Sender) localContainer.getBean("clusteredSender");
        localReceiver = (Receiver) localContainer.getBean("localReceiver");
        remoteReceiver = (Receiver) remoteContainer.getBean("remoteReceiver");
        clusteredReceiver1 = (Receiver) localContainer.getBean("clusteredReceiver");
        clusteredReceiver2 = (Receiver) remoteContainer.getBean("clusteredReceiver");
        Thread.sleep(5000);
    }
    
    protected void tearDown() throws Exception {
        context.close();
    }
    
    public void test() throws Exception {
        // Local
        localSender.sendMessages(messageCount);
        localReceiver.getMessageList().assertMessagesReceived(messageCount);

        // Remote
        remoteSender.sendMessages(messageCount);
        remoteReceiver.getMessageList().assertMessagesReceived(messageCount);

        // Clustered
        clusteredSender.sendMessages(messageCount);
        long t0 = System.currentTimeMillis();
        int n1 = 0;
        int n2 = 0;
        while (System.currentTimeMillis() - t0 < 10000) {
            n1 = clusteredReceiver1.getMessageList().getMessageCount();
            n2 = clusteredReceiver2.getMessageList().getMessageCount();
            if (n1 + n2 == messageCount) {
                break;
            }
        }
        assertEquals(messageCount, n1 + n2);
        assertTrue(n1 > 0);
        assertTrue(n2 > 0);
    }

}
