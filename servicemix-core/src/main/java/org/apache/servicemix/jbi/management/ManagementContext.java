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
package org.apache.servicemix.jbi.management;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.EnvironmentContext;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;

import javax.jbi.JBIException;
import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.registry.LocateRegistry;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Management Context applied to a ServiceMix container
 * 
 * @version $Revision$
 */
public class ManagementContext extends BaseLifeCycle implements ManagementContextMBean {
    /**
     * Default servicemix domain
     */
    public static final String DEFAULT_DOMAIN = "org.apache.servicemix";
    
    private final static Log log = LogFactory.getLog(ManagementContext.class);
    private JBIContainer container;
    private MBeanServer beanServer;
    private int namingPort = 1099;
    private String jndiPath = "/jmxconnector";
    private JMXConnectorServer connectorServer;
    private String jmxDomainName = DEFAULT_DOMAIN;
    private Map beanMap = new ConcurrentHashMap();
    protected Map systemServices = new ConcurrentHashMap();
    private boolean useMBeanServer = true;
    private boolean createMBeanServer = false;
    private boolean locallyCreateMBeanServer = false;

    /**
     * Default Constructor
     */
    public ManagementContext() {
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
     * @return Returns the jmxDomainName.
     */
    public String getJmxDomainName() {
        return jmxDomainName;
    }

    /**
     * @param jmxDomainName The jmxDomainName to set.
     */
    public void setJmxDomainName(String jmxDomainName) {
        this.jmxDomainName = jmxDomainName;
    }

    /**
     * Get the MBeanServer
     * 
     * @return the MBeanServer
     */
    public MBeanServer getMBeanServer() {
        return beanServer;
    }

    /**
     * @return Returns the namingPort.
     */
    public int getNamingPort() {
        return namingPort;
    }

    /**
     * @param namingPort The namingPort to set.
     */
    public void setNamingPort(int namingPort) {
        this.namingPort = namingPort;
    }
    
    /**
     * @return Returns the jndiPath.
     */
    public String getJndiPath() {
        return jndiPath;
    }

    /**
     * @param jndiPath The jndiPath to set.
     */
    public void setJndiPath(String jndiPath) {
        this.jndiPath = jndiPath;
    }

    /**
     * @return Returns the useMBeanServer.
     */
    public boolean isUseMBeanServer() {
        return useMBeanServer;
    }

    /**
     * @param useMBeanServer The useMBeanServer to set.
     */
    public void setUseMBeanServer(boolean useMBeanServer) {
        this.useMBeanServer = useMBeanServer;
    }
    
    /**
     * @return Returns the createMBeanServer flag.
     */
    public boolean isCreateMBeanServer() {
        return createMBeanServer;
    }

    /**
     * @param enableJMX Set createMBeanServer.
     */
    public void setCreateMBeanServer(boolean enableJMX) {
        this.createMBeanServer = enableJMX;
    }

    /**
     * Initialize the ManagementContext
     * 
     * @param container
     * @param server
     * @throws JBIException 
    
     */
    public void init(JBIContainer container, MBeanServer server) throws JBIException  {
        this.container = container;
        jndiPath = "/" + container.getName() + "JMX";
        this.beanServer = server != null ? server : findMBeanServer();
        // register self as a System service
        registerSystemService(this, ManagementContextMBean.class);
    }

    /**
     * Start the item.
     * 
     * @exception JBIException if the item fails to start.
     */
    public void start() throws JBIException {
        super.start();
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception JBIException if the item fails to stop.
     */
    public void stop() throws JBIException {
        super.stop();
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception JBIException if the item fails to shut down.
     */
    public void shutDown() throws JBIException {
        super.shutDown();
        // Unregister all mbeans
        Object[] beans = beanMap.keySet().toArray();
        for (int i = 0; i < beans.length; i++) {
            try {
                unregisterMBean(beans[i]);
            } catch (Exception e) {
                log.debug("Could not unregister mbean", e);
            }
        }
        if (connectorServer != null) {
            try {
                connectorServer.stop();
            }
            catch (IOException e) {
                log.error("Problem stopping the JMX ConnectorServer", e);
            }
        }
        if (locallyCreateMBeanServer && beanServer != null) {
            try {
                beanServer.invoke(ObjectName.getInstance("naming:type=rmiregistry"), "stop", null, null);
            } catch (Exception e) {
                log.debug("Could not stop naming service", e);
            }
            // check to see if the factory knows about this server
            List list = MBeanServerFactory.findMBeanServer(null);
            if (list != null && !list.isEmpty() && list.contains(beanServer)) {
                MBeanServerFactory.releaseMBeanServer(beanServer);
            }
        }
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
     * @param name - is the name of the BC or SE.
     * @return the JMX object name of the component's LifeCycle MBean or null.
     */
    public ObjectName getComponentByName(String name) {
        return container.getRegistry().getComponentObjectName(name);
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
     * @param serviceName - is the name of the system service
     * @return the JMX object name of the service or null
     */
    public ObjectName getSystemService(String serviceName) {
        return (ObjectName) systemServices.get(serviceName);
    }

    /**
     * Looks up all JBI Framework System Services currently installed.
     * 
     * @return array of JMX object names of system services
     */
    public ObjectName[] getSystemServices() {
        ObjectName[] result = null;
        Collection col = systemServices.values();
        result = new ObjectName[col.size()];
        col.toArray(result);
        return result;
    }

    /**
     * Check if a given JBI Installable Component is a Binding Component.
     * 
     * @param componentName - the unique name of the component
     * @return true if the component is a binding
     */
    public boolean isBinding(String componentName) {
        return container.getRegistry().isBinding(componentName);
    }

    /**
     * Check if a given JBI Component is a service engine.
     * 
     * @param componentName - the unique name of the component
     * @return true if the component is a service engine
     */
    public boolean isEngine(String componentName) {
        return container.getRegistry().isEngine(componentName);
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
     * Formulate and return the MBean ObjectName of a custom control MBean for a JBI component.
     * 
     * @param type
     * @param name
     * @return the JMX ObjectName of the MBean, or <code>null</code> if <code>customName</code> is invalid.
     */
    public ObjectName createCustomComponentMBeanName(String type, String name) {
        ObjectName result = null;
        String tmp = jmxDomainName + ":" + "type=" + sanitizeString(type) + ",name=" + sanitizeString(name);
        try {
            result = new ObjectName(tmp);
        }
        catch (MalformedObjectNameException e) {
            log.error("Couldn't create ObjectName from: " + type + " , " + name);
        }
        return result;
    }

    /**
     * Create an ObjectName
     * 
     * @param provider
     * @return the ObjectName
     */
    public ObjectName createObjectName(MBeanInfoProvider provider) {
        ObjectName result = null;
        try {
            String tmp = jmxDomainName + ":" + "type="
                    + (provider.getClass().getName() + ",name=" + getRelativeName(provider));
            result = new ObjectName(tmp);
        }
        catch (MalformedObjectNameException e) {
            // shouldn't happen
            String error = "Could not create ObjectName for " + provider.getClass() + ", " + provider.getName();
            log.error(error, e);
            throw new RuntimeException(error);
        }
        return result;
    }

    /**
     * Get a qualified name
     * 
     * @param provider
     * @return the name
     */
    public String getRelativeName(MBeanInfoProvider provider) {
        return sanitizeString(container.getName() + "." + provider.getName());
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
    public void registerMBean(ObjectName name, Object resource, Class interfaceMBean, String description)
            throws JMException {
        if (beanServer != null) {
            Object mbean = MBeanBuilder.buildStandardMBean(resource, interfaceMBean, description);
            if (beanServer.isRegistered(name)) {
                beanServer.unregisterMBean(name);
            }
            beanServer.registerMBean(mbean, name);
            beanMap.put(name, resource);
        }
    }


    /**
     * Retrive an System ObjectName
     * 
     * @param domainName
     * @param containerName
     * @param theClass
     * @return the ObjectName
     */
    public static ObjectName getSystemObjectName(String domainName, String containerName, Class theClass) {
        String tmp = domainName + ":" + "type=" + theClass.getName() + ",name="
                + getRelativeName(containerName, theClass);
        ObjectName result = null;
        try {
            result = new ObjectName(tmp);
        }
        catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }
    
    public static ObjectName getContainerObjectName(String domainName, String containerName) {
        String tmp = domainName + ":" + "type=" + JBIContainer.class.getName() + ",name=" + containerName;
        ObjectName result = null;
        try {
            result = new ObjectName(tmp);
        }
        catch (MalformedObjectNameException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    private static String getRelativeName(String containerName, Class theClass) {
        String name = theClass.getName();
        int index = name.lastIndexOf(".");
        if (index >= 0 && (index + 1) < name.length()) {
            name = name.substring(index + 1);
        }
        return containerName + "." + name;
    }

    /**
     * Register a System service
     * 
     * @param service
     * @param implementationType
     * @throws JBIException
     */
    public void registerSystemService(BaseLifeCycle service, Class implementationType) throws JBIException {
        registerSystemService(service, implementationType, getRelativeName(service));
    }

    public void registerSystemService(BaseLifeCycle service, Class type, Class implementationType, String name) throws JBIException {
        String tmp = jmxDomainName + ":" + "type=" + type.getName() + ",name=" + name;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Registering system service: class=" + implementationType.getName() + ", name=" + tmp);
            }
            ObjectName objName = new ObjectName(tmp);
            registerMBean(objName, service, implementationType, service.getDescription());
            systemServices.put(name, objName);
        }
        catch (MalformedObjectNameException e) {
            throw new JBIException(e);
        }
        catch (JMException e) {
            throw new JBIException(e);
        }
    }

    /**
     * Register a System service
     * 
     * @param service
     * @param implementationType
     * @param name
     * @throws JBIException
     */
    public void registerSystemService(BaseLifeCycle service, Class implementationType, String name) throws JBIException {
        registerSystemService(service, service.getClass(), implementationType, name);
    }

    /**
     * Unregister an MBean
     * 
     * @param name
     * @throws JMException
     */
    public void unregisterMBean(ObjectName name) throws JBIException {
        try {
            if (name != null && beanServer != null && beanServer.isRegistered(name)) {
                beanServer.unregisterMBean(name);
                beanMap.remove(name);
            }
        } catch (JMException e) {
            throw new JBIException("Could not unregister mbean", e);
        }
    }

    /**
     * Unregister an MBean
     * 
     * @param bean
     * @throws JMException
     */
    public void unregisterMBean(Object bean) throws JBIException {
        for (Iterator i = beanMap.entrySet().iterator();i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (entry.getValue() == bean) {
            	ObjectName name = (ObjectName) entry.getKey();
            	unregisterMBean(name);
                break;
            }
        }
    }

   

    protected synchronized MBeanServer findMBeanServer() {
        MBeanServer result = null;
        // create the mbean server and start an rmi connector
        if (useMBeanServer) {
            try {
                // lets piggy back on another MBeanServer - we could be in an appserver!
                List list = MBeanServerFactory.findMBeanServer(null);
                if (list != null && list.size() > 0) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found " + list.size() + " mbean servers. Getting the first one");
                    } 
                    result = (MBeanServer) list.get(0);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No mbean server found");
                    } 
                }
                if (result == null && createMBeanServer) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating mbean server");
                    } 
                    result = MBeanServerFactory.createMBeanServer(jmxDomainName);
                    locallyCreateMBeanServer = true;
                    // Register and start the rmiregistry MBean, needed by JSR 160 RMIConnectorServer
                    ObjectName namingName = ObjectName.getInstance("naming:type=rmiregistry");
                    if (!result.isRegistered(namingName)) {
                        try {
                            // Do not use the createMBean as the mx4j jar may not be in the 
                            // same class loader than the server
                            Class cl = Class.forName("mx4j.tools.naming.NamingService");
                            result.registerMBean(cl.newInstance(), namingName);
                            //result.createMBean("mx4j.tools.naming.NamingService", namingName, null);
                            // set the naming port
                            Attribute attr = new Attribute("Port", new Integer(namingPort));
                            result.setAttribute(namingName, attr);
                            result.invoke(namingName, "start", null, null);
                        }
                        catch (Exception e) {
                            // could already be in use
                            log.debug("Could not start naming service", e);
                            // Now, make sure a registry is loaded
                            try {
                                LocateRegistry.createRegistry(namingPort);
                            }
                            catch (Throwable t) {
                                // proably exists already
                                log.debug("Failed to create local registry", t);
                            }
                        }
                        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + namingPort
                                + jndiPath);
                        // Create and start the RMIConnectorServer
                        connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, result);
                        connectorServer.start();
                    }
                    /*
                    // Now, make sure a registry is loaded
                    try {
                        LocateRegistry.createRegistry(namingPort);
                    }
                    catch (Throwable t) {
                        // proably exists already
                        log.debug("Failed to create local registry", t);
                    }
                    // Create and start the RMIConnectorServer
                    JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + namingPort
                            + jndiPath);
                    connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, result);
                    connectorServer.start();
                    */
                }
            }
            catch (NoClassDefFoundError e) {
                log.error("Could not load MBeanServer", e);
            }
            /*
            catch (JMException e) {
                log.error("Could not start the remote: JMX ConnectorServer", e);
            }
            */
            catch (MalformedURLException e) {
                log.error("Bad URL:", e);
            }
            catch (IOException e) {
                log.error("Could not start the remote: JMX ConnectorServer", e);
            }
            catch (Throwable e) {
                // probably don't have access to system properties
                log.error("Failed to initialize MBeanServer", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Not using jmx: useMBeanServer is false");
            }
        }
        return result;
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