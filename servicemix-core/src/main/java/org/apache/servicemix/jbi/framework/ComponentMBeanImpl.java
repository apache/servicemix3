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

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.deployment.SharedLibraryList;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.BaseLifeCycle;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.messaging.DeliveryChannelImpl;
import org.apache.servicemix.jbi.messaging.MessagingStats;
import org.apache.servicemix.jbi.util.XmlPersistenceSupport;

/**
 * Defines basic statistics on the Component
 */
public class ComponentMBeanImpl extends BaseLifeCycle implements ComponentMBean {
    
    private static Log log = LogFactory.getLog(ComponentMBeanImpl.class);
    
    private boolean exchangeThrottling;
    private long throttlingTimeout = 100;
    private int throttlingInterval = 1;
       
    private Component component;
    private ComponentLifeCycle lifeCycle;
    private ServiceUnitManager suManager;
    private ComponentContextImpl context;
    private ActivationSpec activationSpec;
    private ObjectName mBeanName;
    private ComponentStats componentStatsMBean;
    private ObjectName componentStatsMBeanName;
    private JBIContainer container;
    private MessagingStats messagingStats;
    private ComponentNameSpace componentName;
    private String description = "POJO Component";
    private boolean pojo;
    private boolean binding;
    private boolean service;
    private File stateFile;
    private String[] sharedLibraries;

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
    public ComponentMBeanImpl(JBIContainer container, 
                              ComponentNameSpace name, 
                              String description, 
                              Component component,
                              boolean binding, 
                              boolean service,
                              String[] sharedLibraries) {
        this.componentName = name;
        this.container = container;
        this.component = component;
        this.description = description;
        this.binding = binding;
        this.service = service;
        this.componentStatsMBean = new ComponentStats(this);
        this.messagingStats = new MessagingStats(name.getName());
        this.sharedLibraries = sharedLibraries;
    }

