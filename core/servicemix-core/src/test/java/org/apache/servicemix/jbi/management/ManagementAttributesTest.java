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

import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
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
public class ManagementAttributesTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ManagementAttributesTest.class);

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

    public void testRemote() throws Exception {
        // The address of the connector server
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + namingHost + ":" + namingPort + jndiPath);
        // Connect a JSR 160 JMXConnector to the server side
        JMXConnector connector = JMXConnectorFactory.connect(url);
        // Retrieve an MBeanServerConnection that represent the MBeanServer the
        // remote
        // connector server is bound to
        MBeanServerConnection connection = connector.getMBeanServerConnection();

        LOGGER.info(connection.getMBeanCount().toString());

        Set set = connection.queryNames(new ObjectName(connection.getDefaultDomain() + ":*"), null);
        for (Iterator iter = set.iterator(); iter.hasNext();) {
            ObjectName name = (ObjectName) iter.next();
            LOGGER.info(name.toString());
            MBeanInfo info = connection.getMBeanInfo(name);
            MBeanAttributeInfo[] mia = info.getAttributes();
            String[] attrNames = new String[mia.length];
            for (int i = 0; i < mia.length; i++) {
                attrNames[i] = mia[i].getName();
                LOGGER.info("attr " + mia[i].getName() + " " + mia[i].getType() + " " + connection.getAttribute(name, mia[i].getName()));
            }

            AttributeList attributeList = (AttributeList) connection.getAttributes(name, attrNames);
            for (int i = 0; i < attributeList.size(); i++) {
                Attribute attribute = (Attribute) attributeList.get(i);
                LOGGER.info("bulk " + attribute.getName() + " " + attribute.getValue() + " " + attribute.toString());
            }
        }
    }

}
