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
package org.apache.servicemix.jbi.nmr.flow.jca;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.servicemix.jbi.RuntimeJBIException;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.jencks.factory.GeronimoTransactionManagerFactoryBean;
import org.jencks.factory.TransactionContextManagerFactoryBean;
import org.jencks.factory.TransactionManagerFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.jbi.JBIException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class JCAFlowTest extends TestCase {
	private JBIContainer senderContainer = new JBIContainer();
    private JBIContainer receiverContainer = new JBIContainer();
    private BrokerService broker;
    private TransactionTemplate tt;
    private static final int NUM_MESSAGES = 10;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        TransactionManagerFactoryBean tmcf = new TransactionManagerFactoryBean();
        tmcf.afterPropertiesSet();
        ExtendedTransactionManager etm = (ExtendedTransactionManager) tmcf.getObject();
        TransactionContextManagerFactoryBean tcmfb = new TransactionContextManagerFactoryBean();
        tcmfb.setTransactionManager(etm);
        tcmfb.afterPropertiesSet();
        TransactionContextManager tcm = (TransactionContextManager) tcmfb.getObject();
        GeronimoTransactionManagerFactoryBean gtmfb = new GeronimoTransactionManagerFactoryBean();
        gtmfb.setTransactionContextManager(tcm);
        gtmfb.afterPropertiesSet();
        TransactionManager tm = (TransactionManager) gtmfb.getObject();
        tt = new TransactionTemplate(new JtaTransactionManager((UserTransaction) tm));
       
        BrokerFactoryBean bfb = new BrokerFactoryBean(new ClassPathResource("org/apache/servicemix/jbi/nmr/flow/jca/broker.xml"));
        bfb.afterPropertiesSet();
        broker = bfb.getBroker();
        broker.start();
        
        JCAFlow senderFlow = new JCAFlow();
        senderFlow.setJmsURL("tcp://localhost:61216");
        senderFlow.setTransactionContextManager(tcm);
        senderContainer.setTransactionManager(tm);
        senderContainer.setName("senderContainer");
        senderContainer.setFlow(senderFlow);
        senderContainer.setMonitorInstallationDirectory(false);
        senderContainer.init();
        senderContainer.start();
        
        
        JCAFlow receiverFlow = new JCAFlow();
        receiverFlow.setJmsURL("tcp://localhost:61216");
        receiverFlow.setTransactionContextManager(tcm);
        receiverContainer.setTransactionManager(tm);
        receiverContainer.setName("receiverContainer");
        receiverContainer.setFlow(receiverFlow);
        receiverContainer.setMonitorInstallationDirectory(false);
        receiverContainer.init();
        receiverContainer.start();
    }
    
    protected void tearDown() throws Exception{
        super.tearDown();
        senderContainer.shutDown();
        receiverContainer.shutDown();
        broker.stop();
    }
    
    public void testInOnly() throws Exception {
        final SenderComponent sender = new SenderComponent();
        final ReceiverComponent receiver =  new ReceiverComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver));

        Thread.sleep(5000);

        sender.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }
    
    public void testTxInOnly() throws Exception {
        final SenderComponent sender = new SenderComponent();
        final ReceiverComponent receiver =  new ReceiverComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver));

        Thread.sleep(5000);
        
    	senderContainer.setAutoEnlistInTransaction(true);
        tt.execute(new TransactionCallback() {
	  		public Object doInTransaction(TransactionStatus status) {
                try {
                    sender.sendMessages(NUM_MESSAGES);
                } catch (JBIException e) {
                    throw new RuntimeJBIException(e);
                }
	  			return null;
	  		}
        });
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
