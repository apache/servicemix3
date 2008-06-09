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
package org.apache.servicemix.common;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.component.Component;
import javax.xml.namespace.QName;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.seda.SedaFlow;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.jencks.GeronimoPlatformTransactionManager;
import junit.framework.TestCase;

public class NewTransactionsTest extends TestCase {

    protected JBIContainer jbi;
    protected TransactionManager txManager;
    protected Component component;
    protected ServiceMixClient client;
    protected Exception exceptionToThrow;
    protected boolean exceptionShouldRollback;

    protected void setUp() throws Exception {
        exceptionToThrow = null;
        exceptionShouldRollback = false;

        txManager = new GeronimoPlatformTransactionManager();

        jbi = new JBIContainer();
        jbi.setFlows(new Flow[] { new SedaFlow() });
        jbi.setEmbedded(true);
        jbi.setUseMBeanServer(false);
        jbi.setTransactionManager(txManager);
        jbi.setAutoEnlistInTransaction(true);
        jbi.setUseNewTransactionModel(true);
        jbi.init();
        jbi.start();
        component = new TestComponent();
        jbi.activateComponent(component, "test");
        client = new DefaultServiceMixClient(jbi);
    }

    protected void tearDown() throws Exception {
        jbi.shutDown();
    }

    public void testTxOkAsync() throws Exception {
        txManager.begin();
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("service"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        me.setProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME, txManager.suspend());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        client.send(me);
        me = (InOnly) client.receive(1000);
        assertNotNull(me);
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        txManager.resume((Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        txManager.commit();
    }

    public void testTxOkSync() throws Exception {
        txManager.begin();
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("service"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        me.setProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME, txManager.suspend());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        boolean ok = client.sendSync(me, 1000);
        assertTrue(ok);
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        txManager.resume((Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        txManager.commit();
    }

    public void testTxExceptionAsync() throws Exception {
        exceptionToThrow = new Exception("Business exception");
        txManager.begin();
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("service"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        me.setProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME, txManager.suspend());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        client.send(me);
            me = (InOnly) client.receive(1000);
        assertNotNull(me);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        txManager.resume((Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        txManager.commit();
    }

    public void testTxExceptionSync() throws Exception {
        exceptionToThrow = new Exception("Business exception");
        txManager.begin();
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("service"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        me.setProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME, txManager.suspend());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        boolean ok = client.sendSync(me, 1000);
        assertTrue(ok);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        txManager.resume((Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        txManager.commit();
    }

    public void testTxExceptionRollbackAsync() throws Exception {
        exceptionToThrow = new Exception("Business exception");
        exceptionShouldRollback = true;
        txManager.begin();
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("service"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        me.setProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME, txManager.suspend());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        client.send(me);
        me = (InOnly) client.receive(10000);
        assertNotNull(me);
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        txManager.resume((Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME));
        assertEquals(Status.STATUS_MARKED_ROLLBACK, txManager.getStatus());
        txManager.rollback();
    }

    public void testTxExceptionRollbackSync() throws Exception {
        exceptionToThrow = new RuntimeException("Runtime exception");
        exceptionShouldRollback = true;
        txManager.begin();
        InOnly me = client.createInOnlyExchange();
        me.setService(new QName("service"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        assertEquals(Status.STATUS_ACTIVE, txManager.getStatus());
        me.setProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME, txManager.suspend());
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        boolean ok = client.sendSync(me, 1000);
        assertTrue(ok);
        assertEquals(Status.STATUS_NO_TRANSACTION, txManager.getStatus());
        txManager.resume((Transaction) me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME));
        assertEquals(Status.STATUS_MARKED_ROLLBACK, txManager.getStatus());
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        txManager.rollback();
    }

    protected class TestComponent extends BaseComponent {
        public TestComponent() {
            super();
        }
        protected BaseLifeCycle createLifeCycle() {
            return new TestLifeCycle();
        }

        protected class TestLifeCycle extends BaseLifeCycle {
            protected ServiceUnit su;
            public TestLifeCycle() {
                super(TestComponent.this);
            }
            protected void doInit() throws Exception {
                super.doInit();
                su = new ServiceUnit();
                su.setComponent(component);
                TestEndpoint ep = new TestEndpoint();
                ep.setService(new QName("service"));
                ep.setEndpoint("endpoint");
                ep.setServiceUnit(su);
                su.addEndpoint(ep);
                getRegistry().registerServiceUnit(su);
            }
            protected void doStart() throws Exception {
                super.doStart();
                su.start();
            }
            protected void doStop() throws Exception {
                super.doStop();
                su.stop();
            }
            protected boolean exceptionShouldRollbackTx(Exception e) {
                return exceptionShouldRollback;
            }
        }

        protected class TestEndpoint extends ProviderEndpoint {
            public void process(MessageExchange exchange) throws Exception {
                if (exceptionToThrow != null) {
                    throw exceptionToThrow;
                }
                exchange.setStatus(ExchangeStatus.DONE);
                send(exchange);
            }
        }
    }

}
