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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.MBeanServerNotFoundException;
import org.springframework.jmx.access.MBeanInfoRetrievalException;
import org.springframework.jmx.access.MBeanProxyFactoryBean;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;

/**
 * Variant of Spring's {@link MBeanProxyFactoryBean} that improves support for WebSphere by automatically
 * determining the cell/node/process attributes of the MBean ObjectName
 */
public class EnhancedMBeanProxyFactoryBean extends MBeanProxyFactoryBean {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(EnhancedMBeanProxyFactoryBean.class);

    protected static final String WEBSPHERE = "WebSphere";
	
	private MBeanServerConnection connection;
	private String originalName;

	@Override
	public void afterPropertiesSet() throws MBeanServerNotFoundException, MBeanInfoRetrievalException {
		try {
			if (WEBSPHERE.equals(connection.getDefaultDomain())) {
				doWebSphereConfiguration();
			}
		} catch (IOException e) {
			LOGGER.debug("Unable to determine default domain name - assuming it's not WebSphere", e);
		}
		super.afterPropertiesSet();
	}

    /*
     * When running in WebSphere, try determining the cell/node/process values and append those to the
     * JMX MBean object name.
     */
	private void doWebSphereConfiguration() {
		LOGGER.info("Running on WebSphere - adding cell/node/process information to {}", originalName);

		try {
            String cell = null;
            String node = null;
            String process = null;

			for (Object value : connection.queryNames(new ObjectName("WebSphere:name=JVM,*"), null)) {
				ObjectName objectName = (ObjectName) value;
				cell = objectName.getKeyProperty("cell");
				node = objectName.getKeyProperty("node");
				process = objectName.getKeyProperty("process");
			}

            String name = String.format("%s,cell=%s,node=%s,process=%s", originalName, cell, node, process);
            super.setObjectName(name);
            LOGGER.debug("Object name has been changed to {}", name);
		} catch (MalformedObjectNameException e) {
            throw new MBeanInfoRetrievalException("Unable to determine cell/node/process information while running in WebSphere", e);
		} catch (IOException e) {
            throw new MBeanInfoRetrievalException("Unable to determine cell/node/process information while running in WebSphere", e);
		}
	}

    // override to capture the MBean server connection
	@Override
	public void setServer(MBeanServerConnection connection) {
		this.connection = connection;
		super.setServer(connection);
	}

    // override to capture the original object name
    @Override
    public void setObjectName(Object name) throws MalformedObjectNameException {
        this.originalName = name != null ? name.toString() : null;
        super.setObjectName(name);
    }

}
