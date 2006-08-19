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
package org.apache.servicemix.web.jmx;

import org.apache.activemq.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import java.util.List;

/**
 * A Flow provides different dispatch policies within the NMR
 *
 * @version $Revision: 356583 $
 */
public class ManagementContext implements Service {
    /**
     * Default servicemix domain
     */
    public static String DEFAULT_DOMAIN = "org.apache.activemq";

    private final static Log log = LogFactory.getLog(ManagementContext.class);

    private MBeanServer beanServer;
    private String jmxDomainName = DEFAULT_DOMAIN;
    private boolean useMBeanServer = true;
    private boolean createMBeanServer = true;
    private boolean locallyCreateMBeanServer = false;

    public ManagementContext() {
        this(null);
    }

    public ManagementContext(MBeanServer server) {
        this.beanServer = server;
    }

    public void start() throws JMSException {
        // lets force the MBeanServer to be created if needed
        getMBeanServer();
    }

    public void stop() throws JMSException {
        if (locallyCreateMBeanServer && beanServer != null) {
            // check to see if the factory knows about this server
            List list = MBeanServerFactory.findMBeanServer(null);
            if (list != null && !list.isEmpty() && list.contains(beanServer)) {
                MBeanServerFactory.releaseMBeanServer(beanServer);
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
        }
        catch (MalformedObjectNameException e) {
            log.error("Couldn't create ObjectName from: " + type + " , " + name);
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
    public static ObjectName getSystemObjectName(String domainName, String containerName, Class theClass)
            throws MalformedObjectNameException, NullPointerException {
        String tmp = domainName + ":" + "type=" + theClass.getName() + ",name="
                + getRelativeName(containerName, theClass);
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
                // lets piggy back on another MBeanServer -
                // we could be in an appserver!
                List list = MBeanServerFactory.findMBeanServer(null);
                if (list != null && list.size() > 0) {
                    result = (MBeanServer) list.get(0);
                }
            }

            if (result == null && createMBeanServer) {
                result = MBeanServerFactory.createMBeanServer(jmxDomainName);
                locallyCreateMBeanServer = true;
            }
        }
        catch (NoClassDefFoundError e) {
            log.error("Couldnot load MBeanServer", e);
        }
        catch (Throwable e) {
            // probably don't have access to system properties
            log.error("Failed to initialize MBeanServer", e);
        }
        return result;
    }
}