    /**
     * Register the MBeans for this Component
     * @param context
     * @return ObjectName
     * @throws JBIException
     */
    public ObjectName registerMBeans(ManagementContext context) throws JBIException{
        try{
            mBeanName = context.createObjectName(this);
            context.registerMBean(mBeanName, this, ComponentMBean.class);
            componentStatsMBeanName = context.createObjectName(componentStatsMBean);
            context.registerMBean(componentStatsMBeanName, componentStatsMBean, ComponentStatsMBean.class);
            return mBeanName;
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
        context.unregisterMBean(mBeanName);
        context.unregisterMBean(componentStatsMBeanName);
    }

    /**
     * Set the Context
     * 
     * @param context
     */
    public void setContext(ComponentContextImpl context) {
        this.context = context;
        this.stateFile = context.getEnvironment().getStateFile();
    }

    /**
     * Get the JMX ObjectName for any additional MBean for this component. If there is none, return null.
     * 
     * @return ObjectName the JMX object name of the additional MBean or null if there is no additional MBean.
     */
    public ObjectName getExtensionMBeanName() {
        if (isInitialized() || isStarted() || isStopped()) {
            return lifeCycle.getExtensionMBeanName();
        } else {
            return null;
        }
    }
    
    /**
     * Get the name of the item
     * @return the name
     */
    public String getName() {
        return componentName.getName();
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
        return description;
    }

    
    public void init() throws JBIException {
        log.info("Initializing component: " + getName());
        if (context != null && component != null) {
            DeliveryChannelImpl channel = new DeliveryChannelImpl(this);
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
        super.init();
    }
    
    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void start() throws javax.jbi.JBIException {
        log.info("Starting component: " + getName());
        try {
            doStart();
            persistRunningState();
            getContainer().getRegistry().checkPendingAssemblies();
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
        log.info("Stopping component: " + getName());
        try {
            doStop();
            persistRunningState();
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
        log.info("Shutting down component: " + getName());
        try {
            doShutDown();
            persistRunningState();
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
    
    public void setShutdownStateAfterInstall() {
        setCurrentState(SHUTDOWN);
    }
    
    /**
     * Start the item - doesn't persist the state
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void doStart() throws javax.jbi.JBIException {
        if (isShutDown()) {
            // need to re-initialze before starting
            init();
        }
        if (!isStarted()) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getLifeCycle().getClass().getClassLoader());
                getLifeCycle().start();
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
            super.start();
            initServiceAssemblies();
            startServiceAssemblies();
        }
        fireEvent(ComponentEvent.COMPONENT_STARTED);
    }

    /**
     * Stop the item - doesn't persist the state
     * 
     * @exception javax.jbi.JBIException
     *                if the item fails to stop.
     */
    public void doStop() throws javax.jbi.JBIException {
        if (isUnknown() || isStarted()) {
            stopServiceAssemblies();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getLifeCycle().getClass().getClassLoader());
                getLifeCycle().stop();
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
	        super.stop();
        }
        fireEvent(ComponentEvent.COMPONENT_STOPPED);
    }

    /**
     * Shut down the item - doesn't persist the state
     * 
     * @exception javax.jbi.JBIException if the item fails to shut down.
     */
    public void doShutDown() throws javax.jbi.JBIException {
        // Transition from UNKNOWN to SHUTDOWN is done at installation time
        // In this case or if the component is already shut down, do nothing
        if (!isUnknown() && !isShutDown()) {
            doStop();
            shutDownServiceAssemblies();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getLifeCycle().getClass().getClassLoader());
                getLifeCycle().shutDown();
            } finally {
                Thread.currentThread().setContextClassLoader(loader);
            }
            if (getDeliveryChannel() != null) {
                getDeliveryChannel().close();
                setDeliveryChannel(null);
            }
        }
        super.shutDown();
        fireEvent(ComponentEvent.COMPONENT_SHUTDOWN);
    }
    
    
    /**
     * Set the initial running state of the Component
     * @throws JBIException
     */
    public void setInitialRunningState() throws JBIException{
        if (!isPojo()) {
            String componentName = getName();
            String runningState = getRunningStateFromStore();
            log.info("Setting running state for Component: " + componentName + " to " + runningState);
            if (runningState != null) {
                if (runningState.equals(LifeCycleMBean.STARTED)) {
                    doStart();
                } else if (runningState.equals(LifeCycleMBean.STOPPED)) {
                    doStart();
                    doStop();
                } else if (runningState.equals(LifeCycleMBean.SHUTDOWN)) {
                    doShutDown();
                }
            }
        }
    }
    
    /**
     * Persist the running state
     */
    public void persistRunningState() {
        if (!isPojo()) {
            String componentName = getName();
            try {
                String currentState = getCurrentState();
                Properties props = new Properties();
                props.setProperty("state", currentState);
                XmlPersistenceSupport.write(stateFile, props);
            } catch (IOException e) {
                log.error("Failed to write current running state for Component: " + componentName, e);
            }
        }
    }
   
    /**
     * @return the current running state from disk
     */
    public String getRunningStateFromStore() {
        String result = LifeCycleMBean.UNKNOWN;
        String componentName = getName();
        try {
            Properties props = (Properties) XmlPersistenceSupport.read(stateFile);
            result = props.getProperty("state", result);
        } catch (Exception e) {
            log.error("Failed to read running state for Component: " + componentName, e);
        }
        return result;
    }

    /**
     * @return the capacity of the inbound queue
     */
    public int getInboundQueueCapacity(){
        // TODO: should not be on the delivery channel
        if (getDeliveryChannel() != null) {
            return getDeliveryChannel().getQueueCapacity();
        } else {
            return 0;
        }
    }
    
    /**
     * Set the inbound queue capacity
     * @param value
     */
    public void setInboundQueueCapacity(int value){
        // TODO: should not be on the delivery channel
        if (getDeliveryChannel() != null) {
            getDeliveryChannel().setQueueCapacity(value);
        }
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
     * @return Returns the pojo.
     */
    public boolean isPojo() {
        return pojo;
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

    /**
     * @return the ObjectName for the stats MBean for this Component - or null if it doesn't exist
     */
    public ObjectName getStatsMBeanName(){
        return componentStatsMBeanName;
    }
    /**
     * Get an array of MBeanAttributeInfo
     * 
     * @return array of AttributeInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "inboundQueueCapacity", "capacity of the inbound queue");
        helper.addAttribute(getObjectToManage(), "exchangeThrottling", "apply throttling");
        helper.addAttribute(getObjectToManage(), "throttlingTimeout", "timeout for throttling");
        helper.addAttribute(getObjectToManage(), "throttlingInterval", "exchange intervals before throttling");
        helper.addAttribute(getObjectToManage(), "extensionMBeanName", "extension mbean name");
        helper.addAttribute(getObjectToManage(), "statsMBeanName", "Statistics mbean name");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

    public void firePropertyChanged(String name, Object oldValue, Object newValue) {
        super.firePropertyChanged(name, oldValue, newValue);
    }

    protected void initServiceAssemblies() throws DeploymentException {
    }

    protected void startServiceAssemblies() throws DeploymentException {
    }

    protected void stopServiceAssemblies() throws DeploymentException {
        Registry registry = getContainer().getRegistry();
        String[] sas = registry.getDeployedServiceAssembliesForComponent(getName());
        for (int i = 0; i < sas.length; i++) {
            ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(sas[i]);
            if (sa.isStarted()) {
                try {
                    sa.stop(false, false);
                    registry.addPendingAssembly(sa);
                } catch (Exception e) {
                    log.error("Error stopping service assembly " + sas[i]);
                }
            }
        }
    }

    protected void shutDownServiceAssemblies() throws DeploymentException {
        Registry registry = getContainer().getRegistry();
        String[] sas = registry.getDeployedServiceAssembliesForComponent(getName());
        for (int i = 0; i < sas.length; i++) {
            ServiceAssemblyLifeCycle sa = registry.getServiceAssembly(sas[i]);
            if (sa.isStopped()) {
                try {
                    sa.shutDown(false);
                    registry.addPendingAssembly(sa);
                } catch (Exception e) {
                    log.error("Error shutting down service assembly " + sas[i]);
                }
            }
        }
    }
    
    protected void fireEvent(int type) {
        ComponentEvent event = new ComponentEvent(this, type);
        ComponentListener[] listeners = (ComponentListener[]) getContainer().getListeners(ComponentListener.class);
        for (int i = 0; i < listeners.length; i++) {
            switch (type) {
            case ComponentEvent.COMPONENT_STARTED:
                listeners[i].componentStarted(event);
                break;
            case ComponentEvent.COMPONENT_STOPPED:
                listeners[i].componentStopped(event);
                break;
            case ComponentEvent.COMPONENT_SHUTDOWN:
                listeners[i].componentShutDown(event);
                break;
            }
        }
        
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

    public JBIContainer getContainer() {
        return container;
    }

    /**
     * @return Returns the messagingStats.
     */
    public MessagingStats getMessagingStats() {
        return messagingStats;
    }

    public Component getComponent() {
        return component;
    }
    
    public ComponentNameSpace getComponentNameSpace() {
        return componentName;
    }

    public ComponentContextImpl getContext() {
        return context;
    }
    public ObjectName getMBeanName() {
        return mBeanName;
    }
    public boolean isBinding() {
        return binding;
    }
    public boolean isService() {
        return service;
    }

    public void setPojo(boolean pojo) {
        this.pojo = pojo;
    }

    public boolean isEngine() {
        return service;
    }

    /**
     * @return the sharedLibraries
     */
    public String[] getSharedLibraries() {
        return sharedLibraries;
    }

}
