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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.messaging.DeliveryChannelImpl;
import org.apache.servicemix.jbi.messaging.MessagingStats;
import org.apache.servicemix.jbi.util.XmlPersistenceSupport;

/**
 * ComponentConnector is used internally for message routing.
 * 
 * @version $Revision$
 */
public class LocalComponentConnector extends ComponentConnector {

    private static final Log log = LogFactory.getLog(LocalComponentConnector.class);
    private Component component;
    private ComponentLifeCycle lifeCycle;
    private ServiceUnitManager suManager;
    private ComponentContextImpl context;
    private ActivationSpec activationSpec;
    private ObjectName extendedMBeanName;
    private ComponentMBeanImpl componentMBean;
    private ObjectName componentMBeanName;
    private ComponentStats componentStatsMBean;
    private ObjectName componentStatsMBeanName;
    private JBIContainer container;
    private MessagingStats messagingStats;
    private boolean exchangeThrottling;
    private long throttlingTimeout = 100;
    private int throttlingInterval = 1;
    private boolean pojo;

    /**
     * Construct with it's id and delivery channel Id
     * 
     * @param name
     * @param description
     * @param component
     * @param dc
     * @param binding
     * @param service
     */
    public LocalComponentConnector(JBIContainer container, ComponentNameSpace name, String description, Component component,
            boolean binding, boolean service) {
        super(name);
        this.container = container;
        this.component = component;
        packet.setDescription(description);
        packet.setBinding(binding);
        packet.setService(service);
        this.componentMBean = new ComponentMBeanImpl(this);
        this.componentStatsMBean = new ComponentStats(this);
        this.messagingStats = new MessagingStats(name.getName());
    }
    
    
    /**
     * Register the MBeans for this Component
     * @param context
     * @return ObjectName
     * @throws JBIException
     */
    public ObjectName registerMBeans(ManagementContext context) throws JBIException{
        try{
            componentMBeanName = context.createObjectName(componentMBean);
            context.registerMBean(componentMBeanName, componentMBean, ComponentMBean.class);
            componentStatsMBeanName = context.createObjectName(componentStatsMBean);
            context.registerMBean(componentStatsMBeanName, componentStatsMBean, ComponentStatsMBean.class);
            return componentMBeanName;
        }catch(Exception e){
            String errorStr="Failed to register MBeans";
            log.error(errorStr,e);
            throw new JBIException(errorStr,e);
        }
    }
    
    /**
     * Unregister Component MBeans
     * @param context
     * @throws JBIException
     */
    public void unregisterMbeans(ManagementContext context) throws JBIException{
        context.unregisterMBean(componentMBeanName);
        context.unregisterMBean(componentStatsMBeanName);
    }

    /**
     * @return true if the Component is local to the Container
     */
    public boolean isLocal() {
        return true;
    }

    /**
     * Get the Context
     * 
     * @return the Context
     */
    public ComponentContextImpl getContext() {
        return context;
    }

    /**
     * Set the Context
     * 
     * @param context
     */
    public void setContext(ComponentContextImpl context) {
        this.context = context;
    }

    /**
     * Add an activated endpoint
     * 
     * @param endpoint
     */
    public void addActiveEndpoint(ServiceEndpoint endpoint) {
        packet.addActiveEndpoint(endpoint);
    }

    /**
     * remove an activated endpoint
     * 
     * @param endpoint
     */
    public void removeActiveEndpoint(ServiceEndpoint endpoint) {
        packet.removeActiveEndpoint(endpoint);
    }

    /**
     * Add an external activated endpoint
     * 
     * @param endpoint
     */
    public void addExternalActiveEndpoint(ServiceEndpoint endpoint) {
        packet.addExternalActiveEndpoint(endpoint);
    }

    /**
     * remove an external activated endpoint
     * 
     * @param endpoint
     */
    public void removeExternalActiveEndpoint(ServiceEndpoint endpoint) {
        packet.removeExternalActiveEndpoint(endpoint);
    }

    /**
     * Get the Set of external activated endpoints
     * 
     * @return the activated endpoint Set
     */
    public Set getExternalActiveEndpoints() {
        return packet.getExternalActiveEndpoints();
    }

    /**
     * @return Returns the component.
     */
    public Component getComponent() {
        return component;
    }

    /**
     * @param component
     *            The component to set.
     */
    public void setComponent(Component component) {
        this.component = component;
    }

    /**
     * @return Returns the deliveryChannel.
     */
    public DeliveryChannelImpl getDeliveryChannel() {
        return (DeliveryChannelImpl) context.getDeliveryChannel();
    }

    /**
     * @param deliveryChannel
     *            The deliveryChannel to set.
     */
    public void setDeliveryChannel(DeliveryChannelImpl deliveryChannel) {
        context.setDeliveryChannel(deliveryChannel);
    }

    /**
     * @return the ActivateionSpec
     */
    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    /**
     * Set the ActivationSpec
     * 
     * @param activationSpec
     */
    public void setActivationSpec(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
    }

    /**
     * @return Returns the mbeanName.
     */
    public ObjectName getMBeanName() {
        return componentMBeanName;
    }

    /**
     * @return Returns the ComponentMBean
     */
    public ComponentMBeanImpl getComponentMBean() {
        return componentMBean;
    }
    
