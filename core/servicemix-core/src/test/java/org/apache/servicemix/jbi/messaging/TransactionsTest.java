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
package org.apache.servicemix.jbi.messaging;

import java.sql.Connection;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.nmr.flow.Flow;
import org.apache.servicemix.jbi.nmr.flow.jca.JCAFlow;
import org.apache.servicemix.jbi.nmr.flow.seda.SedaFlow;
import org.apache.servicemix.store.Store;
import org.apache.servicemix.store.jdbc.JdbcStoreFactory;
import org.apache.servicemix.tck.ExchangeCompletedListener;
import org.jencks.GeronimoPlatformTransactionManager;
import org.jencks.factory.ConnectionManagerFactoryBean;
import org.tranql.connector.AllExceptionsAreFatalSorter;
import org.tranql.connector.jdbc.AbstractXADataSourceMCF;

public class TransactionsTest extends TestCase {

    public static final long TIMEOUT = 1000;

    private static final int ACTIVEMQ_PORT = Integer.parseInt(System.getProperty("activemq.port"));
    private static final String ACTIVEMQ_URL = "tcp://localhost:" + ACTIVEMQ_PORT;

    private JBIContainer jbi;
    private BrokerService broker;
    private GeronimoPlatformTransactionManager tm;
    private ServiceMixClient client;
    private DataSource dataSource;
    private Connection connection;
    private Store store;
    private ExchangeCompletedListener listener;
    
    protected void setUp() throws Exception {

        // Create an AMQ broker
        broker = new BrokerService();
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.addConnector(ACTIVEMQ_URL);
        broker.start();
        
        tm = new GeronimoPlatformTransactionManager();
        
        // Create an embedded database for testing tx results when commit / rollback
        ConnectionManagerFactoryBean factory = new ConnectionManagerFactoryBean();
        factory.setTransactionManager(tm);
        factory.setTransaction("xa");
        factory.afterPropertiesSet();
        ConnectionManager cm = (ConnectionManager) factory.getObject();
        ManagedConnectionFactory mcf = new DerbyDataSourceMCF("target/testdb");
        dataSource = (DataSource) mcf.createConnectionFactory(cm);
        
        connection = dataSource.getConnection();
        
        JdbcStoreFactory storeFactory = new JdbcStoreFactory();
        storeFactory.setDataSource(dataSource);
        storeFactory.setTransactional(true);
        store = storeFactory.open("store");
        
        jbi = new JBIContainer();
        jbi.setFlows(new Flow[] {new SedaFlow(), new JCAFlow(ACTIVEMQ_URL) });
        jbi.setEmbedded(true);
        jbi.setUseMBeanServer(false);
        jbi.setCreateMBeanServer(false);
        jbi.setTransactionManager(tm);
        jbi.setAutoEnlistInTransaction(true);
        listener = new ExchangeCompletedListener();
        jbi.addListener(listener);
        jbi.init();
        jbi.start();
        
        client = new DefaultServiceMixClient(jbi);
    }
    
    protected void tearDown() throws Exception {
        listener.assertExchangeCompleted();
        jbi.shutDown();
        Thread.sleep(100);
        broker.stop();
        connection.close();
    }
    
    protected InOnly createInOnly() throws Exception {
        InOnly me = client.createInOnlyExchange();
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        me.setService(new QName("service"));
        return me;
    }
    
    protected InOut createInOut() throws Exception {
        InOut me = client.createInOutExchange();
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        me.setService(new QName("service"));
        return me;
    }
    
