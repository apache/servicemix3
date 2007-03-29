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

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.event.ComponentAdapter;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.event.EndpointAdapter;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.EndpointListener;
import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author gnodet
 * @org.apache.xbean.XBean element="statistics"
 */
public class StatisticsService extends BaseSystemService implements StatisticsServiceMBean {

    private static final Log log = LogFactory.getLog(StatisticsService.class);
    
    private ConcurrentHashMap<String, ComponentStats> componentStats = new ConcurrentHashMap<String, ComponentStats>();
    private ConcurrentHashMap<String, EndpointStats> endpointStats = new ConcurrentHashMap<String, EndpointStats>();
    
    private ComponentListener componentListener;
    private EndpointListener endpointListener;
    private ExchangeListener exchangeListener;
    private boolean dumpStats = true;
    private long statsInterval = 5;
    private Timer statsTimer;
    private TimerTask timerTask;

    /**
     * @return the statsInterval
     */
    public long getStatsInterval() {
        return statsInterval;
    }

    /**
     * @param statsInterval the statsInterval to set
     */
    public void setStatsInterval(long statsInterval) {
        this.statsInterval = statsInterval;
    }

    /**
     * @return the dumpStats
     */
    public boolean isDumpStats() {
        return dumpStats;
    }

    /**
     * @param dumpStats the dumpStats to set
     */
    public void setDumpStats(boolean value) {
        if (dumpStats && !value) {
            if (timerTask != null) {
                timerTask.cancel();
            }
        }
        else if (!dumpStats && value) {
            dumpStats = value;//scheduleStatsTimer relies on dumpStats value
            scheduleStatsTimer();
        }
        dumpStats = value;
    }

    protected Class<StatisticsServiceMBean> getServiceMBean() {
        return StatisticsServiceMBean.class;
    }

    public String getDescription() {
        return "EndpointStats service";
    }
    
    public void resetAllStats() {
        for (Iterator<ComponentStats> it = componentStats.values().iterator(); it.hasNext();) {
            ComponentStats stats = it.next();
            stats.reset();
        }
        for (Iterator<EndpointStats> it = endpointStats.values().iterator(); it.hasNext();) {
            EndpointStats stats = it.next();
            stats.reset();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.management.LifeCycleMBean#start()
     */
    public void start() throws javax.jbi.JBIException {
        super.start();
        this.container.addListener(exchangeListener);
        if (isDumpStats()) {
            scheduleStatsTimer();
        }
    }

    /* (non-Javadoc)
     * @see javax.jbi.management.LifeCycleMBean#stop()
     */
    public void stop() throws javax.jbi.JBIException {
        this.container.removeListener(exchangeListener);
        super.stop();
        for (Iterator<ComponentStats> it = componentStats.values().iterator(); it.hasNext();) {
            ComponentStats stats = it.next();
            stats.close();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (statsTimer != null) {
            statsTimer.cancel();
        }
    }

    public void init(JBIContainer container) throws JBIException {
        endpointListener = new EndpointAdapter() {
            public void internalEndpointRegistered(EndpointEvent event) {
                onEndpointRegistered(event);
            }
            public void internalEndpointUnregistered(EndpointEvent event) {
                onEndpointUnregistered(event);
            }
            public void externalEndpointRegistered(EndpointEvent event) {
                onEndpointRegistered(event);
            }
            public void externalEndpointUnregistered(EndpointEvent event) {
                onEndpointUnregistered(event);
            }
        };
        componentListener = new ComponentAdapter() {
            public void componentInitialized(ComponentEvent event) {
                onComponentInitialized(event);
            }
            public void componentShutDown(ComponentEvent event) {
                onComponentShutDown(event);
            }
        };
        exchangeListener = new ExchangeListener() {
            public void exchangeSent(ExchangeEvent event) {
                onExchangeSent(event);
            }
            public void exchangeAccepted(ExchangeEvent event) {
                onExchangeAccepted(event);
            }
        };
        container.addListener(componentListener);
        container.addListener(endpointListener);
        super.init(container);
    }
    
    protected void onComponentInitialized(ComponentEvent event) {
        ComponentMBeanImpl component = event.getComponent();
        String key = component.getName();
        ComponentStats stats = new ComponentStats(component);
        componentStats.putIfAbsent(key, stats);
        // Register MBean
        ManagementContext context= container.getManagementContext();
        try {
            context.registerMBean(context.createObjectName(context.createObjectNameProps(stats, true)), 
                    stats, 
                    ComponentStatsMBean.class);
        } catch (Exception e) {
            log.info("Unable to register component statistics MBean: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Unable to register component statistics MBean", e);
            }
        }
    }
    
    protected void onComponentShutDown(ComponentEvent event) {
        ComponentMBeanImpl component = event.getComponent();
        String key = component.getName();
        ComponentStats stats = componentStats.remove(key);
        if (stats == null) {
            return;
        }
        // Register MBean
        ManagementContext context= container.getManagementContext();
        try {
            context.unregisterMBean(context.createObjectName(context.createObjectNameProps(stats, true)));
        } catch (Exception e) {
            log.info("Unable to unregister component statistics MBean: " + e);
            if (log.isDebugEnabled()) {
                log.debug("Unable to unregister component statistics MBean", e);
            }
        }
    }
    
    protected void onEndpointRegistered(EndpointEvent event) {
        AbstractServiceEndpoint endpoint = (AbstractServiceEndpoint) event.getEndpoint();
        String key = EndpointSupport.getUniqueKey(endpoint);
        ComponentStats compStats = componentStats.get(endpoint.getComponentNameSpace().getName()); 
        EndpointStats stats = new EndpointStats(endpoint, compStats.getMessagingStats());
        endpointStats.putIfAbsent(key, stats);
        // Register MBean
        ManagementContext context= container.getManagementContext();
        try {
            context.registerMBean(context.createObjectName(context.createObjectNameProps(stats, true)), 
                                  stats, 
                                  EndpointStatsMBean.class);
        } catch (Exception e) {
            log.info("Unable to register endpoint statistics MBean: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Unable to register endpoint statistics MBean", e);
            }
        }
    }
    
    protected void onEndpointUnregistered(EndpointEvent event) {
        AbstractServiceEndpoint endpoint = (AbstractServiceEndpoint) event.getEndpoint();
        String key = EndpointSupport.getUniqueKey(endpoint);
        EndpointStats stats = endpointStats.remove(key);
        // Register MBean
        ManagementContext context= container.getManagementContext();
        try {
            context.unregisterMBean(context.createObjectName(context.createObjectNameProps(stats, true)));
        } catch (Exception e) {
            log.info("Unable to unregister endpoint statistics MBean: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Unable to unregister endpoint statistics MBean", e);
            }
        }
    }