    /**
    * @return Returns the stats mbeanName.
    */
   public ObjectName getStatsMBeanName() {
       return componentStatsMBeanName;
   }

   /**
    * @return Returns the ComponentMBean
    */
   public ComponentStats getComponentStatsMBean() {
       return componentStatsMBean;
   }

    /**
     * @return Returns the extendedMBeanName.
     */
    public ObjectName getExtendedMBeanName() {
        return extendedMBeanName;
    }

    /**
     * @param extendedMBeanName
     *            The extendedMBeanName to set.
     */
    public void setExtendedMBeanName(ObjectName extendedMBeanName) {
        this.extendedMBeanName = extendedMBeanName;
    }

    /**
     * Initialize the Component
     * 
     * @throws JBIException
     */
    public void init() throws JBIException {
        if (context != null && component != null) {
            DeliveryChannelImpl channel = new DeliveryChannelImpl(container, context.getComponentName());
            channel.setConnector(this);
            channel.setContext(context);
            context.setDeliveryChannel(channel);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getLifeCycle().getClass().getClassLoader());
                getLifeCycle().init(context);
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
        }
    }

    /**
     * @return Returns the pojo.
     */
    public boolean isPojo() {
        return pojo;
    }

    /**
     * @param pojo
     *            The pojo to set.
     */
    public void setPojo(boolean pojo) {
        this.pojo = pojo;
    }

    public ComponentLifeCycle getLifeCycle() {
        if (lifeCycle == null) {
            lifeCycle = component.getLifeCycle();
        }
        return lifeCycle;
    }

    public ServiceUnitManager getServiceUnitManager() {
        if (suManager == null) {
            suManager = component.getServiceUnitManager();
        }
        return suManager;
    }

    /**
     * write the current running state of the Component to disk
     */
    void writeRunningState() {
        if (!isPojo()) {
            String componentName = getComponentNameSpace().getName();
            if (componentMBean != null) {
                try {
                    String currentState = componentMBean.getCurrentState();
                    File stateFile = context.getContainer().getEnvironmentContext()
                            .getComponentStateFile(componentName);
                    Properties props = new Properties();
                    props.setProperty("state", currentState);
                    XmlPersistenceSupport.write(stateFile, props);
                } catch (IOException e) {
                    log.error("Failed to write current running state for Component: " + componentName, e);
                }
            } else {
                log.warn("No componentMBean available for Component: " + componentName);
            }
        }
    }

    /**
     * Read the last running state from disk and set the component to this state
     * 
     * @throws JBIException
     */
    void setRunningStateFromStore() throws JBIException {
        if (!isPojo()) {
            String componentName = getComponentNameSpace().getName();
            String runningState = getRunningStateFromStore();
            log.info("Setting running state for Component: " + componentName + " to " + runningState);
            if (runningState != null && componentMBean != null) {
                if (runningState.equals(LifeCycleMBean.STARTED)) {
                    componentMBean.doStart();
                } else if (runningState.equals(LifeCycleMBean.STOPPED)) {
                    componentMBean.doStart();
                    componentMBean.doStop();
                } else if (runningState.equals(LifeCycleMBean.SHUTDOWN)) {
                    componentMBean.doShutDown();
                }
            }
        }
    }

    /**
     * @return the current running state from disk
     */
    String getRunningStateFromStore() {
        String result = LifeCycleMBean.UNKNOWN;
        if (componentMBean != null) {
            String componentName = getComponentNameSpace().getName();
            try {
                File stateFile = context.getContainer().getEnvironmentContext().getComponentStateFile(componentName);
                Properties props = (Properties) XmlPersistenceSupport.read(stateFile);
                result = props.getProperty("state", result);
            } catch (Exception e) {
                log.error("Failed to read running state for Component: " + componentName, e);
            }
        }
        return result;
    }

    /**
     * @return Returns the container.
     */
    public JBIContainer getContainer() {
        return container;
    }

    /**
     * @return Returns the messagingStats.
     */
    public MessagingStats getMessagingStats() {
        return messagingStats;
    }


    /**
     * Is MessageExchange sender throttling enabled ?
     * 
     * @return true if throttling enabled
     */
    public boolean isExchangeThrottling() {
        return exchangeThrottling;
    }

    /**
     * Set message throttling
     * 
     * @param value
     */
    public void setExchangeThrottling(boolean value) {
        this.exchangeThrottling = value;
    }

    /**
     * Get the throttling timeout
     * 
     * @return throttling tomeout (ms)
     */
    public long getThrottlingTimeout() {
        return throttlingTimeout;
    }

    /**
     * Set the throttling timout
     * 
     * @param value (ms)
     */
    public void setThrottlingTimeout(long value) {
        throttlingTimeout = value;
    }

    /**
     * Get the interval for throttling - number of Exchanges set before the throttling timeout is applied
     * 
     * @return interval for throttling
     */
    public int getThrottlingInterval() {
        return throttlingInterval;
    }

    /**
     * Set the throttling interval number of Exchanges set before the throttling timeout is applied
     * 
     * @param value
     */
    public void setThrottlingInterval(int value) {
        throttlingInterval = value;
    }

}
