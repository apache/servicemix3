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
package org.apache.servicemix.jbi.nmr.flow.seda;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.servicemix.jbi.event.ComponentAdapter;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;

/**
 * The SedaFlow introduces a simple event staging between the internal processes 
 * in the NMR Broker. A Seda flow (the default) is suited for general deployment, 
 * as the additional staging is well suited buffering exchanges between heavily 
 * routed to components (where state may be being used) for example.
 * 
 * @version $Revision$
 * @org.apache.xbean.XBean element="sedaFlow"
 */
public class SedaFlow extends AbstractFlow {

    protected Map queueMap = new ConcurrentHashMap();
    protected AtomicBoolean started = new AtomicBoolean(false);
    protected ComponentListener listener;

    /**
     * The type of Flow
     * 
     * @return the type
     */
    public String getDescription() {
        return "seda";
    }

    /**
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker) throws JBIException {
        super.init(broker);
        listener = new ComponentAdapter() {
            public void componentShutDown(ComponentEvent event) {
                onComponentShutdown(event.getComponent().getComponentNameSpace());
            }
        };
        broker.getContainer().addListener(listener);
    }

    /**
     * Check if the flow can support the requested QoS for this exchange
     * @param me the exchange to check
     * @return true if this flow can handle the given exchange
     */
    public boolean canHandle(MessageExchange me) {
        if (isPersistent(me)) {
            return false;
        }
        if (isClustered(me)) {
            return false;
        }
        if (isTransacted(me)) {
            if (!isSynchronous(me)) {
                // we have the mirror, so the role is the one for the target component
                if (me.getStatus() == ExchangeStatus.ACTIVE) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * start the flow
     * 
     * @throws JBIException
     */
    public void start() throws JBIException {
        if (started.compareAndSet(false, true)) {
            for (Iterator i = queueMap.values().iterator();i.hasNext();) {
                SedaQueue queue = (SedaQueue) i.next();
                queue.start();
            }
        }
        super.start();
    }

    /**
     * stop the flow
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException {
        if (started.compareAndSet(true, false)) {
            for (Iterator i = queueMap.values().iterator();i.hasNext();) {
                SedaQueue queue = (SedaQueue) i.next();
                queue.stop();
            }
        }
        super.stop();
    }

    /**
     * shutDown the flow
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        broker.getContainer().removeListener(listener);
        for (Iterator i = queueMap.values().iterator(); i.hasNext();) {
            SedaQueue queue = (SedaQueue) i.next();
            queue.shutDown();
            unregisterQueue(queue);
        }
        super.shutDown();
    }

    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws JBIException
     */
    protected void doSend(MessageExchangeImpl me) throws JBIException {
        if (me.getDestinationId() == null) {
            me.setDestinationId(((AbstractServiceEndpoint) me.getEndpoint()).getComponentNameSpace());
        }
        if (isTransacted(me)) {
            me.setTxState(MessageExchangeImpl.TX_STATE_CONVEYED);
        }
        // If the message has been sent synchronously, do not use seda
        // as it would consume threads from the work manager in a useless
        // way.  This could lead to deadlocks.
        suspendTx(me);
        enqueuePacket(me);
    }
    
    protected void doRouting(MessageExchangeImpl me) throws MessagingException {
        resumeTx(me);
        super.doRouting(me);
    }

    /**
     * Put the packet in the queue for later processing. 
     * @param packet
     * @throws JBIException
     */
    protected void enqueuePacket(MessageExchangeImpl me) throws JBIException {
        ComponentNameSpace cns = me.getDestinationId();
        SedaQueue queue = (SedaQueue) queueMap.get(cns);
        if (queue == null) {
            queue = createQueue(cns);
        }
        try {
            queue.enqueue(me);
        }
        catch (InterruptedException e) {
            throw new MessagingException(queue + " Failed to enqueue exchange: " + me, e);
        }
    }
    
    protected synchronized SedaQueue createQueue(ComponentNameSpace cns) throws JBIException {
        SedaQueue queue = (SedaQueue) queueMap.get(cns);
        if (queue == null) {
            queue = new SedaQueue(cns);
            queue.init(this);
            registerQueue(cns, queue);
            if (started.get()) {
                queue.start();
            }
            queueMap.put(cns, queue);
        }
        return queue;
    }
    
    /**
     * Process state changes in Components
     * 
     * @param event
     */
    public synchronized void onComponentShutdown(ComponentNameSpace cns) {
        SedaQueue queue = (SedaQueue) queueMap.remove(cns);
        if (queue != null) {
            try {
                queue.shutDown();
                unregisterQueue(queue);
            }
            catch (JBIException e) {
                log.error("Failed to stop SedaQueue: " + queue + ": " + e);
                if (log.isDebugEnabled()) {
                    log.debug("Failed to stop SedaQueue: " + queue, e);
                }
            }
        }
    }

    /**
     * release a queue
     * 
     * @param queue
     */
    public synchronized void release(SedaQueue queue) {
        if (queue != null) {
            queueMap.remove(queue.getComponentNameSpace());
            unregisterQueue(queue);
        }
    }

    /**
     * Get Queue number
     * 
     * @return number of running Queues
     */
    public int getQueueNumber() {
        return queueMap.size();
    }

    protected void registerQueue(ComponentNameSpace cns, SedaQueue queue) {
        try {
            ObjectName objectName = broker.getContainer().getManagementContext().createObjectName(queue);
            if (getSubType() != null) {
                objectName = new ObjectName(objectName + ",subtype=" + getSubType());
            }
            queue.setObjectName(objectName);
            broker.getContainer().getManagementContext().registerMBean(objectName, queue, LifeCycleMBean.class);
        }
        catch (JMException e) {
            log.error("Failed to register SedaQueue: " + queue + " with the ManagementContext: " + e);
            if (log.isDebugEnabled()) {
                log.debug("Failed to register SedaQueue: " + queue + " with the ManagementContext", e);
            }
        }
    }

    protected void unregisterQueue(SedaQueue queue) {
        try {
            broker.getContainer().getManagementContext().unregisterMBean(queue.getObjectName());
        }
        catch (JBIException e) {
            log.error("Failed to unregister SedaQueue: " + queue + " from the ManagementContext");
            if (log.isDebugEnabled()) {
                log.debug("Failed to unregister SedaQueue: " + queue + " with the ManagementContext", e);
            }
        }
    }

    /**
     * Get an array of MBeanAttributeInfo
     * 
     * @return array of AttributeInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "queueNumber", "number of running SedaQueues");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }

    protected void suspendTx(MessageExchangeImpl me) throws MessagingException {
        try {
            Transaction oldTx = me.getTransactionContext();
            if (oldTx != null) {
                TransactionManager tm = (TransactionManager) getBroker().getContainer().getTransactionManager();
                if (tm != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Suspending transaction for " + me.getExchangeId() + " in " + this);
                    }
                    Transaction tx = tm.suspend();
                    if (tx != oldTx) {
                        throw new IllegalStateException("the transaction context set in the messageExchange is not bound to the current thread");
                    }
                }
            }
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }

    protected void resumeTx(MessageExchangeImpl me) throws MessagingException {
        try {
            Transaction oldTx = me.getTransactionContext();
            if (oldTx != null) {
                TransactionManager tm = (TransactionManager) getBroker().getContainer().getTransactionManager();
                if (tm != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Resuming transaction for " + me.getExchangeId() + " in " + this);
                    }
                    tm.resume(oldTx);
                }
            }
        } catch (Exception e) {
            throw new MessagingException(e);
        }
    }

}
