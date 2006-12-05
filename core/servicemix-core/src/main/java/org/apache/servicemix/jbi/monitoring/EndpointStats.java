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
package org.apache.servicemix.jbi.monitoring;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;

public class EndpointStats extends BaseLifeCycle implements EndpointStatsMBean {

    private AbstractServiceEndpoint endpoint;
    private MessagingStats stats;
    
    public EndpointStats(AbstractServiceEndpoint endpoint, MessagingStats componentStats) {
        this.endpoint = endpoint;
        this.stats = new MessagingStats(EndpointSupport.getUniqueKey(endpoint), componentStats);
    }
    
    MessagingStats getMessagingStats() {
        return stats;
    }
    
    void incrementInbound() {
        stats.getInboundExchanges().increment();
        stats.getInboundExchangeRate().addTime();
    }
    
    void incrementOutbound() {
        stats.getOutboundExchanges().increment();
        stats.getOutboundExchangeRate().addTime();
    }
    
    /**
     * Get the type of the item
     * @return the type
     */
    public String getType() {
        return "Statistics";
    }

    public String getSubType() {
        return "Endpoint";
    }
    
    /**
     * Get the name of the item
     * @return the name
     */
    public String getName() {
        return EndpointSupport.getUniqueKey(endpoint);
    }

    /**
     * Get the Description of the item
     * @return the description
     */
    public String getDescription() {
        return "Statistics for endpoint " + EndpointSupport.getUniqueKey(endpoint);
    }

    public long getInboundExchangeCount() {
        return stats.getInboundExchanges().getCount();
    }

    public double getInboundExchangeRate() {
        return stats.getInboundExchangeRate().getAveragePerSecond();
    }

    public long getOutboundExchangeCount() {
        return stats.getOutboundExchanges().getCount();
    }

    public double getOutboundExchangeRate() {
        return stats.getOutboundExchangeRate().getAveragePerSecond();
    }

    /**
     * Reset all stats counters
     */
    public void reset() {
        stats.reset();
    }

    /**
     * Get an array of MBeanAttributeInfo
     * 
     * @return array of AttributeInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
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
