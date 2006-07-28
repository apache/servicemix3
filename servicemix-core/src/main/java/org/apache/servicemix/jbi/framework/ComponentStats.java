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
package org.apache.servicemix.jbi.framework;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.management.OperationInfoHelper;

/**
 * Defines basic statistics on the Component
 */
public class ComponentStats extends BaseLifeCycle implements ComponentStatsMBean {

    private ComponentMBeanImpl component;

    /**
     * Constructor
     * 
     * @param lcc
     */
    public ComponentStats(ComponentMBeanImpl component) {
        this.component = component;
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
    public String getName() {
        return component.getName();
    }

    /**
     * Get the Description of the item
     * @return the description
     */
    public String getDescription() {
        return "Statistics for " + component.getDescription();
    }

    /**
     * Get the Inbound MessageExchange count
     * 
     * @return inbound count
     */
    public long getInboundExchangeCount() {
        return component.getMessagingStats().getInboundExchanges().getCount();
    }

    /**
     * Get the Inbound MessageExchange rate (number/sec)
     * 
     * @return the inbound exchange rate
     */
    public double getInboundExchangeRate() {
        return component.getMessagingStats().getInboundExchangeRate().getAverageTime();
    }

    /**
     * Get the Outbound MessageExchange count
     * 
     * @return outbound count
     */
    public long getOutboundExchangeCount() {
        return component.getMessagingStats().getOutboundExchanges().getCount();
    }

    /**
     * Get the Outbound MessageExchange rate (number/sec)
     * 
     * @return the outbound exchange rate
     */
    public double getOutboundExchangeRate() {
        return component.getMessagingStats().getOutboundExchangeRate().getAverageTime();
    }

    /**
     * @return size of the inbound Queue
     */
    public int getInboundQueueSize() {
        if (component.getDeliveryChannel() != null) {
            return component.getDeliveryChannel().getQueueSize();
        } else {
            return 0;
        }
    }

    /**
     * Reset all stats counters
     */
    public void reset() {
        component.getMessagingStats().reset();
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
        helper.addOperation(getObjectToManage(), "reset", "reset statistic counters");
        return helper.getOperationInfos();
    }

}
