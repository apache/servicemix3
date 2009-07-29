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
package org.apache.servicemix.samples.clients.jmx;

import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.servicemix.jbi.management.ManagementContext;

/**
 * <p>
 * A simple JMX client to connect to a local ServiceMix instance and monitor it.
 * </p>
 * 
 * @author jbonofre
 */
public class Client {
    
    /**
     * <p>
     * Main method to connect to ServiceMix and retrieve the list of service assemblies.
     * </p>
     * 
     * @param args main arguments.
     * @throws Exception in case of error.
     */
    public static final void main(String[] args) throws Exception {
        // create the JMX service URL.
        JMXServiceURL jmxServiceUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + ManagementContext.DEFAULT_CONNECTOR_PORT + ManagementContext.DEFAULT_CONNECTOR_PATH);
        // get the JMX connector.
        String[] credentials = new String[] { "smx", "smx" };
        Map<String, Object> environment = new HashMap<String, Object>();
        environment.put(JMXConnector.CREDENTIALS, credentials);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl);
        
        // get the AdminCommandsServiceMBean
        ObjectName objectName = ManagementContext.getSystemObjectName(ManagementContext.DEFAULT_DOMAIN, JBIContainer.DEFAULT_NAME, AdminCommandsServiceMBean.class);
        AdminCommandsServiceMBean adminCommandsServiceMBean = (AdminCommandsServiceMBean) MBeanServerInvocationHandler.newProxyInstance(jmxConnector.getMBeanServerConnection(), objectName, AdminCommandsServiceMBean.class, true);
        
        // list components deployed into the SMX instance
        System.out.println("Components available: ");
        System.out.println(adminCommandsServiceMBean.listComponents(false, false, false, null, null, null));
        
        // list service assemblies into the SMX instance
        System.out.println("Service Assemblies available: ");
        System.out.println(adminCommandsServiceMBean.listServiceAssemblies(null, null, null));
        
        // close the JMX connection.
        jmxConnector.close();
    }

}
