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

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.management.OperationInfoHelper;

/**
 * Defines basic statistics on the Component
 */
public class ComponentStatsMBeanImpl  extends BaseLifeCycle implements ComponentStatsMBean {
    private LocalComponentConnector connector;
    private ObjectName objectName;
       

    /**
     * Constructor
     * 
     * @param lcc
     */
    public ComponentStatsMBeanImpl(LocalComponentConnector lcc) {
        this.connector = lcc;
    }

    
    /**
     * Get the ObjectName for this mbean
     * @return the ObjectName
     */
    public ObjectName getObjectName(){
        return objectName;
    }
    
    
    /**
     * Get the type of the item
     * @return the type
     */
    public String getType() {
        return "Component";
    }
    
    public String getSubType() {
        return "Statistics";
    }
    
    /**
     * Get the name of the item
     * @return the name
     */
    public String getName(){
        return connector.getComponentNameSpace().getName();
    }
    
   /**
     * Get the Description of the item
     * @return the description
     */
    public String getDescription(){
        return "Statistics for " + connector.getComponentPacket().getDescription();
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
     * @return size of the inbound Queue
     */
    public int getInboundQueueSize(){
        return connector.getDeliveryChannel().getQueueSize();
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
        helper.addAttribute(getObjectToManage(), "inboundExchangeCount", "count of inbound exchanges");
        helper.addAttribute(getObjectToManage(), "outboundExchangeCount", "count of outbound exchanges");
        helper.addAttribute(getObjectToManage(), "inboundExchangeRate", "rate of inbound exchanges/sec");
        helper.addAttribute(getObjectToManage(), "outboundExchangeRate", "rate of outbound exchanges/sec");
        return helper.getAttributeInfos();
    }
    
    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     */
    public MBeanOperationInfo[] getOperationInfos() {
        OperationInfoHelper helper = new OperationInfoHelper();
        return helper.getOperationInfos();
    }


    /**
     * Set the object name
     * @param objectName
     */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}
    
    


}
