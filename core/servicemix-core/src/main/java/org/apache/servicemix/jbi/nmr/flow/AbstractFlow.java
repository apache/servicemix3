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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.executors.ExecutorFactory;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.messaging.ExchangePacket;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.apache.servicemix.locks.ReentrantReadWriteLock;

/**
 * A simple Straight through flow
 * 
 * @version $Revision$
 */
public abstract class AbstractFlow extends BaseLifeCycle implements Flow {

    protected final Log log = LogFactory.getLog(getClass());
    protected Broker broker;
    protected ExecutorFactory executorFactory;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Thread suspendThread;
    private String name;
    private int writeLockTimeout = 5;
    
    
    /**
     * Initialize the Region
     * 
     * @param br
     * @throws JBIException
     */
    public void init(Broker br) throws JBIException {
        this.broker = br;
        this.executorFactory = br.getContainer().getExecutorFactory();
        // register self with the management context
        ObjectName objectName = br.getContainer().getManagementContext().createObjectName(this);
        try {
            br.getContainer().getManagementContext().registerMBean(objectName, this, LifeCycleMBean.class);
        } catch (JMException e) {
            throw new JBIException("Failed to register MBean with the ManagementContext", e);
        }
    }

    /**
     * start the flow
     * @throws JBIException
     */
    public void start() throws JBIException {
        super.start();
    }

    /**
     * stop the flow
     * @throws JBIException
     */
    public void stop() throws JBIException {
        if (log.isDebugEnabled()) {
            log.debug("Called Flow stop");
        }
        if (suspendThread != null) {
            suspendThread.interrupt();
        }
        super.stop();
    }

    /**
     * shutDown the flow
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        if (log.isDebugEnabled()) {
            log.debug("Called Flow shutdown");
        }
        broker.getContainer().getManagementContext().unregisterMBean(this);
        super.shutDown();
    }

    /**
     * Distribute an ExchangePacket
     * @param packet
     * @throws JBIException
     */
    public void send(MessageExchange me) throws JBIException {
        if (log.isDebugEnabled()) {
            log.debug("Called Flow send");
        }
        // do send
        try {
            lock.readLock().lock();
            doSend((MessageExchangeImpl) me);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * suspend the flow to prevent any message exchanges
     */
    public synchronized void suspend() {
        if (log.isDebugEnabled()) {
            log.debug("Called Flow suspend");
        }
        try {
            lock.writeLock().tryLock(writeLockTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.debug("try lock interrupted");
        }
        suspendThread = Thread.currentThread();
    }

    /**
     * resume message exchange processing
     */
    public synchronized void resume() {
        if (log.isDebugEnabled()) {
            log.debug("Called Flow resume");
        }
        try {
            lock.writeLock().unlock();
        } catch (IllegalMonitorStateException e) {
            log.debug("not hold the write lock");
        }
        suspendThread = null;
    }

    /**
     * Do the Flow specific routing
     * @param packet
     * @throws JBIException
     */
    protected abstract void doSend(MessageExchangeImpl me) throws JBIException;

    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws MessagingException
     */
    protected void doRouting(MessageExchangeImpl me) throws MessagingException {
        ComponentNameSpace id = me.getRole() == Role.PROVIDER ? me.getDestinationId() : me.getSourceId();
        //As the MessageExchange could come from another container - ensure we get the local Component
        ComponentMBeanImpl lcc = broker.getContainer().getRegistry().getComponent(id.getName());
        if (lcc != null) {
            if (lcc.getDeliveryChannel() != null) {
                try {
                    lock.readLock().lock();
                    lcc.getDeliveryChannel().processInBound(me);
                } finally {
                    lock.readLock().unlock();
                }
            } else {
                throw new MessagingException("Component " + id.getName() + " is shut down");
            }
        } else {
            throw new MessagingException("No component named " + id.getName() + " - Couldn't route MessageExchange " + me);
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
        helper.addAttribute(getObjectToManage(), "description", "The type of flow");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }

    /**
     * Check if the given packet should be persisted or not.
     * @param packet
     * @return
     */
    protected boolean isPersistent(MessageExchange me) {
        ExchangePacket packet = ((MessageExchangeImpl) me).getPacket();
        if (packet.getPersistent() != null) {
            return packet.getPersistent().booleanValue();
        } else {
            return broker.getContainer().isPersistent();
        }
    }

    protected boolean isTransacted(MessageExchange me) {
        return me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME) != null;
    }

    protected boolean isSynchronous(MessageExchange me) {
        Boolean sync = (Boolean) me.getProperty(JbiConstants.SEND_SYNC);
        return sync != null && sync.booleanValue();
    }

    protected boolean isClustered(MessageExchange me) {
        MessageExchangeImpl mei = (MessageExchangeImpl) me;
        if (mei.getDestinationId() == null) {
            ServiceEndpoint se = me.getEndpoint();
            if (se instanceof InternalEndpoint) {
                return ((InternalEndpoint) se).isClustered();
                // Unknown: assume this is not clustered
            } else {
                return false;
            }
        } else {
            String destination = mei.getDestinationId().getContainerName();
            String source = mei.getSourceId().getContainerName();
            return !source.equals(destination);
        }
    }

    public Broker getBroker() {
        return broker;
    }

    /**
     * Get the type of the item
     * @return the type
     */
    public String getType() {
        return "Flow";
    }

    /**
     * Get the name of the item
     * @return the name
     */
    public String getName() {
        if (this.name == null) {
            String n = super.getName();
            if (n.endsWith("Flow")) {
                n = n.substring(0, n.length() - 4);
            }
            return n;
        } else {
            return this.name;
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the executorFactory
     */
    public ExecutorFactory getExecutorFactory() {
        return executorFactory;
    }

    /**
     * Specifies the interval for which ReentrantReadWriteLock.WriteLock.tryLock will elapse,
     * This is specified in seconds.
     * 
     * @param writeLockTimeout
     *            the number of second ReentrantReadWriteLock.WriteLock.tryLock will elapse
     * @org.apache.xbean.Property description="the number of second ReentrantReadWriteLock.WriteLock.tryLock will elapse. 
     *                             The default is 5 secs."
     */
    public void setWriteLockTimeout(int writeLockTimeout) {
        this.writeLockTimeout = writeLockTimeout;
    }

    public int getWriteLockTimeout() {
        return writeLockTimeout;
    }

}
