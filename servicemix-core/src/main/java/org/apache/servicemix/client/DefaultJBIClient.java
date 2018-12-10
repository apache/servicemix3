/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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


import org.apache.servicemix.client.*;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessagingException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * @version $Revision: $
 */
public class DefaultJBIClient extends ComponentSupport implements Client {

    private JBIContainer container;
    
    public DefaultJBIClient() {
    }

    /**
     * Provides the JBI container used for message dispatch.
     */
    public DefaultJBIClient(JBIContainer container) throws JBIException {
        this(container, new ActivationSpec());
    }

    /**
     * Provides the JBI container and the activation specification, which can be used to register this
     * client at a specific endpoint so that default container routing rules can be configured via dependency injection
     * and the client endpoint metadata can be configured to allow services to talk to this client.
     */
    public DefaultJBIClient(JBIContainer container, ActivationSpec activationSpec) throws JBIException {
        activationSpec.setComponent(this);
        container.activateComponent(activationSpec);
    }

    public Destination createEndpoint(URI uri) throws JBIException {
        // TODO
        /*
        ensureEndpointCreated(uri);
        return new DefaultEndpoint(this);
        */
        return null;
    }

    public Destination createEndpoint(String uri) throws URISyntaxException, JBIException {
        return createEndpoint(new URI(uri));
    }
    
    // Properties
    // -------------------------------------------------------------------------
    public JBIContainer getContainer() {
        if (container == null) {
            container = createContainer();
        }
        return container;
    }

    public void setContainer(JBIContainer container) {
        this.container = container;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected JBIContainer createContainer() {
        return new JBIContainer();
    }

    protected void ensureEndpointCreated(URI uri) {
    }
}