    public void testInOnlyAsyncSendAndListener() throws Exception {
        jbi.activateComponent(new Listener(false, false), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        me = client.receive(TIMEOUT);
        assertNotNull(me);
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlyAsyncSendAndListenerWithRollback() throws Exception {
        jbi.activateComponent(new Listener(false, true), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNull(client.receive(TIMEOUT));
        
        assertNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlySyncSendAndListener() throws Exception {
        jbi.activateComponent(new Listener(false, false), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlySyncSendAndListenerWithProviderRollback() throws Exception {
        jbi.activateComponent(new Listener(false, true), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlySyncSendAndListenerWithConsumerRollback() throws Exception {
        jbi.activateComponent(new Listener(false, false), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlyAsyncSendAndPoll() throws Exception {
        jbi.activateComponent(new Async(false, false), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        me = client.receive(TIMEOUT);
        assertNotNull(me);
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlyAsyncSendAndPollWithRollback() throws Exception {
        jbi.activateComponent(new Async(false, true), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNull(client.receive(TIMEOUT));
        
        assertNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlySyncSendAndPoll() throws Exception {
        jbi.activateComponent(new Async(false, false), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlySyncSendAndPollWithProviderRollback() throws Exception {
        jbi.activateComponent(new Async(false, true), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNull(store.load(me.getExchangeId()));
    }
    
    public void testInOnlySyncSendAndPollWithConsumerRollback() throws Exception {
        jbi.activateComponent(new Async(false, false), "target");
        
        MessageExchange me = createInOnly();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.setRollbackOnly();
        assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
        assertEquals(ExchangeStatus.DONE, me.getStatus());
        assertTrue(me.isTransacted());
        tm.rollback();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNull(store.load(me.getExchangeId()));
    }

    public void testInOutAsyncSendAndAsyncSendAndListener() throws Exception {
        jbi.activateComponent(new Listener(false, false), "target");
        
        MessageExchange me = createInOut();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        me = client.receive(TIMEOUT);
        assertNotNull(me);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertTrue(me.isTransacted());
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.done(me);
        
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNotNull(store.load(me.getExchangeId()));
    }

    /*
     * NOT SUPPORTED
     *
    public void testInOutAsyncSendAndSyncSendAndListener() throws Exception {
        jbi.activateComponent(new Listener(true, false), "target");
        
        MessageExchange me = createInOut();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        me = client.receive(TIMEOUT);
        assertNotNull(me);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertTrue(me.isTransacted());
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.done(me);
        
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNotNull(store.load(me.getExchangeId()));
    }
    */
    
    /*
     * NOT SUPPORTED
     *
    public void testInOutSyncSendAndAsyncSendAndListener() throws Exception {
        jbi.activateComponent(new Listener(false, false), "target");
        
        MessageExchange me = createInOut();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertTrue(me.isTransacted());
        client.done(me);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    */
    
    public void testInOutSyncSendAndSyncSendAndListener() throws Exception {
        jbi.activateComponent(new Listener(true, false), "target");
        
        MessageExchange me = createInOut();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertTrue(me.isTransacted());
        client.done(me);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    public void testInOutAsyncSendAndAsyncSendAndPoll() throws Exception {
        jbi.activateComponent(new Async(false, false), "target");
        
        MessageExchange me = createInOut();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.send(me);
        assertNull(client.receive(TIMEOUT));
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        me = client.receive(TIMEOUT);
        assertNotNull(me);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertTrue(me.isTransacted());
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.done(me);
        
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    public void testInOutSyncSendAndSyncSendAndPoll() throws Exception {
        jbi.activateComponent(new Async(true, false), "target");
        
        MessageExchange me = createInOut();
        tm.begin();
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        client.sendSync(me, TIMEOUT);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        assertTrue(me.isTransacted());
        client.done(me);
        assertEquals(Status.STATUS_ACTIVE, tm.getStatus());
        tm.commit();
        assertEquals(Status.STATUS_NO_TRANSACTION, tm.getStatus());
        
        assertNotNull(store.load(me.getExchangeId()));
    }
    
    protected class Async extends ComponentSupport implements Runnable {
        private boolean sync;
        private boolean rollback;
        private Thread runner;
        private boolean running;
        public Async(boolean sync, boolean rollback) {
            this.sync = sync;
            this.rollback = rollback;
            setService(new QName("service"));
            setEndpoint("endpoint");
        }
        public synchronized void start() throws JBIException {
            if (!running) {
                running = true;
                runner = new Thread(this);
                runner.start();
            }
        }
        public void run() {
            while (running) {
                try {
                    DeliveryChannel deliveryChannel = getContext().getDeliveryChannel();
                    MessageExchange messageExchange = deliveryChannel.accept();
                    process(messageExchange);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        public synchronized void stop() throws JBIException {
            running = false;
        }
        protected void process(MessageExchange exchange) throws Exception {
            if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
                return;
            }
            try {
                store.store(exchange.getExchangeId(), exchange);
            } catch (Exception e) {
                throw new MessagingException(e);
            }
            if (rollback) {
                try {
                    tm.setRollbackOnly();
                } catch (Exception e) {
                    throw new MessagingException(e);
                }
            }
            if (exchange instanceof InOnly) {
                exchange.setStatus(ExchangeStatus.DONE);
                getDeliveryChannel().send(exchange);
            } else {
                NormalizedMessage msg = exchange.createMessage();
                msg.setContent(exchange.getMessage("in").getContent());
                exchange.setMessage(msg, "out");
                if (sync) {
                    getDeliveryChannel().sendSync(exchange);
                } else {
                    getDeliveryChannel().send(exchange);
                }
            }
        }
    }
    
    protected class Listener extends ComponentSupport implements MessageExchangeListener {
        private boolean sync;
        private boolean rollback;
        public Listener(boolean sync, boolean rollback) {
            this.sync = sync;
            this.rollback = rollback;
            setService(new QName("service"));
            setEndpoint("endpoint");
        }
        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
                return;
            }
            try {
                store.store(exchange.getExchangeId(), exchange);
            } catch (Exception e) {
                throw new MessagingException(e);
            }
            if (rollback) {
                try {
                    tm.setRollbackOnly();
                } catch (Exception e) {
                    throw new MessagingException(e);
                }
            }
            if (exchange instanceof InOnly) {
                exchange.setStatus(ExchangeStatus.DONE);
                getDeliveryChannel().send(exchange);
            } else {
                NormalizedMessage msg = exchange.createMessage();
                msg.setContent(exchange.getMessage("in").getContent());
                exchange.setMessage(msg, "out");
                if (sync) {
                    getDeliveryChannel().sendSync(exchange, TIMEOUT);
                } else {
                    getDeliveryChannel().send(exchange);
                }
            }
        }
        
    }
    
    public static class DerbyDataSourceMCF extends AbstractXADataSourceMCF {
        private static final long serialVersionUID = 7971682207810098396L;
        protected DerbyDataSourceMCF(String dbName) {
            super(createXADS(dbName), new AllExceptionsAreFatalSorter());
        }
        public String getPassword() {
            return null;
        }
        public String getUserName() {
            return null;
        }
        protected static XADataSource createXADS(String dbName) {
            EmbeddedXADataSource xads = new EmbeddedXADataSource();
            xads.setDatabaseName(dbName);
            xads.setCreateDatabase("create");
            return xads;
        }
    }
    
}
