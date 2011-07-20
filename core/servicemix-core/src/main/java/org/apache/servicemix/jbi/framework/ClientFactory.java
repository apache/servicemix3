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
package org.apache.servicemix.jbi.framework;

import java.io.Serializable;

import javax.jbi.JBIException;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.naming.NamingException;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFactory extends BaseSystemService implements ClientFactoryMBean, Serializable {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ClientFactory.class);
    
    private String jndiName = DEFAULT_JNDI_NAME;

    private boolean isFactoryJNDIregistered;

    public ClientFactory() {
    }
    
    /**
     * @return the jndiName
     */
    public String getJndiName() {
        return jndiName;
    }

    /**
     * @param jndiName the jndiName to set
     */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public ServiceMixClient createClient() throws JBIException {
        return new DefaultServiceMixClient(getContainer());
    }

    protected Class getServiceMBean() {
        return ClientFactoryMBean.class;
    }

    public String getDescription() {
        return "Client Factory Service";
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        helper.addOperation(getObjectToManage(), "createClient", 0, "create a new client");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void start() throws javax.jbi.JBIException {
        try {
            getContainer().getNamingContext().bind(jndiName, this);
            isFactoryJNDIregistered = true;
        } catch (NamingException e) {
            LOGGER.warn("Could not start ClientFactory: {}", e.getMessage());
            LOGGER.debug("Could not start ClientFactory.", e);
            isFactoryJNDIregistered = false;
        }
        super.start();
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException if the item fails to stop.
     */
    public void stop() throws javax.jbi.JBIException {
        super.stop();
        try {
            if (isFactoryJNDIregistered) {
                getContainer().getNamingContext().unbind(jndiName);
            }
        } catch (NamingException e) {
            LOGGER.warn("Could not stop ClientFactory: {}", e.getMessage());
            LOGGER.debug("Could not stop ClientFactory", e);
        }
    }

}
