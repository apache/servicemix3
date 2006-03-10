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
package org.apache.servicemix.gbean;

import java.io.IOException;

import javax.jbi.JBIException;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.transaction.context.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.servicemix.jbi.container.JBIContainer;

public class ServiceMixGBean implements GBeanLifecycle, ServiceMixContainer {

    private Log log = LogFactory.getLog(getClass().getName());
    
    private JBIContainer container;
    private String name;
    private String directory;
    private TransactionContextManager transactionContextManager;
    private WorkManager workManager;

    private JMXConnectorServer connectorServer;
    private int namingPort = 1099;
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ServiceMix JBI Container", ServiceMixGBean.class, "JBIContainer");
        infoFactory.addInterface(ServiceMixContainer.class);
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("directory", String.class, true);
        infoFactory.addReference("transactionContextManager", TransactionContextManager.class);
        infoFactory.addReference("workManager", WorkManager.class);
        infoFactory.setConstructor(new String[]{"name", "directory"});
        infoFactory.setConstructor(new String[]{"name", "directory", "transactionContextManager", "workManager"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public ServiceMixGBean(String name, String directory) {
        this(name, directory, null, null);
    }

    public ServiceMixGBean(String name, String directory, TransactionContextManager transactionContextManager, WorkManager workManager) {
        this.name = name;
        this.directory = directory;
        this.transactionContextManager = transactionContextManager;
        this.workManager = workManager;
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean created");
        }
    }
    
    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean doStart");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServiceMixGBean.class.getClassLoader());
        try {
            if (container == null) {
                container = createContainer();
                container.init();
                container.start();
                // Create a JMX Connector
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + namingPort
                        + "/jmxrmi");
                // Create and start the RMIConnectorServer
                MBeanServer server = container.getMBeanServer();
                connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, null, server);
                connectorServer.start();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean doStop");
        }
        try {
            if (container != null) {
                container.shutDown();
                connectorServer.stop();
            }
        } finally {
            container = null;
            connectorServer = null;
        }
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean doFail");
        }
        try {
            if (container != null) {
                try {
                    container.shutDown();
                }
                catch (JBIException e) {
                    log.info("Caught while closing due to failure: " + e, e);
                }
                try {
                    connectorServer.stop();
                } catch (IOException e) {
                    log.info("Caught while closing due to failure: " + e, e);
                }
            }
        } finally {
            container = null;
            connectorServer = null;
        }
    }

    private JBIContainer createContainer() {
        JBIContainer container = new JBIContainer();
        container.setName(name);
        container.setRootDir(directory);
        container.setTransactionManager(getTransactionManager());
        container.setMonitorInstallationDirectory(false);
        container.setMonitorDeploymentDirectory(false);
        container.setWorkManager(workManager);
        return container;
    }
    
    public TransactionManager getTransactionManager() {
        if (transactionContextManager != null) {
            return new GeronimoTransactionManager(transactionContextManager);
        }
        return null;
    }
    
    public JBIContainer getContainer() {
        return container;
    }

}
