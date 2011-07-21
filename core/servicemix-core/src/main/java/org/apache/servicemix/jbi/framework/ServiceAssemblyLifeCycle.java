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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.servicemix.jbi.container.ServiceAssemblyEnvironment;
import org.apache.servicemix.jbi.deployment.Connection;
import org.apache.servicemix.jbi.deployment.Consumes;
import org.apache.servicemix.jbi.deployment.DescriptorFactory;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.deployment.Services;
import org.apache.servicemix.jbi.event.ServiceAssemblyEvent;
import org.apache.servicemix.jbi.event.ServiceAssemblyListener;
import org.apache.servicemix.jbi.management.AttributeInfoHelper;
import org.apache.servicemix.jbi.management.MBeanInfoProvider;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.util.XmlPersistenceSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ComponentConnector is used internally for message routing
 * 
 * @version $Revision$
 */
public class ServiceAssemblyLifeCycle implements ServiceAssemblyMBean, MBeanInfoProvider {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ServiceAssemblyLifeCycle.class);

    private ServiceAssembly serviceAssembly;

    private String currentState = SHUTDOWN;

    private ServiceUnitLifeCycle[] sus;
    
    private Registry registry;

    private PropertyChangeListener listener;
    
    private ServiceAssemblyEnvironment env;
    
    /**
     * Construct a LifeCycle
     * 
     * @param sa
     * @param env
     * @param registry
     */
    public ServiceAssemblyLifeCycle(ServiceAssembly sa, 
                                    ServiceAssemblyEnvironment env,
                                    Registry registry) {
        this.serviceAssembly = sa;
        this.env = env;
        this.registry = registry;
    }
    
    protected void setServiceUnits(ServiceUnitLifeCycle[] serviceUnits) {
        this.sus = serviceUnits;
    }

    /**
     * Initialize all SUs in Service Assembly.
     *
     * @return Result/Status of this operation
     * @throws Exception
     */
    public synchronized String init() throws Exception {
        LOGGER.info("Initializing service assembly: {}", getName());
        // Init service units
        List<Element> componentFailures = new ArrayList<Element>();
        for (int i = 0; i < sus.length; i++) {
            if (!sus[i].isStarted()) { 
                sus[i].init();
            }
        } 
        if (componentFailures.size() == 0) {
            return ManagementSupport.createSuccessMessage("init");
        } else {
            throw ManagementSupport.failure("init", componentFailures);
        }
    }

    /**
     * Start a Service Assembly and put it in the STARTED state.
     *
     * @return Result/Status of this operation.
     * @throws Exception
     */
    public String start() throws Exception {
        return start(true);
    }
    
    public synchronized String start(boolean writeState) throws Exception {
        LOGGER.info("Starting service assembly: {}", getName());
        // Start connections
        try {
            startConnections();
        } catch (JBIException e) {
            throw ManagementSupport.failure("start", e.getMessage());
        }
        // Start service units
        List<Element> componentFailures = new ArrayList<Element>();
        for (int i = 0; i < sus.length; i++) {
            if (sus[i].isShutDown()) {
                try {
                    sus[i].init();
                } catch (DeploymentException e) {
                    componentFailures.add(getComponentFailure(e, "start", sus[i].getComponentName()));
                }
            }
        }
        for (int i = 0; i < sus.length; i++) {
            if (sus[i].isStopped()) {
                try {
                    sus[i].start();
                } catch (DeploymentException e) {
                    componentFailures.add(getComponentFailure(e, "start", sus[i].getComponentName()));
                }
            }
        }
        if (componentFailures.size() == 0) {
            currentState = STARTED;
            if (writeState) {
                writeRunningState();
            }
            fireEvent(ServiceAssemblyEvent.ASSEMBLY_STARTED);
            return ManagementSupport.createSuccessMessage("start");
        } else {
            throw ManagementSupport.failure("start", componentFailures);
        }
    }

    /**
     * Stops the service assembly and puts it in STOPPED state.
     * 
     * @return Result/Status of this operation.
     * @throws Exception 
     */
    public String stop() throws Exception {
        return stop(true, false);
    }
    
    public synchronized String stop(boolean writeState, boolean forceInit) throws Exception {
        LOGGER.info("Stopping service assembly: {}", getName());
        // Stop connections
        stopConnections();
        // Stop service units
        List<Element> componentFailures = new ArrayList<Element>();
        if (forceInit) {
            for (int i = 0; i < sus.length; i++) {
                try {
                    sus[i].init();
                } catch (DeploymentException e) {
                    componentFailures.add(getComponentFailure(e, "stop", sus[i].getComponentName()));
                }
            }
        }
        for (int i = 0; i < sus.length; i++) {
            if (sus[i].isStarted()) {
                try {
                    sus[i].stop();
                } catch (DeploymentException e) {
                    componentFailures.add(getComponentFailure(e, "stop", sus[i].getComponentName()));
                }
            }
        }
        if (componentFailures.size() == 0) {
            currentState = STOPPED;
            if (writeState) {
                writeRunningState();
            }
            fireEvent(ServiceAssemblyEvent.ASSEMBLY_STOPPED);
            return ManagementSupport.createSuccessMessage("stop");
        } else {
            throw ManagementSupport.failure("stop", componentFailures);
        }
    }

    /**
     * Shutdown the service assembly and puts it in SHUTDOWN state.
     * 
     * @return Result/Status of this operation.
     * @throws Exception 
     */
    public String shutDown() throws Exception {
        return shutDown(true);
    }
    
    public synchronized String shutDown(boolean writeState) throws Exception {
        LOGGER.info("Shutting down service assembly: {}", getName());
        if (currentState != STOPPED) {
            this.stop(writeState, false);
        }
        List<Element> componentFailures = new ArrayList<Element>();
        for (int i = 0; i < sus.length; i++) {
            if (sus[i].isStarted()) {
                try {
                    sus[i].stop();
                } catch (DeploymentException e) {
                    componentFailures.add(getComponentFailure(e, "shutDown", sus[i].getComponentName()));
                }
            }
        }
        for (int i = 0; i < sus.length; i++) {
            if (sus[i].isStopped()) {
                try {
                    sus[i].shutDown();
                } catch (DeploymentException e) {
                    componentFailures.add(getComponentFailure(e, "shutDown", sus[i].getComponentName()));
                }
            }
        }
        if (componentFailures.size() == 0) {
            currentState = SHUTDOWN;
            if (writeState) {
                writeRunningState();
            }
            fireEvent(ServiceAssemblyEvent.ASSEMBLY_SHUTDOWN);
            return ManagementSupport.createSuccessMessage("shutDown");
        } else {
            throw ManagementSupport.failure("shutDown", componentFailures);
        }
    }

    /**
     * @return the currentState as a String
     */
    public String getCurrentState() {
        return currentState;
    }

    boolean isShutDown() {
        return currentState.equals(SHUTDOWN);
    }

    boolean isStopped() {
        return currentState.equals(STOPPED);
    }

    boolean isStarted() {
        return currentState.equals(STARTED);
    }

    /**
     * @return the name of the ServiceAssembly
     */
    public String getName() {
        return serviceAssembly.getIdentification().getName();
    }

    /**
     * 
     * @return the description of the ServiceAssembly
     */
    public String getDescription() {
        return serviceAssembly.getIdentification().getDescription();
    }

    /**
     * @return the ServiceAssembly
     */
    public ServiceAssembly getServiceAssembly() {
        return serviceAssembly;
    }
    
    public String getDescriptor() {
        File saDir = env.getInstallDir();
        return DescriptorFactory.getDescriptorAsText(saDir);
    }

    /**
     * @return string representation of this
     */
    public String toString() {
        return "ServiceAssemblyLifeCycle[name=" + getName() + ",state=" + getCurrentState() + "]";
    }

    /**
     * write the current running state of the Component to disk
     */
    void writeRunningState() {
        try {
            if (env.getStateFile() != null) {
                String state = getCurrentState();
                Properties props = new Properties();
                props.setProperty("state", state);
                XmlPersistenceSupport.write(env.getStateFile(), props);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to write current running state for ServiceAssembly: {}", getName(), e);
        }
    }

    /**
     * get the current running state from disk
     */
    String getRunningStateFromStore() {
        try {
            if (env.getStateFile() != null && env.getStateFile().exists()) {
                Properties props = (Properties) XmlPersistenceSupport.read(env.getStateFile());
                return props.getProperty("state", SHUTDOWN);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read current running state for ServiceAssembly: {}", getName(), e);
        }
        return null;
    }
   
    /**
     * Restore this service assembly to its state at shutdown.
     * @throws Exception
     */
    public synchronized void restore() throws Exception {
        restore(true);
    }
 
    /**
     * Restore this service assembly to its state at shutdown.
     * @param forceInit
     * @throws Exception
     */
    public synchronized void restore(boolean forceInit) throws Exception {
        String state = getRunningStateFromStore();
        if (STARTED.equals(state)) {
            start(false);
        } else {
            stop(false, forceInit);
            if (SHUTDOWN.equals(state)) {
                shutDown(false);
            }
        }
    }

    public ServiceUnitLifeCycle[] getDeployedSUs() {
        return sus;
    }
    
    protected void startConnections() throws JBIException {
        if (serviceAssembly.getConnections() == null
                || serviceAssembly.getConnections().getConnections() == null) {
            return;
        }
        Connection[] connections = serviceAssembly.getConnections().getConnections();
        for (int i = 0; i < connections.length; i++) {
            if (connections[i].getConsumer().getInterfaceName() != null) {
                QName fromItf = connections[i].getConsumer().getInterfaceName();
                QName toSvc = connections[i].getProvider().getServiceName();
                String toEp = connections[i].getProvider().getEndpointName();
                registry.registerInterfaceConnection(fromItf, toSvc, toEp);
            } else {
                QName fromSvc = connections[i].getConsumer().getServiceName();
                String fromEp = connections[i].getConsumer().getEndpointName();
                QName toSvc = connections[i].getProvider().getServiceName();
                String toEp = connections[i].getProvider().getEndpointName();
                String link = getLinkType(fromSvc, fromEp);
                registry.registerEndpointConnection(fromSvc, fromEp, toSvc, toEp, link);
            }
        }
    }
    
    protected String getLinkType(QName svc, String ep) {
        for (int i = 0; i < sus.length; i++) {
            Services s = sus[i].getServices();
            if (s != null && s.getConsumes() != null) {
                Consumes[] consumes = s.getConsumes();
                for (int j = 0; j < consumes.length; j++) {
                    if (svc.equals(consumes[j].getServiceName())
                            && ep.equals(consumes[j].getEndpointName())) {
                        return consumes[j].getLinkType();
                    }
                }
            }
        }
        return null;
    }
    
    protected void stopConnections() {
        if (serviceAssembly.getConnections() == null
                || serviceAssembly.getConnections().getConnections() == null) {
            return;
        }
        Connection[] connections = serviceAssembly.getConnections().getConnections();
        for (int i = 0; i < connections.length; i++) {
            if (connections[i].getConsumer().getInterfaceName() != null) {
                QName fromItf = connections[i].getConsumer().getInterfaceName();
                registry.unregisterInterfaceConnection(fromItf);
            } else {
                QName fromSvc = connections[i].getConsumer().getServiceName();
                String fromEp = connections[i].getConsumer().getEndpointName();
                registry.unregisterEndpointConnection(fromSvc, fromEp);
            }
        }
    }

    protected Element getComponentFailure(Exception exception, String task, String component) {
        Element result = null;
        String resultMsg = exception.getMessage();
        try {
            Document doc = parse(resultMsg);
            result = getElement(doc, "component-task-result");
        } catch (Exception e) {
            LOGGER.warn("Could not parse result exception", e);
        }
        if (result == null) {
            result = ManagementSupport.createComponentFailure(
                    task, component,
                    "Unable to parse result string", exception);
        }
        return result;
    }
     
    protected Document parse(String result) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(result)));
    }
    
    protected Element getElement(Document doc, String name) {
        NodeList l = doc.getElementsByTagNameNS("http://java.sun.com/xml/ns/jbi/management-message", name);
        return (Element) l.item(0);
    }

    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "currentState", "current state of the assembly");
        helper.addAttribute(getObjectToManage(), "name", "name of the assembly");
        helper.addAttribute(getObjectToManage(), "description", "description of the assembly");
        helper.addAttribute(getObjectToManage(), "serviceUnits", "list of service units contained in this assembly");
        return helper.getAttributeInfos();
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "start", "start the assembly");
        helper.addOperation(getObjectToManage(), "stop", "stop the assembly");
        helper.addOperation(getObjectToManage(), "shutDown", "shutdown the assembly");
        helper.addOperation(getObjectToManage(), "getDescriptor", "retrieve the jbi descriptor for this assembly");
        return helper.getOperationInfos();
    }

    public Object getObjectToManage() {
        return this;
    }

    public String getType() {
        return "ServiceAssembly";
    }

    public String getSubType() {
        return null;
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

    public ObjectName[] getServiceUnits() {
        ObjectName[] names = new ObjectName[sus.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = registry.getContainer().getManagementContext().createObjectName(sus[i]);
        }
        return names;
    }
    
    public ServiceAssemblyEnvironment getEnvironment() {
        return env;
    }
    
    protected void fireEvent(int type) {
        ServiceAssemblyEvent event = new ServiceAssemblyEvent(this, type);
        ServiceAssemblyListener[] listeners = 
                (ServiceAssemblyListener[]) registry.getContainer().getListeners(ServiceAssemblyListener.class);
        for (int i = 0; i < listeners.length; i++) {
            switch (type) {
            case ServiceAssemblyEvent.ASSEMBLY_STARTED:
                listeners[i].assemblyStarted(event);
                break;
            case ServiceAssemblyEvent.ASSEMBLY_STOPPED:
                listeners[i].assemblyStopped(event);
                break;
            case ServiceAssemblyEvent.ASSEMBLY_SHUTDOWN:
                listeners[i].assemblyShutDown(event);
                break;
            default:
                break;
            }
        }
    }

}
