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
package org.servicemix.jbi.nmr.flow;

import edu.emory.mathcs.backport.java.util.concurrent.locks.ReadWriteLock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.jbi.framework.ComponentNameSpace;
import org.servicemix.jbi.framework.LocalComponentConnector;
import org.servicemix.jbi.management.AttributeInfoHelper;
import org.servicemix.jbi.management.BaseLifeCycle;
import org.servicemix.jbi.messaging.ExchangePacket;
import org.servicemix.jbi.messaging.MessageExchangeImpl;
import org.servicemix.jbi.nmr.Broker;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

/**
 * A simple Straight through flow
 * 
 * @version $Revision$
 */
public abstract class AbstractFlow extends BaseLifeCycle implements Flow {
    private static final Log log = LogFactory.getLog(AbstractFlow.class);
    protected Broker broker;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Thread suspendThread = null;
    private String subType;

    /**
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, String subType) throws JBIException {
        this.broker = broker;
        this.subType = subType;
		// register self with the management context
        ObjectName objectName = broker.getManagementContext().createObjectName(this);
        try {
        	if (subType != null) {
        		objectName = new ObjectName(objectName + ",subtype=" + subType);
        	}
            broker.getManagementContext().registerMBean(objectName, this, LifeCycleMBean.class);
        }
        catch (JMException e) {
            throw new JBIException("Failed to register MBean with the ManagementContext", e);
        }
    }
    
    /**
     * start the flow
     * @throws JBIException
     */
    public void start() throws JBIException{
        super.start();
    }
    
    
    /**
     * stop the flow
     * @throws JBIException
     */
    public void stop() throws JBIException{
    	if (log.isDebugEnabled())
    		log.debug("Called Flow stop");
        if (suspendThread != null){
            suspendThread.interrupt();
        }
        super.stop();
    }
    
    /**
     * shutDown the flow
     * @throws JBIException
     */
    public void shutDown() throws JBIException{
    	if (log.isDebugEnabled())
    		log.debug("Called Flow shutdown");
        super.shutDown();
    }
    
    /**
     * Distribute an ExchangePacket
     * @param packet
     * @throws JBIException
     */
    public void send(MessageExchange me) throws JBIException{
    	// Check persistence
    	if (log.isDebugEnabled())
    		log.debug("Called Flow send");
    	if (!canPersist() && isPersistent(me)) {
    		throw new UnsupportedOperationException("persistence is not available on st flow");
    	}
    	// do send
        try {
            lock.readLock().lock();
            doSend((MessageExchangeImpl) me);
        } finally{
            lock.readLock().unlock();
        }
    }
    
    /**
     * suspend the flow to prevent any message exchanges
     */
    public synchronized void suspend(){
    	if (log.isDebugEnabled())
    		log.debug("Called Flow suspend");
        lock.writeLock().lock();
        suspendThread = Thread.currentThread();
    }
    
    
    /**
     * resume message exchange processing
     */
    public synchronized void resume(){
    	if (log.isDebugEnabled())
    		log.debug("Called Flow resume");
        lock.writeLock().unlock();
        suspendThread = null;
    }
    
    /**
     * Do the Flow specific routing
     * @param packet
     * @throws JBIException
     */
    protected abstract void doSend(MessageExchangeImpl me) throws JBIException;

    /**
     * Ability for this flow to persist exchanges.
     * 
     * @return <code>true</code> if this flow can persist messages
     */
    protected boolean canPersist() {
    	return false;
    }
    
    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws MessagingException
     */
    public void doRouting(MessageExchangeImpl me) throws MessagingException {
    	if (log.isDebugEnabled())
    		log.debug("Called Flow doRouting");
        ComponentNameSpace id = me.getRole() == Role.PROVIDER ? me.getDestinationId() : me.getSourceId();
        //As the MessageExchange could come from another container - ensure we get the local Component
        ComponentNameSpace copy = id.copy();
        copy.setContainerName(broker.getContainerName());
        LocalComponentConnector lcc = broker.getRegistry().getLocalComponentConnector(copy);
        if (lcc != null) {
            lcc.getDeliveryChannel().processInBound(me);
        }
        else {
            throw new MessagingException("No component with id (" + id + ") - Couldn't route MessageExchange " + me);
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

    public Broker getBroker() {
        return broker;
    }

    public String getSubType() {
        return subType;
    }

}