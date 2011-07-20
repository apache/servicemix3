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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.Callable;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.deployment.Descriptor;
import org.apache.servicemix.jbi.deployment.DescriptorFactory;
import org.apache.servicemix.jbi.deployment.ServiceUnit;
import org.apache.servicemix.jbi.deployment.Services;
import org.apache.servicemix.jbi.event.ServiceUnitEvent;
import org.apache.servicemix.jbi.event.ServiceUnitListener;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.MBeanInfoProvider;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceUnitLifeCycle implements ServiceUnitMBean, MBeanInfoProvider {

    /**
     * Default timeout for deployment operation.
     */
    private static final long DEFAULT_DEPLOYMENT_OPERATION_TIMEOUT = 2 * 60 * 1000;

    /**
     * Name of the system property that contains deployment operation timeout.
     */
    private static final String DEPLOYMENT_OPERATION_TIMEOUT_PROPERTY = "org.apache.servicemix.deployment.timeout";

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ServiceUnitLifeCycle.class);

    private ServiceUnit serviceUnit;

    private String currentState = SHUTDOWN;
    
    private String serviceAssembly;
    
    private Registry registry;

    private PropertyChangeListener listener;
    
    private Services services;
    
    private File rootDir;
    
    public ServiceUnitLifeCycle(ServiceUnit serviceUnit, 
                                String serviceAssembly,
                                Registry registry,
                                File rootDir) {
        this.serviceUnit = serviceUnit;
        this.serviceAssembly = serviceAssembly;
        this.registry = registry;
        this.rootDir = rootDir;
        Descriptor d = DescriptorFactory.buildDescriptor(rootDir);
        if (d != null) {
            services = d.getServices();
        }
    }

    /**
     * Deployment operation executor that supports timeout.
     */
    private class TimedOutExecutor {

        private final ClassLoader classLoader;
        private final String name;
        private final Callable<?> task;
        private Exception fault;

        /**
         * Constructor.
         *
         * @param classLoader the class loader to use for task execution.
         * @param name Name of this executor. Used for error reporting purposes.
         * @param task Task to execute. Return value of task is not used.
         */
        public TimedOutExecutor(ClassLoader classLoader, String name, Callable<?> task) {
            this.classLoader = classLoader;
            this.name = name;
            this.task = task;
        }

        private class Worker implements Runnable {

            public void run() {
                try {
                    task.call();
                } catch (Exception e) {
                    fault = e;
                }
            }
        }

        /**
         * Executes task respecting timeout.
         *
         * @param timeout Timeout for task execution in milliseconds.
         * @throws DeploymentException If task throws an exception, current thread is interrupted or timeout expires.
         */
        public void execute(long timeout) throws DeploymentException {
            Thread worker = new Thread(new Worker(), "AsyncDeployer for " + name);
            worker.setContextClassLoader(classLoader);
            worker.start();
            try {
                worker.join(timeout);
                if (worker.isAlive()) {
                    worker.interrupt();
                    throw new DeploymentException("Timeout expired while waiting for async operation " + name);
                } else {
                    if (fault != null) {
                        throw new DeploymentException("Error while executing async operation " + name, fault);
                    }
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new DeploymentException("Interrupted while waiting for async operation " + name, interruptedException);
            }
        }
    }

    /**
     * Returns effective deployment operation timeout.
     *
     * @return Timeout in milliseconds.
     */
    private long getDeploymentTimeout() {
        String propertyValue = System.getProperty(DEPLOYMENT_OPERATION_TIMEOUT_PROPERTY);
        if (propertyValue == null) {
            return DEFAULT_DEPLOYMENT_OPERATION_TIMEOUT;
        }
        try {
            return Long.parseLong(propertyValue);
        } catch (NumberFormatException numberFormatException) {
            LOGGER.warn("Wrong value for system property {}", DEPLOYMENT_OPERATION_TIMEOUT_PROPERTY, numberFormatException);
            return DEFAULT_DEPLOYMENT_OPERATION_TIMEOUT;
        }
    }

    /**
     * Initialize the service unit.
     *
     * @throws DeploymentException 
     */
    public void init() throws DeploymentException {
        LOGGER.info("Initializing service unit: {}", getName());
        checkComponentStarted("init");
        final ServiceUnitManager sum = getServiceUnitManager();
        final File path = getServiceUnitRootPath();
        new TimedOutExecutor(getComponentClassLoader(), "init " + getName(),
                new Callable<Object>() {
                    public Object call() throws Exception {
                        sum.init(getName(), path.getAbsolutePath());
                        return null;
                    }
                }).execute(getDeploymentTimeout());
        currentState = STOPPED;
    }
    
    /**
     * Start the service unit.
     *
     * @throws DeploymentException 
     */
    public void start() throws DeploymentException {
        LOGGER.info("Starting service unit: {}", getName());
        checkComponentStarted("start");
        final ServiceUnitManager sum = getServiceUnitManager();
        new TimedOutExecutor(getComponentClassLoader(), "start " + getName(),
                new Callable<Object>() {
                    public Object call() throws Exception {
                        sum.start(getName());
                        return null;
                    }
                }).execute(getDeploymentTimeout());
        currentState = STARTED;
    }

    /**
     * Stop the service unit. This suspends current messaging activities.
     *
     * @throws DeploymentException 
     */
    public void stop() throws DeploymentException {
        LOGGER.info("Stopping service unit: {}", getName());
        checkComponentStarted("stop");
        final ServiceUnitManager sum = getServiceUnitManager();
        new TimedOutExecutor(getComponentClassLoader(), "stop " + getName(),
                new Callable<Object>() {
                    public Object call() throws Exception {
                        sum.stop(getName());
                        return null;
                    }
                }).execute(getDeploymentTimeout());
        currentState = STOPPED;
    }

    /**
     * Shut down the service unit. 
     * This releases resources, preparatory to uninstallation.
     *
     * @throws DeploymentException 
     */
    public void shutDown() throws DeploymentException {
        LOGGER.info("Shutting down service unit: {}", getName());
        checkComponentStartedOrStopped("shutDown");
        final ServiceUnitManager sum = getServiceUnitManager();
        new TimedOutExecutor(getComponentClassLoader(), "shutdown " + getName(),
                new Callable<Object>() {
                    public Object call() throws Exception {
                        sum.shutDown(getName());
                        return null;
                    }
                }).execute(getDeploymentTimeout());
        currentState = SHUTDOWN;
    }

    /**
     * @return the currentState as a String
     */
    public String getCurrentState() {
        return currentState;
    }

    public boolean isShutDown() {
        return currentState.equals(SHUTDOWN);
    }

    public boolean isStopped() {
        return currentState.equals(STOPPED);
    }

    public boolean isStarted() {
        return currentState.equals(STARTED);
    }
    
    /**
     * @return the name of the ServiceAssembly
     */
    public String getName() {
        return serviceUnit.getIdentification().getName();
    }

    /**
     * @return the description of the ServiceAssembly
     */
    public String getDescription() {
        return serviceUnit.getIdentification().getDescription();
    }
    
    public String getComponentName() {
        return serviceUnit.getTarget().getComponentName();
    }

    public String getServiceAssembly() {
        return serviceAssembly;
    }

    public String getDescriptor() {
        File suDir = getServiceUnitRootPath();
        return DescriptorFactory.getDescriptorAsText(suDir);
    }
    
    public Services getServices() {
        return services;
    }

    protected void checkComponentStarted(String task) throws DeploymentException {
        String componentName = getComponentName();
        String suName = getName();
        ComponentMBeanImpl lcc = registry.getComponent(componentName);
        if (lcc == null) {
            throw ManagementSupport.componentFailure("deploy", componentName, "Target component "
                            + componentName + " for service unit " + suName + " is not installed");
        }
        if (!lcc.isStarted()) {
            throw ManagementSupport.componentFailure("deploy", componentName, "Target component "
                            + componentName + " for service unit " + suName + " is not started");
        }
        if (lcc.getServiceUnitManager() == null) {
            throw ManagementSupport.componentFailure("deploy", componentName, "Target component "
                            + componentName + " for service unit " + suName + " does not accept deployments");
        }
    }
    
    protected void checkComponentStartedOrStopped(String task) throws DeploymentException {
        String componentName = getComponentName();
        String suName = getName();
        ComponentMBeanImpl lcc = registry.getComponent(componentName);
        if (lcc == null) {
            throw ManagementSupport.componentFailure("deploy", componentName, "Target component "
                            + componentName + " for service unit " + suName + " is not installed");
        }
        if (!lcc.isStarted() && !lcc.isStopped()) {
            throw ManagementSupport.componentFailure("deploy", componentName, "Target component "
                            + componentName + " for service unit " + suName + " is not started");
        }
        if (lcc.getServiceUnitManager() == null) {
            throw ManagementSupport.componentFailure("deploy", componentName, "Target component "
                            + componentName + " for service unit " + suName + " does not accept deployments");
        }
    }
    
    protected File getServiceUnitRootPath() {
        return rootDir;
    }
    
    protected ServiceUnitManager getServiceUnitManager() {
        ComponentMBeanImpl lcc = registry.getComponent(getComponentName());
        return lcc.getServiceUnitManager();
    }

    protected ClassLoader getComponentClassLoader() {
        ComponentMBeanImpl lcc = registry.getComponent(getComponentName());
        // TODO: should retrieve the real component class loader
        return lcc.getComponent().getClass().getClassLoader();
    }

    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "currentState", "current state of the service unit");
        helper.addAttribute(getObjectToManage(), "name", "name of the service unit");
        helper.addAttribute(getObjectToManage(), "componentName", "component name of the service unit");
        helper.addAttribute(getObjectToManage(), "serviceAssembly", "service assembly name of the service unit");
        helper.addAttribute(getObjectToManage(), "description", "description of the service unit");
        return helper.getAttributeInfos();
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "getDescriptor", "retrieve the jbi descriptor for this unit");
        return helper.getOperationInfos();
    }

    public Object getObjectToManage() {
        return this;
    }

    public String getType() {
        return "ServiceUnit";
    }

    public String getSubType() {
        return getComponentName();
    }

    public void setPropertyChangeListener(PropertyChangeListener l) {
        this.listener = l;
    }

    protected void firePropertyChanged(String name, Object oldValue, Object newValue) {
        PropertyChangeListener l = listener;
        if (l != null) {
            PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
            l.propertyChange(event);
        }
    }

    public String getKey() {
        return getComponentName() + "/" + getName();
    }

    protected void fireEvent(int type) {
        ServiceUnitEvent event = new ServiceUnitEvent(this, type);
        ServiceUnitListener[] listeners = (ServiceUnitListener[]) registry.getContainer().getListeners(ServiceUnitListener.class);
        for (int i = 0; i < listeners.length; i++) {
            switch (type) {
            case ServiceUnitEvent.UNIT_STARTED:
                listeners[i].unitStarted(event);
                break;
            case ServiceUnitEvent.UNIT_STOPPED:
                listeners[i].unitStopped(event);
                break;
            case ServiceUnitEvent.UNIT_SHUTDOWN:
                listeners[i].unitShutDown(event);
                break;
            default:
                break;
            }
        }
    }

}
