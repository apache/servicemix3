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

import javax.jbi.management.AdminServiceMBean;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ManagementContextTest
 */
public class AdminServiceTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AdminServiceTest.class);

    private JBIContainer container;

    // The host, port and path where the rmiregistry runs.
    private String namingHost = "localhost";

    private int namingPort = ManagementContext.DEFAULT_CONNECTOR_PORT;

    private String jndiPath = ManagementContext.DEFAULT_CONNECTOR_PATH;

    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setRmiPort(namingPort);
        container.setCreateMBeanServer(true);
        container.init();
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }

    public void testAdminService() throws Exception {
        // The address of the connector server
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + namingHost + ":" + namingPort + jndiPath);
        // Connect a JSR 160 JMXConnector to the server side
        JMXConnector connector = JMXConnectorFactory.connect(url);
        // Retrieve an MBeanServerConnection that represent the MBeanServer the remote
        // connector server is bound to
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        // Call the server side as if it is a local MBeanServer
        ObjectName asmName = getObjectName(ManagementContext.class);
        Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, asmName, AdminServiceMBean.class, true);
        AdminServiceMBean asm = (AdminServiceMBean) proxy;

        LOGGER.info(asm.getBindingComponents().toString());
        LOGGER.info(asm.getComponentByName("toto").toString());
    }

    protected ObjectName getObjectName(Class systemClass) {
        return ManagementContext.getSystemObjectName(ManagementContext.DEFAULT_DOMAIN, JBIContainer.DEFAULT_NAME, systemClass);
    }

}
