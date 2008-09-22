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
package org.apache.servicemix.lwcontainer;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.xml.namespace.QName;

import org.apache.activemq.util.IdGenerator;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.common.endpoints.AbstractEndpoint;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;

public class LwContainerEndpoint extends AbstractEndpoint {

    private static final QName SERVICE_NAME = new QName("http://lwcontainer.servicemix.org", "LwContainerComponent");

    private ActivationSpec activationSpec;

    public LwContainerEndpoint(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
        this.service = SERVICE_NAME;
        if (activationSpec.getId() != null) {
            this.endpoint = activationSpec.getId();
        } else if (activationSpec.getComponentName() != null) {
            this.endpoint = activationSpec.getComponentName();
        } else {
            this.endpoint = new IdGenerator().generateId();
        }
    }

    public Role getRole() {
        throw new UnsupportedOperationException();
    }

    public void activate() throws Exception {
        getContainer().activateComponent(activationSpec);
    }

    public void deactivate() throws Exception {
        getContainer().deactivateComponent(activationSpec.getId());
    }

    public ExchangeProcessor getProcessor() {
        throw new UnsupportedOperationException();
    }
    
    public JBIContainer getContainer() {
        ComponentContext context = getServiceUnit().getComponent().getComponentContext();
        if (context instanceof ComponentContextImpl) {
            return ((ComponentContextImpl) context).getContainer();
        }
        throw new IllegalStateException("LwContainer component can only be deployed in ServiceMix");
    }

    @Override
    public void process(MessageExchange exchange) throws Exception {
        getProcessor().process(exchange);
    }

    @Override
    public void start() throws Exception {
        // gracefully do nothing
    }

    @Override
    public void stop() throws Exception {
        // gracefully do nothing
        
    }

}
