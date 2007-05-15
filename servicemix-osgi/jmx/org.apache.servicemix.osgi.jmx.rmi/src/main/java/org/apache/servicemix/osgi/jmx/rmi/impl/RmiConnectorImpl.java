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
package org.apache.servicemix.osgi.jmx.rmi.impl;

import java.io.IOException;
import java.util.Map;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServerMBean;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.MBeanServerForwarder;

import org.apache.servicemix.osgi.jmx.rmi.RmiConnector;

/**
 * 
 * @author gnodet
 */
public class RmiConnectorImpl implements RmiConnector {

    private JMXConnectorServerMBean connector;

    public RmiConnectorImpl(JMXConnectorServerMBean connector) {
        this.connector = connector;
    }

    /**
     * @return
     * @see javax.management.remote.JMXConnectorServerMBean#getAddress()
     */
    public JMXServiceURL getAddress() {
        return connector.getAddress();
    }

    /**
     * @return
     * @see javax.management.remote.JMXConnectorServerMBean#getAttributes()
     */
    public Map<String, ?> getAttributes() {
        return connector.getAttributes();
    }

    /**
     * @return
     * @see javax.management.remote.JMXConnectorServerMBean#getConnectionIds()
     */
    public String[] getConnectionIds() {
        return connector.getConnectionIds();
    }

    /**
     * @return
     * @see javax.management.remote.JMXConnectorServerMBean#isActive()
     */
    public boolean isActive() {
        return connector.isActive();
    }

    /**
     * @param mbsf
     * @see javax.management.remote.JMXConnectorServerMBean#setMBeanServerForwarder(javax.management.remote.MBeanServerForwarder)
     */
    public void setMBeanServerForwarder(MBeanServerForwarder mbsf) {
        connector.setMBeanServerForwarder(mbsf);
    }

    /**
     * @throws IOException
     * @see javax.management.remote.JMXConnectorServerMBean#start()
     */
    public void start() throws IOException {
        connector.start();
    }

    /**
     * @throws IOException
     * @see javax.management.remote.JMXConnectorServerMBean#stop()
     */
    public void stop() throws IOException {
        connector.stop();
    }

    /**
     * @param env
     * @return
     * @throws IOException
     * @see javax.management.remote.JMXConnectorServerMBean#toJMXConnector(java.util.Map)
     */
    public JMXConnector toJMXConnector(Map<String, ?> env) throws IOException {
        return connector.toJMXConnector(env);
    }

}
