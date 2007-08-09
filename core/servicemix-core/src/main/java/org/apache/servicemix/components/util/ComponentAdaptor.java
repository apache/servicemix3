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
package org.apache.servicemix.components.util;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

/**
 * A simple adaptor which can be used to turn any instance of
 * a {@link ComponentLifeCycle} into a fully fledged JBI {@link Component}
 *
 * @version $Revision$
 */
public class ComponentAdaptor implements Component {

    private ComponentLifeCycle lifeCycle;
    private QName service;
    private String endpoint;
    private ComponentContext context;
    private ServiceUnitManager serviceManager;


    public ComponentAdaptor(ComponentLifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public ComponentAdaptor(ComponentLifeCycle lifeCycle, QName service, String endpoint) {
        this.lifeCycle = lifeCycle;
        this.service = service;
        this.endpoint = endpoint;
    }

    /**
     * Called when the Component is initialized
     *
     * @param ctx
     * @throws javax.jbi.JBIException
     */
    public void init(ComponentContext ctx) throws JBIException {
        this.context = ctx;
        if (service != null && endpoint != null) {
            ctx.activateEndpoint(service, endpoint);
        }
    }

    /**
     * @return the lifecycel control implementation
     */
    public ComponentLifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public ServiceUnitManager getServiceUnitManager() {
        initializeServiceUnitManager();
        return serviceManager;
    }

    public void setServiceManager(ServiceUnitManager serviceManager) {
        this.serviceManager = serviceManager;
    }


    public ComponentContext getContext() {
        return context;
    }

    public String toString() {
        return getClass().getName() + " for " + lifeCycle;
    }

    protected synchronized void initializeServiceUnitManager() {
        if (this.serviceManager == null) {
            this.serviceManager = createServiceUnitManager();
        }
    }

    protected ServiceUnitManager createServiceUnitManager() {
        return new ServiceUnitManagerSupport();
    }


    public Document getServiceDescription(ServiceEndpoint ep) {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean isExchangeWithConsumerOkay(ServiceEndpoint ep, MessageExchange exchange) {
        // TODO Auto-generated method stub
        return true;
    }


    public boolean isExchangeWithProviderOkay(ServiceEndpoint ep, MessageExchange exchange) {
        // TODO Auto-generated method stub
        return true;
    }


    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        // TODO Auto-generated method stub
        return null;
    }

}
