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
package org.apache.servicemix.jbi.nmr.flow.jms;

import javax.transaction.TransactionManager;

import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;
import org.jencks.factory.GeronimoTransactionManagerFactoryBean;
import org.jencks.factory.TransactionContextManagerFactoryBean;
import org.jencks.factory.TransactionManagerFactoryBean;

public class StatelessJcaFlowTest extends StatelessJmsFlowTest {

    private TransactionContextManager tcm;
    private TransactionManager tm;
    
    protected void setUp() throws Exception {
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
        super.setUp();
    }
    
    protected JBIContainer createContainer(String name) throws Exception {
        JBIContainer container = new JBIContainer();
        container.setName(name);
        JCAFlow flow = new JCAFlow();
        flow.setJmsURL("tcp://localhost:61616");
        flow.setTransactionContextManager(tcm);
        container.setTransactionManager(tm);
        container.setFlow(flow);
        container.setUseMBeanServer(false);
        container.setEmbedded(true);
        container.init();
        container.start();
        return container;
    }
    

}
