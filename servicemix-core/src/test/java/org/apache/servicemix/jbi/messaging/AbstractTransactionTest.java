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
package org.apache.servicemix.jbi.messaging;

import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.servicemix.jbi.RuntimeJBIException;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.AsyncReceiverPojo;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.jencks.factory.GeronimoTransactionManagerFactoryBean;
import org.jencks.factory.TransactionContextManagerFactoryBean;
import org.jencks.factory.TransactionManagerFactoryBean;
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
public abstract class AbstractTransactionTest extends TestCase {

    protected static final int NUM_MESSAGES = 10;
    
    protected TransactionTemplate tt;
    protected TransactionManager tm;
    protected TransactionContextManager tcm;
    protected JBIContainer senderContainer;
    
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        createTransactionLayer();
        senderContainer = createJbiContainer("senderContainer");
    }
    
    protected void tearDown() throws Exception {
    	senderContainer.shutDown();
    }
    
    protected void createTransactionLayer() throws Exception {
        TransactionManagerFactoryBean tmcf = new TransactionManagerFactoryBean();
        tmcf.afterPropertiesSet();
        ExtendedTransactionManager etm = (ExtendedTransactionManager) tmcf.getObject();
        TransactionContextManagerFactoryBean tcmfb = new TransactionContextManagerFactoryBean();
        tcmfb.setTransactionManager(etm);
        tcmfb.afterPropertiesSet();
        tcm = (TransactionContextManager) tcmfb.getObject();
        GeronimoTransactionManagerFactoryBean gtmfb = new GeronimoTransactionManagerFactoryBean();
        gtmfb.setTransactionContextManager(tcm);
        gtmfb.afterPropertiesSet();
        tm = (TransactionManager) gtmfb.getObject();
        tt = new TransactionTemplate(new JtaTransactionManager((UserTransaction) tm));
    }
    
    protected JBIContainer createJbiContainer(String name) throws Exception {
    	JBIContainer container = new JBIContainer();
        container.setTransactionManager(tm);
        container.setName(name);
        container.setFlow(createFlow());
        container.setAutoEnlistInTransaction(true);
        container.setMonitorInstallationDirectory(false);
        container.init();
        container.start();
        return container;
    }
    
    protected abstract Flow createFlow();
    
    protected void runSimpleTest(final boolean syncSend, final boolean syncReceive) throws Exception {
        final SenderComponent sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));
        final Receiver receiver;
        if (syncReceive) {
        	receiver = new ReceiverComponent();
        } else {
        	receiver = new AsyncReceiverPojo();
        }

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver));
        
        tt.execute(new TransactionCallback() {
	  		public Object doInTransaction(TransactionStatus status) {
                try {
                    sender.sendMessages(NUM_MESSAGES, syncSend);
                } catch (JBIException e) {
                    throw new RuntimeJBIException(e);
                }
	  			return null;
	  		}
        });
        receiver.getMessageList().assertMessagesReceived(NUM_MESSAGES);
    }
    
    public void testSyncSendSyncReceive() throws Exception {
        runSimpleTest(true, true);
    }

    public void testAsyncSendSyncReceive() throws Exception {
    	runSimpleTest(false, true);
    }

    public void testSyncSendAsyncReceive() throws Exception {
        runSimpleTest(true, false);
    }

    public void testAsyncSendAsyncReceive() throws Exception {
    	runSimpleTest(false, false);
    }

}
