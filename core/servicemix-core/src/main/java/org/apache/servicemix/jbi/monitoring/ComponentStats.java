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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines basic statistics on the Component
 */
public class ComponentStats extends BaseLifeCycle implements ComponentStatsMBean {

    public static final String STATS_FILE = "stats.csv";
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ComponentStats.class);
    
    private ComponentMBeanImpl component;
    private MessagingStats stats;
    private File statsFile;
    private PrintWriter statsWriter;

    /**
     * Constructor
     * 
     * @param component
     */
    public ComponentStats(ComponentMBeanImpl component) {
        this.component = component;
        this.stats = new MessagingStats(component.getName());
        if (component.getContext() != null && component.getContext().getEnvironment() != null) {
            File componentRoot = component.getContext().getEnvironment().getComponentRoot();
            if (componentRoot != null && componentRoot.exists()) {
                this.statsFile = new File(componentRoot, STATS_FILE);
            }
        }
    }

    MessagingStats getMessagingStats() {
        return stats;
    }
    
    void dumpStats() {
        if (statsFile != null) {
            try {
                if (statsWriter == null) {
                    FileOutputStream fileOut = new FileOutputStream(statsFile);
                    statsWriter = new PrintWriter(fileOut, true);
                    statsWriter.println(component.getComponentNameSpace().getName() + ":");
                    statsWriter.println("inboundExchanges,inboundExchangeRate,outboundExchanges,outboundExchangeRate");
                }
                long inbound = stats.getInboundExchanges().getCount();
                double inboundRate = stats.getInboundExchangeRate().getAveragePerSecond();
                long outbound = stats.getOutboundExchanges().getCount();
                double outboundRate = stats.getOutboundExchangeRate().getAveragePerSecond();
                statsWriter.println(inbound + "," + inboundRate + "," + outbound + "," + outboundRate);
            } catch (IOException e) {
                LOGGER.warn("Failed to dump stats", e);
            }
        }
    }
    
    void close() {
        if (statsWriter != null) {
            statsWriter.close();
            statsWriter = null;
        }
    }
    
    void incrementInbound() {
        /*
        if (component.getContainer().isNotifyStatistics()) {
            long oldCount = stats.getInboundExchanges().getCount();
            stats.getInboundExchanges().increment();
            component.firePropertyChanged(
                    "inboundExchangeCount",
                    new Long(oldCount),
                    new Long(stats.getInboundExchanges().getCount()));
            double oldRate = stats.getInboundExchangeRate().getAverageTime();
            stats.getInboundExchangeRate().addTime();
            component.firePropertyChanged("inboundExchangeRate",
                    new Double(oldRate),
                    new Double(stats.getInboundExchangeRate().getAverageTime()));
        } else {
            stats.getInboundExchanges().increment();
            stats.getInboundExchangeRate().addTime();
        }
        */
        stats.getInboundExchanges().increment();
        stats.getInboundExchangeRate().addTime();
    }
    
    void incrementOutbound() {
        /*
        if (component.getContainer().isNotifyStatistics()) {
            long oldCount = stats.getInboundExchanges().getCount();
            stats.getOutboundExchanges().increment();
            component.firePropertyChanged(
                    "outboundExchangeCount",
                    new Long(oldCount),
                    new Long(stats.getInboundExchanges().getCount()));
            double oldRate = stats.getInboundExchangeRate().getAverageTime();
            stats.getOutboundExchangeRate().addTime();
            component.firePropertyChanged("outboundExchangeRate",
                    new Double(oldRate),
                    new Double(stats.getInboundExchangeRate().getAverageTime()));
        } else {
            stats.getOutboundExchanges().increment();
            stats.getOutboundExchangeRate().addTime();
        }
        */
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
        return "Component";
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
        return "Statistics for component " + component.getDescription();
    }

    /**
     * Get the Inbound MessageExchange count
     * 
     * @return inbound count
     */
    public long getInboundExchangeCount() {
        return stats.getInboundExchanges().getCount();
    }

    /**
     * Get the Inbound MessageExchange rate (number/sec)
     * 
     * @return the inbound exchange rate
     */
    public double getInboundExchangeRate() {
        return stats.getInboundExchangeRate().getAveragePerSecond();
    }

    /**
     * Get the Outbound MessageExchange count
     * 
     * @return outbound count
     */
    public long getOutboundExchangeCount() {
        return stats.getOutboundExchanges().getCount();
    }

    /**
     * Get the Outbound MessageExchange rate (number/sec)
     * 
     * @return the outbound exchange rate
     */
    public double getOutboundExchangeRate() {
        return stats.getOutboundExchangeRate().getAveragePerSecond();
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