    protected void onExchangeSent(ExchangeEvent event) {
        MessageExchange me = event.getExchange();
        // This is a new exchange sent by a consumer
        if (me.getStatus() == ExchangeStatus.ACTIVE &&
            me.getRole() == Role.CONSUMER && 
            me.getMessage("out") == null && 
            me.getFault() == null &&
            me instanceof MessageExchangeImpl) 
        {
            MessageExchangeImpl mei = (MessageExchangeImpl) me;
            String source = (String) me.getProperty(JbiConstants.SENDER_ENDPOINT);
            if (source == null) {
                source = mei.getSourceId().getName();
                ComponentStats stats = componentStats.get(source);
                stats.incrementOutbound();
            } else {
                ServiceEndpoint[] ses = getContainer().getRegistry().getEndpointRegistry().getAllEndpointsForComponent(mei.getSourceId());
                for (int i = 0; i < ses.length; i++) {
                    if (EndpointSupport.getKey(ses[i]).equals(source)) {
                        source = EndpointSupport.getUniqueKey(ses[i]);
                        EndpointStats stats = endpointStats.get(source);
                        stats.incrementOutbound();
                        break;
                    }
                }
            }                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
        }
    }
    
    protected void onExchangeAccepted(ExchangeEvent event) {
        MessageExchange me = event.getExchange();
        // This is a new exchange sent by a consumer
        if (me.getStatus() == ExchangeStatus.ACTIVE &&
            me.getRole() == Role.PROVIDER && 
            me.getMessage("out") == null && 
            me.getFault() == null &&
            me instanceof MessageExchangeImpl) 
        {
            String source = EndpointSupport.getUniqueKey(me.getEndpoint());
            EndpointStats stats = endpointStats.get(source);
            stats.incrementInbound();
        }        
    }
    
    protected void scheduleStatsTimer() {
        if (statsTimer == null) {
            statsTimer = new Timer(true);
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new TimerTask() {
            public void run() {
                doDumpStats();
            }
        };
        long interval = statsInterval * 1000;
        statsTimer.scheduleAtFixedRate(timerTask, interval, interval);
    }
    
    protected void doDumpStats() {
        for (Iterator<ComponentStats> it = componentStats.values().iterator(); it.hasNext();) {
            ComponentStats stats = it.next();
            stats.dumpStats();
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
        helper.addAttribute(getObjectToManage(), "dumpStats", "Periodically dump Component statistics");
        helper.addAttribute(getObjectToManage(), "statsInterval", "Interval (secs) before dumping statistics");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "resetAllStats", "reset all statistics");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

}
