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
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.servicemix.jbi.jmx.ConnectorServerFactoryBean;
import org.apache.servicemix.jbi.jmx.RmiRegistryFactoryBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around ActiveMQ ManagementContext. Re-use to build/find mbean server
 * 
 * @version $Revision$
 */
class MBeanServerContext {

    /**
     * Default ActiveMQ domain
     */
    public static final String DEFAULT_DOMAIN = "org.apache.activemq";

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ManagementContext.class);

    private MBeanServer beanServer;

    private String jmxDomainName = DEFAULT_DOMAIN;

    private boolean useMBeanServer = true;

    private boolean createMBeanServer = true;

    private boolean locallyCreateMBeanServer;

    private boolean createConnector = true;

    private boolean findTigerMbeanServer;

    private int connectorPort = 1099;

    private String connectorPath = "/jmxrmi";

    private AtomicBoolean started = new AtomicBoolean(false);

    private ConnectorServerFactoryBean connectorServerFactoryBean;

    private RmiRegistryFactoryBean rmiRegistryFactoryBean;

    public MBeanServerContext() {
        this(null);
    }

    public MBeanServerContext(MBeanServer server) {
        this.beanServer = server;
    }

    public void start() throws IOException {
        // lets force the MBeanServer to be created if needed
        if (started.compareAndSet(false, true)) {
            getMBeanServer();
            if (createConnector) {
                try {
                    rmiRegistryFactoryBean = new RmiRegistryFactoryBean();
                    rmiRegistryFactoryBean.setPort(connectorPort);
                    rmiRegistryFactoryBean.afterPropertiesSet();
                } catch (Exception e) {
                    LOGGER.warn("Failed to start rmi registry: {}", e.getMessage());
                    LOGGER.debug("Failed to start rmi registry", e);
                }
                try {
                    connectorServerFactoryBean = new ConnectorServerFactoryBean();
                    // connectorServerFactoryBean.setDaemon(true);
                    connectorServerFactoryBean.setObjectName("connector:name=rmi");
                    // connectorServerFactoryBean.setThreaded(true);
                    connectorServerFactoryBean.setServer(getMBeanServer());
                    String serviceUrl = "service:jmx:rmi:///jndi/rmi://localhost:" + connectorPort + connectorPath;
                    connectorServerFactoryBean.setServiceUrl(serviceUrl);
                    connectorServerFactoryBean.afterPropertiesSet();
                } catch (Exception e) {
                    LOGGER.warn("Failed to start jmx connector: {}", e.getMessage());
                    LOGGER.debug("Failed to create jmx connector", e);
                }
            }
        }
    }

    public void stop() throws IOException {
        if (started.compareAndSet(true, false)) {
            if (connectorServerFactoryBean != null) {
                try {
                    connectorServerFactoryBean.destroy();
                } catch (Exception e) {
                    LOGGER.warn("Failed to stop jmx connector: {}", e.getMessage());
                    LOGGER.debug("Failed to stop jmx connector", e);
                } finally {
                    connectorServerFactoryBean = null;
                }
            }
            if (rmiRegistryFactoryBean != null) {
                try {
                    rmiRegistryFactoryBean.destroy();
                } catch (RemoteException e) {
                    LOGGER.warn("Failed to stop rmi registry: {}", e.getMessage());
                    LOGGER.debug("Failed to stop rmi registry", e);
                } finally {
                    rmiRegistryFactoryBean = null;
                }
            }
            if (locallyCreateMBeanServer && beanServer != null) {
                // check to see if the factory knows about this server
                List list = MBeanServerFactory.findMBeanServer(null);
                if (list != null && !list.isEmpty() && list.contains(beanServer)) {
                    MBeanServerFactory.releaseMBeanServer(beanServer);
                }
            }
        }
    }

    /**
     * @return Returns the jmxDomainName.
     */
    public String getJmxDomainName() {
        return jmxDomainName;
    }

    /**
     * @param jmxDomainName
     *            The jmxDomainName to set.
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
        if (this.beanServer == null) {
            this.beanServer = findMBeanServer();
        }
        return beanServer;
    }

    /**
     * Set the MBeanServer
     * 
     * @param mbs
     */
    public void setMBeanServer(MBeanServer mbs) {
        this.beanServer = mbs;
    }

    /**
     * @return Returns the useMBeanServer.
     */
    public boolean isUseMBeanServer() {
        return useMBeanServer;
    }

    /**
     * @param useMBeanServer
     *            The useMBeanServer to set.
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
     * @param enableJMX
     *            Set createMBeanServer.
     */
    public void setCreateMBeanServer(boolean enableJMX) {
        this.createMBeanServer = enableJMX;
    }

    public boolean isFindTigerMbeanServer() {
        return findTigerMbeanServer;
    }

    /**
     * Enables/disables the searching for the Java 5 platform MBeanServer
     */
    public void setFindTigerMbeanServer(boolean findTigerMbeanServer) {
        this.findTigerMbeanServer = findTigerMbeanServer;
    }

    /**
     * Formulate and return the MBean ObjectName of a custom control MBean
     * 
     * @param type
     * @param name
     * @return the JMX ObjectName of the MBean, or <code>null</code> if
     *         <code>customName</code> is invalid.
     */
    public ObjectName createCustomComponentMBeanName(String type, String name) {
        ObjectName result = null;
        String tmp = jmxDomainName + ":" + "type=" + sanitizeString(type) + ",name=" + sanitizeString(name);
        try {
            result = new ObjectName(tmp);
        } catch (MalformedObjectNameException e) {
            LOGGER.error("Couldn't create ObjectName from: {} , {}", type, name);
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
        }
        return result;
    }

    /**
     * Retrive an System ObjectName
     * 
     * @param domainName
     * @param containerName
     * @param theClass
     * @return the ObjectName
     * @throws MalformedObjectNameException
     */
    public static ObjectName getSystemObjectName(String domainName, String containerName, 
                                                 Class theClass) throws MalformedObjectNameException, NullPointerException {
        String tmp = domainName + ":" + "type=" + theClass.getName() + ",name=" + getRelativeName(containerName, theClass);
        return new ObjectName(tmp);
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
     * Unregister an MBean
     * 
     * @param name
     * @throws JMException
     */
    public void unregisterMBean(ObjectName name) throws JMException {
        if (beanServer != null && beanServer.isRegistered(name)) {
            beanServer.unregisterMBean(name);
        }
    }

    protected synchronized MBeanServer findMBeanServer() {
        MBeanServer result = null;
        // create the mbean server
        try {
            if (useMBeanServer) {
                if (findTigerMbeanServer) {
                    result = findTigerMBeanServer();
                }
                if (result == null) {
                    // lets piggy back on another MBeanServer -
                    // we could be in an appserver!
                    List list = MBeanServerFactory.findMBeanServer(null);
                    if (list != null && list.size() > 0) {
                        result = (MBeanServer) list.get(0);
                    }
                }
            }
            if (result == null && createMBeanServer) {
                result = createMBeanServer();
            }
        } catch (NoClassDefFoundError e) {
            LOGGER.error("Could not load MBeanServer", e);
        } catch (Throwable e) {
            // probably don't have access to system properties
            LOGGER.error("Failed to initialize MBeanServer", e);
        }
        return result;
    }

    public static MBeanServer findTigerMBeanServer() {
        String name = "java.lang.management.ManagementFactory";
        Class type = loadClass(name, ManagementContext.class.getClassLoader());
        if (type != null) {
            try {
                Method method = type.getMethod("getPlatformMBeanServer", new Class[0]);
                if (method != null) {
                    Object answer = method.invoke(null, new Object[0]);
                    if (answer instanceof MBeanServer) {
                        return (MBeanServer) answer;
                    } else {
                        LOGGER.warn("Could not cast: {} into an MBeanServer. There must be some classloader strangeness in town", answer);
                    }
                } else {
                    LOGGER.warn("Method getPlatformMBeanServer() does not appear visible on type: {}", type.getName());
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to call getPlatformMBeanServer() due to: {}", e, e);
            }
        } else {
            LOGGER.trace("Class not found: {} so probably running on Java 1.4", name);
        }
        return null;
    }

    private static Class loadClass(String name, ClassLoader loader) {
        try {
            return loader.loadClass(name);
        } catch (ClassNotFoundException e) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(name);
            } catch (ClassNotFoundException e1) {
                return null;
            }
        }
    }

    /**
     * @return
     * @throws NullPointerException
     * @throws MalformedObjectNameException
     * @throws IOException
     */
    protected MBeanServer createMBeanServer() throws MalformedObjectNameException, IOException {
        MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer(jmxDomainName);
        locallyCreateMBeanServer = true;
        return mbeanServer;
    }

    public String getConnectorPath() {
        return connectorPath;
    }

    public void setConnectorPath(String connectorPath) {
        this.connectorPath = connectorPath;
    }

    public int getConnectorPort() {
        return connectorPort;
    }

    public void setConnectorPort(int connectorPort) {
        this.connectorPort = connectorPort;
    }

    public boolean isCreateConnector() {
        return createConnector;
    }

    public void setCreateConnector(boolean createConnector) {
        this.createConnector = createConnector;
    }

}
