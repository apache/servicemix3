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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.id.IdGenerator;
import org.apache.servicemix.jbi.ExchangeTimeoutException;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.listener.MessageExchangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeliveryChannel implementation
 * 
 * @version $Revision$
 */
public class DeliveryChannelImpl implements DeliveryChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliveryChannelImpl.class);

    private JBIContainer container;

    private ComponentContextImpl context;

    private ComponentMBeanImpl component;

    private BlockingQueue<MessageExchangeImpl> queue;

    private IdGenerator idGenerator = new IdGenerator();

    private MessageExchangeFactory inboundFactory;

    private int intervalCount;

    private AtomicBoolean closed = new AtomicBoolean(false);

    private Map<Thread, Boolean> waiters = new ConcurrentHashMap<Thread, Boolean>();

    private TransactionManager transactionManager;

    /**
     * When using clustering and sendSync, the exchange received will not be the
     * same as the one sent (because it has been serialized/deserialized. We
     * thus need to keep the original exchange in a map and override its state.
     */
    private Map<String, MessageExchangeImpl> exchangesById = new ConcurrentHashMap<String, MessageExchangeImpl>();

    /**
     * Constructor
     */
    public DeliveryChannelImpl(ComponentMBeanImpl component) {
        this.component = component;
        this.container = component.getContainer();
        this.queue = new ArrayBlockingQueue<MessageExchangeImpl>(component.getInboundQueueCapacity());
        this.transactionManager = (TransactionManager) this.container.getTransactionManager();
    }

    /**
     * @return size of the inbound Queue
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * close the delivery channel
     * 
     * @throws MessagingException
     */
    public void close() throws MessagingException {
        if (this.closed.compareAndSet(false, true)) {
            LOGGER.debug("Closing DeliveryChannel {}", this);
            List<MessageExchangeImpl> pending = new ArrayList<MessageExchangeImpl>(queue.size());
            queue.drainTo(pending);
            for (MessageExchangeImpl messageExchange : pending) {
                if (messageExchange.getTransactionContext() != null
                                && messageExchange.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT) {
                    notifyExchange(messageExchange.getMirror(), messageExchange.getMirror(), "close");
                }
            }
            // Interrupt all blocked thread
            Thread[] threads = waiters.keySet().toArray(new Thread[waiters.size()]);
            for (int i = 0; i < threads.length; i++) {
                threads[i].interrupt();
            }
            // deactivate all endpoints from this component
            ServiceEndpoint[] endpoints = container.getRegistry().getEndpointsForComponent(component.getComponentNameSpace());
            for (int i = 0; i < endpoints.length; i++) {
                try {
                    component.getContext().deactivateEndpoint(endpoints[i]);
                } catch (JBIException e) {
                    LOGGER.error("Error deactivating endpoint", e);
                }
            }
            // TODO: Cause all accepts to return null
            // TODO: Abort all pending exchanges
        }
    }

    protected void checkNotClosed() throws MessagingException {
        if (closed.get()) {
            throw new MessagingException(this + " has been closed.");
        }
    }

    /**
     * Create a message exchange factory. This factory will create exchange
     * instances with all appropriate properties set to null.
     * 
     * @return a message exchange factory
     */
    public MessageExchangeFactory createExchangeFactory() {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setContext(context);
        ActivationSpec activationSpec = context.getActivationSpec();
        if (activationSpec != null) {
            String componentName = context.getComponentNameSpace().getName();
            // lets auto-default the container-routing information
            QName serviceName = activationSpec.getDestinationService();
            if (serviceName != null) {
                result.setServiceName(serviceName);
                LOGGER.debug("default destination serviceName for {} = {}", componentName, serviceName);
            }
            QName interfaceName = activationSpec.getDestinationInterface();
            if (interfaceName != null) {
                result.setInterfaceName(interfaceName);
                LOGGER.debug("default destination interfaceName for {} = {}", componentName, interfaceName);
            }
            QName operationName = activationSpec.getDestinationOperation();
            if (operationName != null) {
                result.setOperationName(operationName);
                LOGGER.debug("default destination operationName for {} = {}", componentName, operationName);
            }
            String endpointName = activationSpec.getDestinationEndpoint();
            if (endpointName != null) {
                boolean endpointSet = false;
                LOGGER.debug("default destination endpointName for {} = {}", componentName, endpointName);
                if (serviceName != null && endpointName != null) {
                    endpointName = endpointName.trim();
                    ServiceEndpoint endpoint = container.getRegistry().getEndpoint(serviceName, endpointName);
                    if (endpoint != null) {
                        result.setEndpoint(endpoint);
                        LOGGER.info("Set default destination endpoint for {} to {}", componentName, endpoint);
                        endpointSet = true;
                    }
                }
                if (!endpointSet) {
                    LOGGER.warn("Could not find destination endpoint for " + componentName + " service(" + serviceName
                                    + ") with endpointName " + endpointName);
                }
            }
        }
        return result;
    }

    /**
     * Create a message exchange factory for the given interface name.
     * 
     * @param interfaceName
     *            name of the interface for which all exchanges created by the
     *            returned factory will be set
     * @return an exchange factory that will create exchanges for the given
     *         interface; must be non-null
     */
    public MessageExchangeFactory createExchangeFactory(QName interfaceName) {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setInterfaceName(interfaceName);
        return result;
    }

    /**
     * Create a message exchange factory for the given service name.
     * 
     * @param serviceName
     *            name of the service for which all exchanges created by the
     *            returned factory will be set
     * @return an exchange factory that will create exchanges for the given
     *         service; must be non-null
     */
    public MessageExchangeFactory createExchangeFactoryForService(QName serviceName) {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setServiceName(serviceName);
        return result;
    }

    /**
     * Create a message exchange factory for the given endpoint.
     * 
     * @param endpoint
     *            endpoint for which all exchanges created by the returned
     *            factory will be set for
     * @return an exchange factory that will create exchanges for the given
     *         endpoint
     */
    public MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint) {
        MessageExchangeFactoryImpl result = createMessageExchangeFactory();
        result.setEndpoint(endpoint);
        return result;
    }

    protected MessageExchangeFactoryImpl createMessageExchangeFactory() {
        MessageExchangeFactoryImpl messageExchangeFactory = new MessageExchangeFactoryImpl(idGenerator, closed);
        messageExchangeFactory.setContext(context);
        return messageExchangeFactory;
    }

    /**
     * @return a MessageExchange - blocking call
     * @throws MessagingException
     */
    public MessageExchange accept() throws MessagingException {
        return accept(Long.MAX_VALUE);
    }

    /**
     * return a MessageExchange
     * 
     * @param timeoutMS
     * @return Message Exchange
     * @throws MessagingException
     */
    public MessageExchange accept(long timeoutMS) throws MessagingException {
        try {
            checkNotClosed();
            MessageExchangeImpl me = queue.poll(timeoutMS, TimeUnit.MILLISECONDS);
            if (me != null) {
                // If the exchange has already timed out,
                // do not give it to the component
                if (me.getPacket().isAborted()) {
                    LOGGER.debug("Aborted {} in {}", me.getExchangeId(), this);
                    me = null;
                } else {
                    LOGGER.debug("Accepting {} in {}", me.getExchangeId(), this);
                    // If we have a tx lock and the exchange is not active, we
                    // need
                    // to notify here without resuming transaction
                    if (me.getTxLock() != null && me.getStatus() != ExchangeStatus.ACTIVE) {
                        notifyExchange(me.getMirror(), me.getTxLock(), "acceptFinishedExchangeWithTxLock");
                        me.handleAccept();
                        LOGGER.trace("Accepted: {}", me);
                    // We transactionnaly deliver a finished exchange
                    } else if (me.isTransacted() && me.getStatus() != ExchangeStatus.ACTIVE) {
                        // Do not resume transaction
                        me.handleAccept();
                        LOGGER.trace("Accepted: {}", me);
                    } else {
                        resumeTx(me);
                        me.handleAccept();
                        LOGGER.trace("Accepted: {}", me);
                    }
                }
            }
            if (me != null) {
                // Call input listeners
                ExchangeListener[] l = (ExchangeListener[]) container.getListeners(ExchangeListener.class);
                ExchangeEvent event = new ExchangeEvent(me, ExchangeEvent.EXCHANGE_ACCEPTED);
                for (int i = 0; i < l.length; i++) {
                    try {
                        l[i].exchangeAccepted(event);
                    } catch (Exception e) {
                        LOGGER.warn("Error calling listener: {}", e.getMessage(), e);
                    }
                }
            }
            return me;
        } catch (InterruptedException e) {
            throw new MessagingException("accept failed", e);
        }
    }

    protected void autoSetPersistent(MessageExchangeImpl me) {
        Boolean persistent = me.getPersistent();
        if (persistent == null) {
            if (context.getActivationSpec().getPersistent() != null) {
                persistent = context.getActivationSpec().getPersistent();
            } else {
                persistent = Boolean.valueOf(context.getContainer().isPersistent());
            }
            me.setPersistent(persistent);
        }
    }

    protected void throttle() {
        if (component.isExchangeThrottling()) {
            if (component.getThrottlingInterval() <= intervalCount) {
                intervalCount = -1;
                try {
                    long timeout = component.getThrottlingTimeout();
                    LOGGER.debug("throttling, sleep for: {}", timeout);
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    LOGGER.warn("throttling failed", e);
                }
            }
            intervalCount++;
        }

    }

    protected void doSend(MessageExchangeImpl me, boolean sync) throws MessagingException {
        MessageExchangeImpl mirror = me.getMirror();
        boolean finished = me.getStatus() != ExchangeStatus.ACTIVE;
        try {
            LOGGER.trace("Sent: {}", me);
            // If the message has timed out
            if (me.getPacket().isAborted()) {
                throw new ExchangeTimeoutException(me);
            }
            // Auto enlist exchange in transaction
            autoEnlistInTx(me);
            // Update persistence info
            autoSetPersistent(me);
            if (me.getRole().equals(Role.CONSUMER) 
                    && me.getStatus().equals(ExchangeStatus.ACTIVE)) {
                // Throttle if needed
                // the throttle should happen when send messageexchange from
                //consumer to provider, so avoid throttling for response me and
                //Done me
                throttle();
            }
            // Store the consumer component
            if (me.getRole() == Role.CONSUMER) {
                me.setSourceId(component.getComponentNameSpace());
            }
            // Call the listeners before the ownership changes
            // Call input listeners
            ExchangeListener[] l = (ExchangeListener[]) container.getListeners(ExchangeListener.class);
            ExchangeEvent event = new ExchangeEvent(me, ExchangeEvent.EXCHANGE_SENT);
            for (int i = 0; i < l.length; i++) {
                try {
                    l[i].exchangeSent(event);
                } catch (Exception e) {
                    LOGGER.warn("Error calling listener: {}", e.getMessage(), e);
                }
            }
            // Change ownership
            me.handleSend(sync);
            mirror.setTxState(MessageExchangeImpl.TX_STATE_NONE);
            // If this is the DONE or ERROR status from a synchronous
            // transactional exchange,
            // it should not be part of the transaction, so remove the tx
            // context
            if (finished && me.getTxLock() == null && me.getTxState() == MessageExchangeImpl.TX_STATE_CONVEYED
                            && !me.isPushDelivery() && me.getRole() == Role.CONSUMER) {
                me.setTransactionContext(null);
            }
            container.sendExchange(mirror);
        } catch (MessagingException e) {
            LOGGER.debug("Exception processing: {} in {}", me.getExchangeId(), this);
            throw e;
        } finally {
            // If there is a tx lock, we need to suspend and notify
            if (me.getTxLock() != null) {
                if (mirror.getTxState() == MessageExchangeImpl.TX_STATE_ENLISTED) {
                    suspendTx(mirror);
                }
                synchronized (me.getTxLock()) {
                    notifyExchange(me, me.getTxLock(), "doSendWithTxLock");
                }
            }
        }
    }

    /**
     * routes a MessageExchange
     * 
     * @param messageExchange
     * @throws MessagingException
     */
    public void send(MessageExchange messageExchange) throws MessagingException {
        // If the delivery channel has been closed
        checkNotClosed();
        // Log call
        LOGGER.debug("Send {} in {}", messageExchange.getExchangeId(), this);
        // // JBI 5.5.2.1.3: remove sync property
        messageExchange.setProperty(JbiConstants.SEND_SYNC, null);
        // Call doSend
        MessageExchangeImpl me = (MessageExchangeImpl) messageExchange;
        doSend(me, false);
    }

    /**
     * routes a MessageExchange
     * 
     * @param messageExchange
     * @return true if processed
     * @throws MessagingException
     */
    public boolean sendSync(MessageExchange messageExchange) throws MessagingException {
        return sendSync(messageExchange, 0);
    }

    /**
     * routes a MessageExchange
     * 
     * @param messageExchange
     * @param timeout
     * @return true if processed
     * @throws MessagingException
     */
    public boolean sendSync(MessageExchange messageExchange, long timeout) throws MessagingException {
        // If the delivery channel has been closed
        checkNotClosed();
        // Log call
        LOGGER.debug("SendSync {} in {}", messageExchange.getExchangeId(), this);
        boolean result = false;
        // JBI 5.5.2.1.3: set the sendSync property
        messageExchange.setProperty(JbiConstants.SEND_SYNC, Boolean.TRUE);
        // Call doSend
        MessageExchangeImpl me = (MessageExchangeImpl) messageExchange;
        String exchangeKey = me.getKey();
        try {
            exchangesById.put(exchangeKey, me);
            // Synchronously send a message and wait for the response
            synchronized (me) {
                doSend(me, true);
                if (me.getSyncState() != MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED) {
                    waitForExchange(me, me, timeout, "sendSync");
                } else {
                    LOGGER.debug("Exchange {} has already been answered (no need to wait)", messageExchange.getExchangeId());
                }
            }
            if (me.getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED) {
                me.handleAccept();
                // If the sender flag has been removed, it means
                // the message has been delivered in the same thread
                // so there is no need to resume the transaction
                // See processInBound
                // if (messageExchangeImpl.getSyncSenderThread() != null) {
                resumeTx(me);
                // }
                // Call input listeners
                ExchangeListener[] l = (ExchangeListener[]) container.getListeners(ExchangeListener.class);
                ExchangeEvent event = new ExchangeEvent(me, ExchangeEvent.EXCHANGE_ACCEPTED);
                for (int i = 0; i < l.length; i++) {
                    try {
                        l[i].exchangeAccepted(event);
                    } catch (Exception e) {
                        LOGGER.warn("Error calling listener: {}", e.getMessage(), e);
                    }
                }
                result = true;
            } else {
                // JBI 5.5.2.1.3: the exchange should be set to ERROR status
                LOGGER.debug("Exchange {} has been aborted", messageExchange.getExchangeId());
                me.getPacket().setAborted(true);
                me.getPacket().setError(new RuntimeException("sendSync timeout for "
                    + messageExchange.getExchangeId()));
                result = false;
            }
        } catch (InterruptedException e) {
            throw new MessagingException(e);
        } catch (RuntimeException e) {
            // e.printStackTrace();
            throw e;
        } finally {
            exchangesById.remove(exchangeKey);
        }
        return result;
    }

    /**
     * @return Returns the container.
     */
    public JBIContainer getContainer() {
        return container;
    }

    /**
     * @param container
     *            The container to set.
     */
    public void setContainer(JBIContainer container) {
        this.container = container;
    }

    /**
     * @return Returns the componentConnector.
     */
    public ComponentMBeanImpl getComponent() {
        return component;
    }

    /**
     * Get the context
     * 
     * @return the context
     */
    public ComponentContextImpl getContext() {
        return context;
    }

    /**
     * set the context
     * 
     * @param context
     */
    public void setContext(ComponentContextImpl context) {
        this.context = context;
    }

    /**
     * Used internally for passing in a MessageExchange
     * 
     * @param me
     * @throws MessagingException
     */
    public void processInBound(MessageExchangeImpl me) throws MessagingException {
        LOGGER.trace("Processing inbound exchange: {}", me);
        // Check if the delivery channel has been closed
        checkNotClosed();
        // Retrieve the original exchange sent
        MessageExchangeImpl original = exchangesById.get(me.getKey());
        if (original != null && me != original) {
            original.copyFrom(me);
            me = original;
        }
        // Check if the incoming exchange is a response to a synchronous
        // exchange previously sent
        // In this case, we do not have to queue it, but rather notify the
        // waiting thread.
        if (me.getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT) {
            // If the mirror has been delivered using push, better wait until
            // the push call return. This can only work if not using clustered
            // flows,
            // but the flag is transient so we do not care.
            // Ensure that data is uptodate with the incoming exchange (in
            // case the exchange has
            // been serialized / deserialized by a clustered flow)
            suspendTx(original);
            me.setSyncState(MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED);
            notifyExchange(original, original, "processInboundSynchronousExchange");
            return;
        }

        // If the component implements the MessageExchangeListener,
        // the delivery can be made synchronously, so we don't need
        // to bother with transactions
        MessageExchangeListener listener = getExchangeListener();
        if (listener != null && this.container.isOptimizedDelivery()) {
            me.handleAccept();
            LOGGER.trace("Received: {}", me);
            // Call input listeners
            ExchangeListener[] l = (ExchangeListener[]) container.getListeners(ExchangeListener.class);
            ExchangeEvent event = new ExchangeEvent(me, ExchangeEvent.EXCHANGE_ACCEPTED);
            for (int i = 0; i < l.length; i++) {
                try {
                    l[i].exchangeAccepted(event);
                } catch (Exception e) {
                    LOGGER.warn("Error calling listener: {}", e.getMessage(), e);
                }
            }
            // Set the flag the the exchange was delivered using push mode
            // This is important for transaction boundaries
            me.setPushDeliver(true);
            // Deliver the exchange
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(component.getComponent().getClass().getClassLoader());
                listener.onMessageExchange(me);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
            // TODO: handle delayed exchange notifications
            return;
        }

        // Component uses pull delivery.

        // If the exchange is transacted, special care should be taken.
        // But if the exchange is no more ACTIVE, just queue it, as
        // we will never have an answer back.
        if (me.isTransacted() && me.getStatus() == ExchangeStatus.ACTIVE) {
            // If the transaction is conveyed by the exchange
            // We do not need to resume the transaction in this thread
            if (me.getTxState() == MessageExchangeImpl.TX_STATE_CONVEYED) {
                try {
                    suspendTx(me);
                    queue.put(me);
                } catch (InterruptedException e) {
                    LOGGER.debug("Exchange {} aborted due to thread interruption", me.getExchangeId(), e);
                    me.getPacket().setAborted(true);
                }
            // Else the delivery / send are enlisted in the current tx.
            // We must suspend the transaction, queue it, and wait for the
            // answer
            // to be sent, at which time the tx should be suspended and resumed
            // in
            // this thread.
            } else {
                Object lock = new Object();
                synchronized (lock) {
                    try {
                        me.setTxLock(lock);
                        suspendTx(me);
                        queue.put(me);
                        waitForExchange(me, lock, 0, "processInboundTransactionalExchange");
                    } catch (InterruptedException e) {
                        LOGGER.debug("Exchange {} aborted due to thread interruption", me.getExchangeId(), e);
                        me.getPacket().setAborted(true);
                    } finally {
                        me.setTxLock(null);
                        resumeTx(me);
                    }
                }
            }
        // If the exchange is ACTIVE, the transaction boundary will suspended
        // when the
        // answer is sent
        // Else just queue the exchange
        } else {
            try {
                queue.put(me);
            } catch (InterruptedException e) {
                LOGGER.debug("Exchange {} aborted due to thread interruption", me.getExchangeId(), e);
                me.getPacket().setAborted(true);
            }
        }
    }

    protected MessageExchangeListener getExchangeListener() {
        Component comp = this.component.getComponent();
        if (comp instanceof MessageExchangeListener) {
            return (MessageExchangeListener) comp;
        }
        ComponentLifeCycle lifecycle = this.component.getLifeCycle();
        if (lifecycle instanceof MessageExchangeListener) {
            return (MessageExchangeListener) lifecycle;
        }
        return null;
    }

    /**
     * Synchronization must be performed on the given exchange when calling this
     * method
     * 
     * @param me
     * @throws InterruptedException
     */
    protected void waitForExchange(MessageExchangeImpl me, Object lock, long timeout, String from) throws InterruptedException {
        // If the channel is closed while here, we must abort
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Waiting for exchange " + me.getExchangeId() + " (" + Integer.toHexString(me.hashCode()) + ") to be answered in "
                            + this + " from " + from);
        }
        Thread th = Thread.currentThread();
        try {
            waiters.put(th, Boolean.TRUE);
            lock.wait(timeout);
        } finally {
            waiters.remove(th);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Notified: " + me.getExchangeId() + "(" + Integer.toHexString(me.hashCode()) + ") in " + this + " from " + from);
        }
    }

    protected void notifyExchange(MessageExchangeImpl me, Object lock, String from) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Notifying exchange " + me.getExchangeId() + "(" + Integer.toHexString(me.hashCode()) + ") in " + this + " from "
                            + from);
        }
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * Get Inbound Factory
     * 
     * @return the inbound message factory
     */
    public MessageExchangeFactory getInboundFactory() {
        if (inboundFactory == null) {
            inboundFactory = createExchangeFactory();
        }
        return inboundFactory;
    }

    protected void suspendTx(MessageExchangeImpl me) {
        if (transactionManager != null && !container.isUseNewTransactionModel()) {
            try {
                Transaction oldTx = me.getTransactionContext();
                if (oldTx != null) {
                    LOGGER.debug("Suspending transaction for {} in {}", me.getExchangeId(), this);
                    Transaction tx = transactionManager.suspend();
                    if (tx != oldTx) {
                        throw new IllegalStateException(
                                "the transaction context set in the messageExchange is not bound to the current thread");
                    }
                }
            } catch (Exception e) {
                LOGGER.info("Exchange {} aborted due to transaction exception", me.getExchangeId(), e);
                me.getPacket().setAborted(true);
            }
        }
    }

    protected void resumeTx(MessageExchangeImpl me) throws MessagingException {
        if (transactionManager != null && !container.isUseNewTransactionModel()) {
            try {
                Transaction oldTx = me.getTransactionContext();
                if (oldTx != null) {
                    LOGGER.debug("Resuming transaction for {} in {}", me.getExchangeId(), this);
                    transactionManager.resume(oldTx);
                }
            } catch (Exception e) {
                throw new MessagingException(e);
            }
        }
    }

    /**
     * If the jbi container configured to do so, the message exchange will
     * automatically be enlisted in the current transaction, if exists.
     * 
     * @throws MessagingException
     */
    protected void autoEnlistInTx(MessageExchangeImpl me) throws MessagingException {
        if (transactionManager != null && container.isAutoEnlistInTransaction() && !container.isUseNewTransactionModel()) {
            try {
                Transaction tx = transactionManager.getTransaction();
                if (tx != null && tx.getStatus() == Status.STATUS_ACTIVE) {
                    Object oldTx = me.getTransactionContext();
                    if (oldTx == null) {
                        me.setTransactionContext(tx);
                    } else if (oldTx != tx) {
                        throw new IllegalStateException(
                                        "the transaction context set in the messageExchange is not bound to the current thread");
                    }
                }
            } catch (Exception e) {
                throw new MessagingException(e);
            }
        }
    }

    /**
     * @return pretty print
     */
    public String toString() {
        return "DeliveryChannel{" + component.getName() + "}";
    }

    /**
     * Cancel all pending exchanges currently being handled by the DeliveryChannel
     */
    public void cancelPendingExchanges() {
        for (String id : exchangesById.keySet()) {
            MessageExchange exchange = exchangesById.get(id);
            synchronized (exchange) {
                exchange.notifyAll();   
            }
        }
    }

}
