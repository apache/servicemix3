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
package org.apache.servicemix.client;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.naming.InitialContext;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A factory bean which creates a ServiceMixClient.
 * It first try to use the configured ClientFactory, the ComponentContext
 * then JBIContainer and at last, it tries to locate the ClientFactory
 * in JNDI under the provided name.
 * 
 * @author <a href="mailto:gnodet [at] gmail.com">Guillaume Nodet</a>
 * @org.apache.xbean.XBean element="client"
 */
public class ClientFactoryBean implements FactoryBean, InitializingBean {

    private String name = ClientFactory.DEFAULT_JNDI_NAME;
    private JBIContainer container;
    private ClientFactory factory;
    private ComponentContext context;
    
    public ClientFactoryBean() {
    }

    /**
     * @return the context
     */
    public ComponentContext getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    /**
     * @return the container
     */
    public JBIContainer getContainer() {
        return container;
    }

    /**
     * @param container the container to set
     */
    public void setContainer(JBIContainer container) {
        this.container = container;
    }

    /**
     * @return the factory
     */
    public ClientFactory getFactory() {
        return factory;
    }

    /**
     * @param factory the factory to set
     */
    public void setFactory(ClientFactory factory) {
        this.factory = factory;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public Object getObject() throws Exception {
        return factory.createClient();
    }

    public Class getObjectType() {
        return ServiceMixClient.class;
    }

    public boolean isSingleton() {
        return false;
    }
    
    public void afterPropertiesSet() throws Exception {
        if (factory == null) {
            if (context != null) {
                factory = new ClientFactory() {
                    public ServiceMixClient createClient() throws JBIException {
                        return new ServiceMixClientFacade(context);
                    }
                };
            } else if (container != null) {
                factory = container.getClientFactory();
            } else {
                factory = (ClientFactory) new InitialContext().lookup(name);
            }
        }
    }
    
}
