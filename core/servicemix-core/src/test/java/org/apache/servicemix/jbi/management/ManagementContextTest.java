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
import javax.jbi.management.LifeCycleMBean;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerDelegateMBean;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ManagementContextTest
 */
public class ManagementContextTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ManagementContextTest.class);

    // The host, port and path where the rmiregistry runs.
    private String namingHost = "localhost";

    private int namingPort = ManagementContext.DEFAULT_CONNECTOR_PORT;

    private String jndiPath = ManagementContext.DEFAULT_CONNECTOR_PATH;

    private ManagementContext context;

    private JBIContainer container;

    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setCreateMBeanServer(true);
        container.setRmiPort(namingPort);
        container.init();
        container.start();
        context = container.getManagementContext();
        /*
         * context = new ManagementContext();
         * context.setCreateMBeanServer(true);
         * context.setNamingPort(namingPort); context.init(container, null);
         */
    }

    protected void tearDown() throws Exception {
        container.shutDown();
    }

    public void testRemote() throws Exception {
        // The address of the connector server
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + namingHost + ":" + namingPort + jndiPath);
        // Connect a JSR 160 JMXConnector to the server side
        JMXConnector connector = JMXConnectorFactory.connect(url);
        // Retrieve an MBeanServerConnection that represent the MBeanServer the
        // remote
        // connector server is bound to
        MBeanServerConnection connection = connector.getMBeanServerConnection();
        // Call the server side as if it is a local MBeanServer
        ObjectName delegateName = ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate");
        Object proxy = MBeanServerInvocationHandler.newProxyInstance(connection, delegateName, MBeanServerDelegateMBean.class, true);
        MBeanServerDelegateMBean delegate = (MBeanServerDelegateMBean) proxy;
        // The magic of JDK 1.3 dynamic proxy and JSR 160:
        // delegate.getImplementationVendor() is actually a remote JMX call,
        // but it looks like a local, old-style, java call.
        LOGGER.info("{} is cool !", delegate.getImplementationVendor());

        ObjectName objName = context.createObjectName(context);

        proxy = MBeanServerInvocationHandler.newProxyInstance(connection, objName, LifeCycleMBean.class, true);
        LifeCycleMBean mc = (LifeCycleMBean) proxy;
        LOGGER.info("STATE = {}", mc.getCurrentState());
        mc.start();
        LOGGER.info("STATE = {}", mc.getCurrentState());
        mc.stop();
        LOGGER.info("STATE = {}", mc.getCurrentState());
    }

    public void testComponent() throws Exception {
        ObjectName[] names = context.getPojoComponents();
        assertEquals(1, names.length);
        EchoComponent echo = new EchoComponent();
        container.activateComponent(echo, "echo");
        names = context.getPojoComponents();
        assertNotNull(names);
        assertEquals(2, names.length);
        assertEquals(LifeCycleMBean.STARTED, echo.getCurrentState());
        context.stopComponent("echo");
        assertEquals(LifeCycleMBean.STOPPED, echo.getCurrentState());
    }

    public void testGetSystemObjectNameQuery() throws MalformedObjectNameException {
        assertEquals(new ObjectName("org.apache.servicemix:ContainerName=ServiceMix,Type=SystemService,Name=AdminService,*"),
                     ManagementContext.getSystemObjectNameQuery("org.apache.servicemix", "ServiceMix", AdminServiceMBean.class));
    }

}
