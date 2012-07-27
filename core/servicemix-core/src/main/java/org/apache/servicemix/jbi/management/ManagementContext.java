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
package org.apache.servicemix.jbi.management;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jbi.JBIException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.servicemix.jbi.container.EnvironmentContext;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Management Context applied to a ServiceMix container
 * 
 * @version $Revision$
 */
public class ManagementContext extends BaseSystemService implements ManagementContextMBean {

    /**
     * Default ServiceMix domain
     */
    public static final String DEFAULT_DOMAIN = "org.apache.servicemix";

    public static final String DEFAULT_CONNECTOR_PATH = "/jmxrmi";

    public static final int DEFAULT_CONNECTOR_PORT = 1099;

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ManagementContext.class);

    protected Map<String, ObjectName> systemServices = new ConcurrentHashMap<String, ObjectName>();

    private Map<ObjectName, Object> beanMap = new ConcurrentHashMap<ObjectName, Object>();

    private MBeanServerContext mbeanServerContext = new MBeanServerContext();

    private ExecutorService executors;

    /**
     * Default Constructor
     */
    public ManagementContext() {
        mbeanServerContext.setJmxDomainName(DEFAULT_DOMAIN);
    }

    /**
     * Get the Description of the item
     * 
     * @return the description
     */
    public String getDescription() {
        return "JMX Management";
    }

    /**
     * Get the MBeanServer
     * 
     * @return the MBeanServer
     */
    public MBeanServer getMBeanServer() {
        return mbeanServerContext.getMBeanServer();
    }

    protected void setMBeanServer(MBeanServer server) {
        mbeanServerContext.setMBeanServer(server);
    }

    /**
     * @return the domain
     */
    public String getJmxDomainName() {
        return mbeanServerContext.getJmxDomainName();
    }

    /**
     * @return Returns the useMBeanServer.
     */
    public boolean isUseMBeanServer() {
        return mbeanServerContext.isUseMBeanServer();
    }

    /**
     * @param useMBeanServer
     *            The useMBeanServer to set.
     */
    public void setUseMBeanServer(boolean useMBeanServer) {
        mbeanServerContext.setUseMBeanServer(useMBeanServer);
    }

    /**
     * @return Returns the createMBeanServer flag.
     */
    public boolean isCreateMBeanServer() {
        return mbeanServerContext.isCreateMBeanServer();
    }

    /**
     * @param enableJMX
     *            Set createMBeanServer.
     */
    public void setCreateMBeanServer(boolean enableJMX) {
        mbeanServerContext.setCreateMBeanServer(enableJMX);
    }

    public void setNamingPort(int portNum) {
        mbeanServerContext.setConnectorPort(portNum);
    }

    public int getNamingPort() {
        return mbeanServerContext.getConnectorPort();
    }

    public boolean isCreateJmxConnector() {
        return mbeanServerContext.isCreateConnector();
    }

    public void setCreateJmxConnector(boolean createJmxConnector) {
        mbeanServerContext.setCreateConnector(createJmxConnector);
    }

    /**
     * Initialize the ManagementContext
     * 
     * @param container
     * @param server
     * @throws JBIException
     * 
     */
    public void init(JBIContainer container, MBeanServer server) throws JBIException {
        if (container.isEmbedded() && server == null) {
            mbeanServerContext.setUseMBeanServer(false);
            mbeanServerContext.setCreateMBeanServer(false);
            mbeanServerContext.setCreateConnector(false);
        }
        mbeanServerContext.setMBeanServer(server);
        try {
            mbeanServerContext.start();
        } catch (IOException e) {
            LOGGER.error("Failed to start mbeanServerContext", e);
        }
        this.executors = Executors.newCachedThreadPool();
        super.init(container);
    }

    protected Class<ManagementContextMBean> getServiceMBean() {
        return ManagementContextMBean.class;
    }

    /**
     * Start the item.
     * 
     * @exception JBIException
     *                if the item fails to start.
     */
    public void start() throws JBIException {
        super.start();
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception JBIException
     *                if the item fails to stop.
     */
    public void stop() throws JBIException {
        super.stop();
    }

    /**
     * Shut down the item. The releases resources, preparatory to
     * uninstallation.
     * 
     * @exception JBIException
     *                if the item fails to shut down.
     */
    public void shutDown() throws JBIException {
        super.shutDown();
        // Unregister all mbeans
        ObjectName[] beans = beanMap.keySet().toArray(new ObjectName[beanMap.size()]);
        for (int i = 0; i < beans.length; i++) {
            try {
                unregisterMBean(beans[i]);
            } catch (Exception e) {
                LOGGER.debug("Could not unregister mbean", e);
            }
        }
        try {
            mbeanServerContext.stop();
        } catch (IOException e) {
            LOGGER.debug("Failed to shutdown mbeanServerContext cleanly", e);
        }
        executors.shutdown();
    }

    /**
     * Get a list of all binding components currently installed.
     * 
     * @return array of JMX object names of all installed BCs.
     */
    public ObjectName[] getBindingComponents() {
        return container.getRegistry().getBindingComponents();
    }

    /**
     * Lookup a JBI Installable Component by its unique name.
     * 
     * @param componentName -
     *            is the name of the BC or SE.
     * @return the JMX object name of the component's LifeCycle MBean or null.
     */
    public ObjectName getComponentByName(String componentName) {
        ComponentMBeanImpl component = container.getRegistry().getComponent(componentName);
        return component != null ? component.getMBeanName() : null;
    }

    /**
     * Get a list of all engines currently installed.
     * 
     * @return array of JMX object names of all installed SEs.
     */
    public ObjectName[] getEngineComponents() {
        return container.getRegistry().getEngineComponents();
    }

    /**
     * @return an array of ObjectNames for all Pojo components
     */
    public ObjectName[] getPojoComponents() {
        return container.getRegistry().getPojoComponents();
    }

    /**
     * Return current version and other info about this JBI Framework.
     * 
     * @return info String
     */
    public String getSystemInfo() {
        return "ServiceMix JBI Container: version: " + EnvironmentContext.getVersion();
    }

    /**
     * Lookup a system service by name.
     * 
     * @param serviceName -
     *            is the name of the system service
     * @return the JMX object name of the service or null
     */
    public ObjectName getSystemService(String serviceName) {
        return systemServices.get(serviceName);
    }

    /**
     * Looks up all JBI Framework System Services currently installed.
     * 
     * @return array of JMX object names of system services
     */
    public ObjectName[] getSystemServices() {
        ObjectName[] result = null;
        Collection<ObjectName> col = systemServices.values();
        result = new ObjectName[col.size()];
        col.toArray(result);
        return result;
    }

    /**
     * Check if a given JBI Installable Component is a Binding Component.
     * 
     * @param componentName -
     *            the unique name of the component
     * @return true if the component is a binding
     */
    public boolean isBinding(String componentName) {
        ComponentMBeanImpl component = container.getRegistry().getComponent(componentName);
        return component != null ? component.isBinding() : false;
    }

    /**
     * Check if a given JBI Component is a service engine.
     * 
     * @param componentName -
     *            the unique name of the component
     * @return true if the component is a service engine
     */
    public boolean isEngine(String componentName) {
        ComponentMBeanImpl component = container.getRegistry().getComponent(componentName);
        return component != null ? component.isEngine() : false;
    }

    /**
     * Start a Component
     * 
     * @param componentName
     * @return the status
     * @throws JBIException
     */
    public String startComponent(String componentName) throws JBIException {
        String result = "NOT FOUND: " + componentName;
        ObjectName objName = getComponentByName(componentName);
        if (objName != null) {
            ComponentMBeanImpl mbean = (ComponentMBeanImpl) beanMap.get(objName);
            if (mbean != null) {
                mbean.start();
                result = mbean.getCurrentState();
            }
        }
        return result;
    }

    /**
     * Stop a Component
     * 
     * @param componentName
     * @return the status
     * @throws JBIException
     */
    public String stopComponent(String componentName) throws JBIException {
        String result = "NOT FOUND: " + componentName;
        ObjectName objName = getComponentByName(componentName);
        if (objName != null) {
            ComponentMBeanImpl mbean = (ComponentMBeanImpl) beanMap.get(objName);
            if (mbean != null) {
                mbean.stop();
                result = mbean.getCurrentState();
            }
        }
        return result;
    }

    /**
     * Shutdown a Component
     * 
     * @param componentName
     * @return the status
     * @throws JBIException
     */
    public String shutDownComponent(String componentName) throws JBIException {
        String result = "NOT FOUND: " + componentName;
        ObjectName objName = getComponentByName(componentName);
        if (objName != null) {
            ComponentMBeanImpl mbean = (ComponentMBeanImpl) beanMap.get(objName);
            if (mbean != null) {
                mbean.shutDown();
                result = mbean.getCurrentState();
            }
        }
        return result;
    }

    /**
     * Formulate and return the MBean ObjectName of a custom control MBean for a
     * JBI component.
     * 
     * @param type
     * @param name
     * @return the JMX ObjectName of the MBean, or <code>null</code> if
     *         <code>customName</code> is invalid.
     */
    public ObjectName createCustomComponentMBeanName(String type, String name) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("ContainerName", container.getName());
        result.put("Type", "Component");
        result.put("Name", sanitizeString(name));
        result.put("SubType", sanitizeString(type));
        return createObjectName(result);
    }

    /**
     * Create an ObjectName
     * 
     * @param provider
     * @return the ObjectName
     */
    public ObjectName createObjectName(MBeanInfoProvider provider) {
        Map<String, String> props = createObjectNameProps(provider);
        return createObjectName(props);
    }

    /**
     * Create an ObjectName
     * 
     * @param name
     * 
     * @return the ObjectName
     */
    public ObjectName createObjectName(String name) {
        ObjectName result = null;
        try {
            result = new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            // shouldn't happen
            String error = "Could not create ObjectName for " + name;
            LOGGER.error(error, e);
            throw new RuntimeException(error);
        }
        return result;
    }

    /**
     * Create an ObjectName
     * 
     * @param domain
     * 
     * @return the ObjectName
     */
    public ObjectName createObjectName(String domain, Map<String, String> props) {
        StringBuffer sb = new StringBuffer();
        sb.append(domain).append(':');
        int i = 0;
        for (Iterator it = props.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            if (i++ > 0) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        ObjectName result = null;
        try {
            result = new ObjectName(sb.toString());
        } catch (MalformedObjectNameException e) {
            // shouldn't happen
            String error = "Could not create ObjectName for " + props;
            LOGGER.error(error, e);
            throw new RuntimeException(error);
        }
        return result;
    }

    /**
     * Create an ObjectName
     * 
     * @param props
     * @return the ObjectName
     */
    public ObjectName createObjectName(Map<String, String> props) {
        return createObjectName(getJmxDomainName(), props);
    }

    /**
     * Create a String used to create an ObjectName
     * 
     * @param provider
     * @return the ObjectName
     */
    public Map<String, String> createObjectNameProps(MBeanInfoProvider provider) {
        return createObjectNameProps(provider, false);
    }

    /**
     * Create a String used to create an ObjectName
     * 
     * @param provider
     * @return the ObjectName
     */
    public Map<String, String> createObjectNameProps(MBeanInfoProvider provider, boolean subTypeBeforeName) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        result.put("ContainerName", container.getName());
        result.put("Type", sanitizeString(provider.getType()));
        if (subTypeBeforeName && provider.getSubType() != null) {
            result.put("SubType", sanitizeString(provider.getSubType()));
        }
        result.put("Name", sanitizeString(provider.getName()));
        if (!subTypeBeforeName && provider.getSubType() != null) {
            result.put("SubType", sanitizeString(provider.getSubType()));
        }
        return result;
    }

    /**
     * The ':' and '/' characters are reserved in ObjectNames
     * 
     * @param in
     * @return sanitized String
     */
    private static String sanitizeString(String in) {
        String result = null;
        if (in != null) {
            result = in.replace(':', '_');
            result = result.replace('/', '_');
            result = result.replace('\\', '_');
            result = result.replace('?', '_');
            result = result.replace('=', '_');
            result = result.replace(',', '_');
        }
        return result;
    }

    /**
     * Register an MBean
     * 
     * @param resource
     * @param name
     * @param interfaceMBean
     * @throws JMException
     */
    public void registerMBean(ObjectName name, MBeanInfoProvider resource, Class interfaceMBean) throws JMException {
        registerMBean(name, resource, interfaceMBean, resource.getDescription());
    }

    /**
     * Register an MBean
     * 
     * @param resource
     * @param name
     * @param interfaceMBean
     * @param description
     * @throws JMException
     */
    public ObjectName registerMBean(ObjectName name, Object resource, Class interfaceMBean, String description) throws JMException {
        if (mbeanServerContext.getMBeanServer() != null) {
            Object mbean = MBeanBuilder.buildStandardMBean(resource, interfaceMBean, description, executors);
            return registerMBean(name, resource, mbean);
        } else {
            return name;
        }
    }

    /*
     * Register an MBean for a given resource, specifying the object name and the actual MBean implementation
     *
     * @param name the MBean's ObjectName
     * @param resource the original bean for which the MBean is a wrapper
     * @param mbean the MBean implementation
     */
    protected ObjectName registerMBean(ObjectName name, Object resource, Object mbean)
        throws InstanceNotFoundException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {

        if (mbeanServerContext.getMBeanServer().isRegistered(name)) {
            mbeanServerContext.getMBeanServer().unregisterMBean(name);
        }
        ObjectName objectName = mbeanServerContext.getMBeanServer().registerMBean(mbean, name).getObjectName();
        beanMap.put(objectName, resource);
        return objectName;
    }

    /**
     * Retrive an System ObjectName
     * 
     * @param domainName
     * @param containerName
     * @param interfaceType
     * @return the ObjectName
     */
    public static ObjectName getSystemObjectName(String domainName, String containerName, Class interfaceType) {
        String tmp = domainName + ":ContainerName=" + containerName + ",Type=SystemService,Name=" + getSystemServiceName(interfaceType);
        ObjectName result = null;
        try {
            result = new ObjectName(tmp);
        } catch (MalformedObjectNameException e) {
            LOGGER.error("Failed to build ObjectName:", e);
        } catch (NullPointerException e) {
            LOGGER.error("Failed to build ObjectName:", e);
        }
        return result;
    }

    /**
     * Retrieve an ObjectName instance that can be used for querying system service mbean instances
     *
     * @param domainName
     * @param containerName
     * @param interfaceType
     * @return
     */
    public static ObjectName getSystemObjectNameQuery(String domainName, String containerName, Class interfaceType) {
        ObjectName result = null;
        try {
            result = new ObjectName(String.format("%s,*", getSystemObjectName(domainName, containerName, interfaceType)));
        } catch (MalformedObjectNameException e) {
            LOGGER.error("Failed to build object name query: {}", e.getMessage(), e);
        }
        return result;
    }

    public static String getSystemServiceName(Class interfaceType) {
        String name = interfaceType.getName();
        name = name.substring(name.lastIndexOf('.') + 1);
        if (name.endsWith("MBean")) {
            name = name.substring(0, name.length() - 5);
        }
        return name;
    }

    public static ObjectName getContainerObjectName(String domainName, String containerName) {
        String tmp = domainName + ":ContainerName=" + containerName + ",Type=JBIContainer";
        ObjectName result = null;
        try {
            result = new ObjectName(tmp);
        } catch (MalformedObjectNameException e) {
            LOGGER.debug("Unable to build ObjectName", e);
        } catch (NullPointerException e) {
            LOGGER.debug("Unable to build ObjectName", e);
        }
        return result;
    }

    /**
     * Register a System service
     * 
     * @param service
     * @param interfaceType
     * @throws JBIException
     */
    public void registerSystemService(BaseSystemService service, Class interfaceType) throws JBIException {
        try {

            String name = service.getName();
            if (systemServices.containsKey(name)) {
                throw new JBIException("A system service for the name " + name + " is already registered");
            }
            ObjectName objName = createObjectName(service);
            LOGGER.debug("Registering system service: {}", objName);
            ObjectName registeredName = registerMBean(objName, service, interfaceType, service.getDescription());
            systemServices.put(name, registeredName);
        } catch (MalformedObjectNameException e) {
            throw new JBIException(e);
        } catch (JMException e) {
            throw new JBIException(e);
        }
    }

    /**
     * Unregister a System service
     * 
     * @param service
     * @throws JBIException
     */
    public void unregisterSystemService(BaseSystemService service) throws JBIException {
        String name = service.getName();
        if (!systemServices.containsKey(name)) {
            throw new JBIException("A system service for the name " + name + " is not registered");
        }
        ObjectName objName = systemServices.remove(name);
        LOGGER.debug("Unregistering system service: {}", objName);
        unregisterMBean(objName);
    }

    /**
     * Unregister an MBean
     * 
     * @param name
     * @throws JBIException
     */
    public void unregisterMBean(ObjectName name) throws JBIException {
        try {
            mbeanServerContext.unregisterMBean(name);
            beanMap.remove(name);
        } catch (JMException e) {
            LOGGER.error("Failed to unregister mbean: {}", name, e);
            throw new JBIException(e);
        }
    }

    /**
     * Unregister an MBean
     * 
     * @param bean
     * @throws JBIException
     */
    public void unregisterMBean(Object bean) throws JBIException {
        for (Iterator i = beanMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (entry.getValue() == bean) {
                ObjectName name = (ObjectName) entry.getKey();
                unregisterMBean(name);
                break;
            }
        }
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
        AttributeInfoHelper helper = new AttributeInfoHelper();
        helper.addAttribute(getObjectToManage(), "bindingComponents", "Get list of all binding components");
        helper.addAttribute(getObjectToManage(), "engineComponents", "Get list of all engine components");
        helper.addAttribute(getObjectToManage(), "pojoComponents", "Get list of all pojo components");
        helper.addAttribute(getObjectToManage(), "systemInfo", "Return current version");
        helper.addAttribute(getObjectToManage(), "systemServices", "Get list of system services");
        return AttributeInfoHelper.join(super.getAttributeInfos(), helper.getAttributeInfos());
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph = helper.addOperation(getObjectToManage(), "getComponentByName", 1, "look up Component by name");
        ph.setDescription(0, "name", "Component name");
        ph = helper.addOperation(getObjectToManage(), "getSystemService", 1, "look up System service by name");
        ph.setDescription(0, "name", "System name");
        ph = helper.addOperation(getObjectToManage(), "isBinding", 1, "Is Component a binding Component?");
        ph.setDescription(0, "name", "Component name");
        ph = helper.addOperation(getObjectToManage(), "isEngine", 1, "Is Component a service engine?");
        ph.setDescription(0, "name", "Component name");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

}
