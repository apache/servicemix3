/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
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
package org.apache.servicemix.jbi.nmr.flow.jms;

import org.activemq.broker.BrokerService;
import org.activemq.xbean.BrokerFactoryBean;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.jms.JMSFlow;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.springframework.core.io.ClassPathResource;

import junit.framework.TestCase;

/**
 *
 * JMSFlowTest
 */
public class JMSFlowTest extends TestCase {
    JBIContainer senderContainer = new JBIContainer();
    JBIContainer receiverContainer = new JBIContainer();
    private SenderComponent sender;
    private ReceiverComponent receiver;
    private static final int NUM_MESSAGES = 10;
    protected BrokerService broker;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
       
        BrokerFactoryBean bfb = new BrokerFactoryBean(new ClassPathResource("org/apache/servicemix/jbi/nmr/flow/jca/broker.xml"));
        bfb.afterPropertiesSet();
        broker = bfb.getBroker();
        broker.start();
        senderContainer.setName("senderContainer");
        senderContainer.setFlowName("jms?jmsURL=tcp://localhost:61216");
        senderContainer.init();
        senderContainer.start();
        Object senderFlow = senderContainer.getFlow();
        assertTrue(senderFlow instanceof JMSFlow);
        
        
        receiverContainer.setName("receiverContainer");
        receiverContainer.setFlowName("jms?jmsURL=tcp://localhost:61216");
        receiverContainer.init();
        receiverContainer.start();
        Object receiverFlow = receiverContainer.getFlow();
        assertTrue(receiverFlow instanceof JMSFlow);

        Thread.sleep(2000);
        
        receiver = new ReceiverComponent();
        sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver));

        
        Thread.sleep(2000);
    }
    
    protected void tearDown() throws Exception{
        super.tearDown();
        senderContainer.shutDown();
        receiverContainer.shutDown();
        broker.stop();
    }
    
    public void testInOnly() throws Exception {
      sender.sendMessages(NUM_MESSAGES);
      Thread.sleep(3000);
      receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }

    public void testClusteredInOnly() throws Exception {
        final SenderComponent sender = new SenderComponent();
        final ReceiverComponent receiver1 =  new ReceiverComponent();
        final ReceiverComponent receiver2 =  new ReceiverComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver2));
        Thread.sleep(1000);

        sender.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();
        
        senderContainer.deactivateComponent("receiver");
        Thread.sleep(1000);
        
        sender.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        assertFalse(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();
        
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.deactivateComponent("receiver");
        Thread.sleep(1000);
        
        sender.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertFalse(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();
    }
    
}
