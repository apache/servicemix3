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
package org.apache.servicemix.jbi.nmr.flow.seda;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.framework.ComponentPacketEvent;
import org.apache.servicemix.jbi.framework.ComponentPacketEventListener;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import java.util.Iterator;
import java.util.Map;

/**
 * The SedaFlow introduces a simple event staging between the internal processes 
 * in the NMR Broker. A Seda flow (the default) is suited for general deployment, 
 * as the additional staging is well suited buffering exchanges between heavily 
 * routed to components (where state may be being used) for example.
 * 
 * @version $Revision$
 */
public class SedaFlow extends AbstractFlow implements ComponentPacketEventListener {
    private static final Log log = LogFactory.getLog(SedaFlow.class);
    protected Map queueMap = new ConcurrentHashMap();
    protected int capacity = 100;
    protected AtomicBoolean started = new AtomicBoolean(false);

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
    public void init(Broker broker, String subType) throws JBIException {
        super.init(broker, subType);
        broker.getRegistry().addComponentPacketListener(this);
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
        broker.getRegistry().removeComponentPacketListener(this);
        for (Iterator i = queueMap.values().iterator();i.hasNext();) {
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
        // If the message has been sent synchronously, do not use seda
        // as it would consume threads from the work manager in a useless
        // way.  This could lead to deadlocks.
        if (me.getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME) == null &&
            me.getSyncState() == MessageExchangeImpl.SYNC_STATE_ASYNC &&
            me.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_ASYNC) {
        	enqueuePacket(me);
        }
        else {
            doRouting(me);
        }
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
            queue = new SedaQueue(cns);
            queueMap.put(cns, queue);
            queue.init(this, capacity);
            registerQueue(cns, queue);
            if (started.get()) {
                queue.start();
            }
        }
        try {
            queue.enqueue(me);
        }
        catch (InterruptedException e) {
            throw new MessagingException(queue + " Failed to enqueue exchange: " + me, e);
        }
    }
    
    /**
     * Process state changes in Components
     * 
     * @param event
     */
    public synchronized void onEvent(ComponentPacketEvent event) {
        // watch for deactivations
        if (event.getStatus() == ComponentPacketEvent.DEACTIVATED) {
            ComponentNameSpace cns = event.getPacket().getComponentNameSpace();
            SedaQueue queue = (SedaQueue) queueMap.remove(cns);
            if (queue != null) {
                try {
                    queue.shutDown();
                    unregisterQueue(queue);
                }
                catch (JBIException e) {
                    log.error("Caught exception stopping SedaQueue: " + queue);
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
     * @return Returns the capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * @param capacity The capacity to set.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
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
            ObjectName objectName = broker.getManagementContext().createObjectName(queue);
            if (getSubType() != null) {
                objectName = new ObjectName(objectName + ",subtype=" + getSubType());
            }
            queue.setObjectName(objectName);
            broker.getManagementContext().registerMBean(objectName, queue, LifeCycleMBean.class);
        }
        catch (JMException e) {
            log.error("Failed to register SedaQueue: " + queue + " with the ManagementContext");
        }
    }

    protected void unregisterQueue(SedaQueue queue) {
        try {
            broker.getManagementContext().unregisterMBean(queue.getObjectName());
        }
        catch (JBIException e) {
            log.error("Failed to unregister SedaQueue: " + queue + " from the ManagementContext");
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
        helper.addAttribute(getObjectToManage(), "capacity", "default  capacity of a SedaQueue");
        helper.addAttribute(getObjectToManage(), "queueNumber", "number of running SedaQueues");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }
}