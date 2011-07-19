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
package org.apache.servicemix.components.jmx;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.CounterMonitor;
import javax.xml.transform.Source;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMX Counter Monitor as a Component to enable firing notifications
 * 
 * @version $Revision$
 */
public class CounterMonitorComponent extends ComponentSupport implements NotificationListener, MessageExchangeListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ComponentSupport.class);

    private String name;
    private ObjectName ourName;
    private String observedObjectName;
    private String attributeName;
    private long granularityPeriod =5000;
    private Number threshold;
    private Number offset;
    private MBeanServer mbeanServer;
    private CounterMonitor counterMonitor = new CounterMonitor();

    /**
     * Called when the Component is initialized
     * 
     * @param cc
     * @throws JBIException
     */
    public void init(ComponentContext cc) throws JBIException {
        super.init(cc);
        validate();
        if (mbeanServer == null) {
            mbeanServer = cc.getMBeanServer();
        }
        try {
            ObjectName observedName = new ObjectName(observedObjectName);
            if (name == null) {
                String type = observedName.getKeyProperty("type");
                type = type != null ? type : "UNKNOWN";
                name = mbeanServer.getDefaultDomain() + ":type=CounterMonitor_" + type;
            }
            ourName = new ObjectName(name);
            counterMonitor.setNotify(true);
            counterMonitor.addObservedObject(observedName);
            counterMonitor.setObservedAttribute(attributeName);
            counterMonitor.setGranularityPeriod(granularityPeriod);
            counterMonitor.setDifferenceMode(false);
            counterMonitor.setInitThreshold(threshold);
            counterMonitor.setOffset(offset);
            mbeanServer.registerMBean(counterMonitor, ourName);
            mbeanServer.addNotificationListener(ourName, this, null, new Object());
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to start.
     */
    public void start() throws javax.jbi.JBIException {
        super.start();
        counterMonitor.start();
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to stop.
     */
    public void stop() throws javax.jbi.JBIException {
        counterMonitor.stop();
        super.stop();
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to shut down.
     */
    public void shutDown() throws javax.jbi.JBIException {
        stop();
        if (ourName != null && mbeanServer != null) {
            try{
                mbeanServer.removeNotificationListener(ourName,this);
            } catch(Exception e) {
                throw new JBIException(e);
            }
        }
        super.shutDown();
    }

   
    /**
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    public void handleNotification(Notification notification,Object arg1) {
        try {
            Source source = new StringSource(notification.getMessage());
            InOnly exchange = getExchangeFactory().createInOnlyExchange();
            NormalizedMessage message = exchange.createMessage();
            message.setContent(source);
            exchange.setInMessage(message);
            send(exchange);
        }
        catch (Exception e) {
            logger.error("Failed to send Notification message to the NMR");
        }
        
    }

    protected void validate() throws JBIException {
        if (observedObjectName == null) {
            throw new DeploymentException("observedObjectName is null");
        }
        if (attributeName == null) {
            throw new DeploymentException("attributeName is null");
        }
        if (threshold == null) {
            throw new DeploymentException("threshold is null");
        }
        if (offset == null) {
            throw new DeploymentException("offset is null");
        }
    }

    /**
     * @return Returns the attributeName.
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @param attributeName
     *            The attributeName to set.
     */
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return Returns the counterMonitor.
     */
    public CounterMonitor getCounterMonitor() {
        return counterMonitor;
    }

    /**
     * @param counterMonitor The counterMonitor to set.
     */
    public void setCounterMonitor(CounterMonitor counterMonitor) {
        this.counterMonitor = counterMonitor;
    }

    /**
     * @return Returns the granularityPeriod.
     */
    public long getGranularityPeriod() {
        return granularityPeriod;
    }

    /**
     * @param granularityPeriod The granularityPeriod to set.
     */
    public void setGranularityPeriod(long granularityPeriod) {
        this.granularityPeriod = granularityPeriod;
    }

    /**
     * @return Returns the mbeanServer.
     */
    public MBeanServer getMbeanServer() {
        return mbeanServer;
    }

    /**
     * @param mbeanServer The mbeanServer to set.
     */
    public void setMbeanServer(MBeanServer mbeanServer) {
        this.mbeanServer = mbeanServer;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the observedObjectName.
     */
    public String getObservedObjectName() {
        return observedObjectName;
    }

    /**
     * @param observedObjectName The observedObjectName to set.
     */
    public void setObservedObjectName(String observedObjectName) {
        this.observedObjectName = observedObjectName;
    }

    /**
     * @return Returns the offset.
     */
    public Number getOffset() {
        return offset;
    }

    /**
     * @param offset The offset to set.
     */
    public void setOffset(Number offset) {
        this.offset = offset;
    }

    /**
     * @return Returns the threshold.
     */
    public Number getThreshold() {
        return threshold;
    }

    /**
     * @param threshold The threshold to set.
     */
    public void setThreshold(Number threshold) {
        this.threshold = threshold;
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // We can only receive acks, so do nothing
    }

}
