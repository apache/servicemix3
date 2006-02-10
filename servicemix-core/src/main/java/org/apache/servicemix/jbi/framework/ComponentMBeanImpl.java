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
package org.apache.servicemix.jbi.framework;

import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.management.OperationInfoHelper;

/**
 * Defines basic statistics on the Component
 */
public class ComponentMBeanImpl extends BaseLifeCycle implements ComponentMBean {
    
    private static Log log = LogFactory.getLog(ComponentMBeanImpl.class);
    
    private LocalComponentConnector connector;
    private ObjectName objectName;
       

    /**
     * Constructor
     * 
     * @param lcc
     */
    public ComponentMBeanImpl(LocalComponentConnector lcc) {
        this.connector = lcc;
    }

    /**
     * Get the JMX ObjectName for any additional MBean for this component. If there is none, return null.
     * 
     * @return ObjectName the JMX object name of the additional MBean or null if there is no additional MBean.
     */
    public ObjectName getExtensionMBeanName() {
        return connector.getComponent().getLifeCycle().getExtensionMBeanName();
    }
    
    /**
     * Get the ObjectName for this mbean
     * @return the ObjectName
     */
    public ObjectName getObjectName(){
        return objectName;
    }
    /**
     * Get the name of the item
     * @return the name
     */
    public String getName(){
        return connector.getComponentNameSpace().getName();
    }
    
    /**
     * Get the type of the item
     * @return the type
     */
    public String getType() {
        return "Component";
    }
    
    public String getSubType() {
        return "LifeCycle";
    }
    
