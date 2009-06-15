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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;

import static org.easymock.EasyMock.*;

public class DeliveryChannelImplTest extends TestCase {

    private static final Log LOG = LogFactory.getLog(DeliveryChannelImplTest.class);

    protected JBIContainer container;

    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }

    public void testExchangeFactoryOnOpenChannel() throws Exception {
        // Retrieve a delivery channel
        TestComponent component = new TestComponent(null, null);
        container.activateComponent(new ActivationSpec("component", component));
        DeliveryChannel channel = component.getChannel();
        // test
        MessageExchangeFactory mef = channel.createExchangeFactory();
        assertNotNull(mef);
        assertNotNull(mef.createInOnlyExchange());
    }

    public void testExchangeFactoryOnClosedChannel() throws Exception {
        // Retrieve a delivery channel
        TestComponent component = new TestComponent(null, null);
        container.activateComponent(new ActivationSpec("component", component));
        DeliveryChannel channel = component.getChannel();
        // test
        channel.close();
        MessageExchangeFactory mef = channel.createExchangeFactory();
        assertNotNull(mef);
        try {
            mef.createInOnlyExchange();
            fail("Exchange creation should have failed (JBI: 5.5.2.1.4)");
        } catch (MessagingException e) {
            // expected
        }
    }

    public void testSendSyncOnSameComponent() throws Exception {
        // Retrieve a delivery channel
        TestComponent component = new TestComponent(new QName("service"), "endpoint");
        container.activateComponent(new ActivationSpec("component", component));
        final DeliveryChannel channel = component.getChannel();
        final AtomicBoolean success = new AtomicBoolean(false);
        final AtomicBoolean done = new AtomicBoolean(false);

        // Create another thread
        Thread t = new Thread() {
            public void run() {
                try {
                    InOut me = (InOut) channel.accept(5000);
                    NormalizedMessage nm = me.createMessage();
                    nm.setContent(new StringSource("<response/>"));
                    me.setOutMessage(nm);
                    channel.sendSync(me);
                    success.set(true);
                    done.set(true);
                } catch (MessagingException e) {
                    LOG.error(e.getMessage(), e);
                    success.set(false);
                    done.set(true);
                }
            }
        };
        t.start();

        MessageExchangeFactory factory = channel.createExchangeFactoryForService(new QName("service"));
        InOut me = factory.createInOutExchange();
        NormalizedMessage nm = me.createMessage();
        nm.setContent(new StringSource("<request/>"));
        me.setInMessage(nm);
        channel.sendSync(me);
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        me.setStatus(ExchangeStatus.DONE);
        channel.send(me);

        if (!done.get()) {
            synchronized (done) {
                done.wait(5000);
            }
        }

        assertTrue("Secondary thread didn't finish", done.get());
        assertTrue("Exception in secondary thread", success.get());
    }
    
    public void testAutoEnlistInActiveTx() throws JBIException, SystemException {
        // set up a mock TransactionManager for the container
        final TransactionManager manager = createMock(TransactionManager.class);
        container.setTransactionManager(manager);
        container.setAutoEnlistInTransaction(true);
        
        // create DeliveryChannel and MessageExchange
        final DeliveryChannelImpl channel = createDeliveryChannel();
        MessageExchangeImpl exchange = createMessageExchange(channel);
        
        // auto-enlistment should only occur when Transaction status is ACTIVE
        final Transaction transaction = createMock(Transaction.class);
        expect(manager.getTransaction()).andReturn(transaction);
        expect(transaction.getStatus()).andReturn(Status.STATUS_ACTIVE);
        replay(manager);
        replay(transaction);
        channel.autoEnlistInTx(exchange);
        assertSame(transaction, exchange.getTransactionContext());
    }
    
    public void testNoAutoEnlistInNonActiveTx() throws JBIException, SystemException {
        // set up a mock TransactionManager for the container        
        final TransactionManager manager = createMock(TransactionManager.class);
        container.setTransactionManager(manager);
        container.setAutoEnlistInTransaction(true);
        final Transaction transaction = createMock(Transaction.class);

        // create DeliveryChannel and MessageExchange
        final DeliveryChannelImpl channel = createDeliveryChannel();
        MessageExchangeImpl exchange = createMessageExchange(channel);
        
        // auto-enlistment should not occur when Transaction status is NO_TRANSACTION or any other status (not tested)
        expect(manager.getTransaction()).andReturn(transaction);
        expect(transaction.getStatus()).andReturn(Status.STATUS_NO_TRANSACTION);
        replay(manager);
        replay(transaction);
        channel.autoEnlistInTx(exchange);
        assertNull(exchange.getTransactionContext());
    }
    
    public void testCancelPendingExchanges() throws Exception {
        final DeliveryChannelImpl channel = createDeliveryChannel();
        final MessageExchangeImpl exchange = createMessageExchange(channel);
        
        final CountDownLatch pending = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            public void run() {
                try {
                    channel.sendSync(exchange);
                    pending.countDown();
                } catch (MessagingException e) {
                    // no need to worry about this
                }
            }            
        });
        
        // let's wait for a second until the exchange got sent
        pending.await(1, TimeUnit.SECONDS);
        
        // now let's cancel the pending exchanges 
        channel.cancelPendingExchanges();
        pending.await(1, TimeUnit.SECONDS);
        assertEquals("There should be no more pending exchanges", 0, pending.getCount());
        assertEquals(ExchangeStatus.ERROR, exchange.getStatus());
    }

    private MessageExchangeImpl createMessageExchange(final DeliveryChannelImpl channel) throws MessagingException {
        MessageExchangeFactory factory = channel.createExchangeFactoryForService(new QName("service"));    
        return (MessageExchangeImpl) factory.createInOutExchange();
    }

    private DeliveryChannelImpl createDeliveryChannel() throws JBIException, MessagingException {
        TestComponent component = new TestComponent(new QName("service"), "endpoint");
        container.activateComponent(new ActivationSpec("component", component));
        return (DeliveryChannelImpl) component.getChannel();
    }

    public static class TestComponent extends ComponentSupport {
        public TestComponent(QName service, String endpoint) {
            super(service, endpoint);
        }

        public DeliveryChannel getChannel() throws MessagingException {
            return getContext().getDeliveryChannel();
        }
    }

}