   /**
     * Get the Description of the item
     * @return the description
     */
    public String getDescription(){
        return connector.getComponentPacket().getDescription();
    }

    
    public void init() throws JBIException{
        connector.init();
        super.init();
    }
    
    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void start() throws javax.jbi.JBIException {
        try {
            doStart();
            connector.writeRunningState();
        } catch (JBIException e) {
            log.error("Could not start component", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Could not start component", e);
            throw e;
        } catch (Error e) {
            log.error("Could not start component", e);
            throw e;
        }
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException if the item fails to stop.
     */
    public void stop() throws javax.jbi.JBIException {
        try {
            doStop();
            connector.writeRunningState();
        } catch (JBIException e) {
            log.error("Could not stop component", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Could not start component", e);
            throw e;
        } catch (Error e) {
            log.error("Could not start component", e);
            throw e;
        }
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception javax.jbi.JBIException if the item fails to shut down.
     */
    public void shutDown() throws javax.jbi.JBIException {
        try {
            doShutDown();
            connector.writeRunningState();
        } catch (JBIException e) {
            log.error("Could not shutDown component", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Could not start component", e);
            throw e;
        } catch (Error e) {
            log.error("Could not start component", e);
            throw e;
        }
    }
    
    /**
     * Start the item - doesn't persist the state
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void doStart() throws javax.jbi.JBIException {
        if (isShutDown()) {
            // need to re-initialze before starting
            connector.init();
        }
        if (!isRunning()) {
            connector.getLifeCycle().start();
            super.start();
        }
    }

    /**
     * Stop the item - doesn't persist the state
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to stop.
     */
    public void doStop() throws javax.jbi.JBIException {
        if (isUnknown() || isRunning()) {
	        connector.getLifeCycle().stop();
	        super.stop();
        }
        
    }

    /**
     * Shut down the item - doesn't persist the state
     * 
     * @exception javax.jbi.JBIException if the item fails to shut down.
     */
    public void doShutDown() throws javax.jbi.JBIException {
        doStop();
        // Transition from UNKNOWN to SHUTDOWN is done at installation time
        // In this case or if the component is already shut down, do nothing
        if (!getCurrentState().equals(UNKNOWN) && !getCurrentState().equals(SHUTDOWN)) {
            connector.getLifeCycle().shutDown();
        }
        super.shutDown();
    }
    
    
    /**
     * Set the initial running state of the Component
     * @throws JBIException
     */
    public void setInitialRunningState() throws JBIException{
        connector.setRunningStateFromStore();
    }
    
    /**
     * Persist the running state
     */
    public void persistRunningState() {
        connector.writeRunningState();
    }
   
    

    /**
     * Get the Inbound MessageExchange count
     * 
     * @return inbound count
     */
    public long getInboundExchangeCount() {
        return connector.getDeliveryChannel().getMessagingStats().getInboundExchanges().getCount();
    }

    /**
     * Get the Inbound MessageExchange rate (number/sec)
     * 
     * @return the inbound exchange rate
     */
    public double getInboundExchangeRate() {
        return connector.getDeliveryChannel().getMessagingStats().getInboundExchangeRate().getAverageTime();
    }

    /**
     * Get the Outbound MessageExchange count
     * 
     * @return outbound count
     */
    public long getOutboundExchangeCount() {
        return connector.getDeliveryChannel().getMessagingStats().getOutboundExchanges().getCount();
    }

    /**
     * Get the Outbound MessageExchange rate (number/sec)
     * 
     * @return the outbound exchange rate
     */
    public double getOutboundExchangeRate() {
        return connector.getDeliveryChannel().getMessagingStats().getOutboundExchangeRate().getAverageTime();
    }

    /**
     * reset all stats counters
     */
    public void reset() {
        connector.getDeliveryChannel().getMessagingStats().getOutboundExchangeRate().reset();
    }
    
    /**
     * @return size of the inbound Queue
     */
    public int getInboundQueueSize(){
        return connector.getDeliveryChannel().getQueueSize();
    }
    
    /**
     * @return the capacity of the inbound queue
     */
    public int getInboundQueueCapacity(){
        return connector.getDeliveryChannel().getQueueCapacity();
    }
    
    /**
     * Set the inbound queue capacity
     * @param value
     */
    public void setInboundQueueCapacity(int value){
        connector.getDeliveryChannel().setQueueCapacity(value);
    }
    
    /**
     * Is MessageExchange sender throttling enabled ?
     * @return true if throttling enabled
     */
    public boolean isExchangeThrottling(){
        return connector.getDeliveryChannel().isExchangeThrottling();
    }
    
    /**
     * Set exchange throttling
     * @param value
     *
     */
    public void setExchangeThrottling(boolean value){
        connector.getDeliveryChannel().setExchangeThrottling(value);
    }
    
    /**
     * Get the throttling timeout
     * @return throttling tomeout (ms)
     */
    public long getThrottlingTimeout(){
        return connector.getDeliveryChannel().getThrottlingTimeout();
    }
    
    /**
     * Set the throttling timout 
     * @param value (ms)
     */
    public void setThrottlingTimeout(long value){
        connector.getDeliveryChannel().setThrottlingTimeout(value);
    }
    
    /**
     * Get the interval for throttling -
     * number of Exchanges set before the throttling timeout is applied
     * @return interval for throttling
     */
    public int getThrottlingInterval(){
        return connector.getDeliveryChannel().getThrottlingInterval();
    }
    
    /**
     * Set the throttling interval
     * number of Exchanges set before the throttling timeout is applied
     * @param value
     */
    public void setThrottlingInterval(int value){
        connector.getDeliveryChannel().setThrottlingInterval(value);
    }
    
    /**
     * @return the ObjectName for the stats MBean for this Component - or null if it doesn't exist
     */
    public ObjectName getStatsMBeanName(){
        return connector.getStatsMbeanName();
    }
    /**
     * Get an array of MBeanAttributeInfo
     * 
     * @return array of AttributeInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "inboundQueueSize", "size of the inbound queue");
        helper.addAttribute(getObjectToManage(), "inboundQueueCapacity", "capacity of the inbound queue");
        helper.addAttribute(getObjectToManage(), "inboundExchangeCount", "count of inbound exchanges");
        helper.addAttribute(getObjectToManage(), "outboundExchangeCount", "count of outbound exchanges");
        helper.addAttribute(getObjectToManage(), "inboundExchangeRate", "rate of inbound exchanges/sec");
        helper.addAttribute(getObjectToManage(), "outboundExchangeRate", "rate of outbound exchanges/sec");
        helper.addAttribute(getObjectToManage(), "exchangeThrottling", "apply throttling");
        helper.addAttribute(getObjectToManage(), "throttlingTimeout", "timeout for throttling");
        helper.addAttribute(getObjectToManage(), "throttlingInterval", "exchange intervals before throttling");
        return AttributeInfoHelper.join(super.getAttributeInfos(),helper.getAttributeInfos());
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "getExtensionMBeanName", "extension mbean name");
        helper.addOperation(getObjectToManage(), "reset", "reset statistic counters");
        return OperationInfoHelper.join(super.getOperationInfos(),helper.getOperationInfos());
    }

	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
    
    public void firePropertyChanged(String name, Object oldValue, Object newValue) {
        super.firePropertyChanged(name, oldValue, newValue);
    }


}
