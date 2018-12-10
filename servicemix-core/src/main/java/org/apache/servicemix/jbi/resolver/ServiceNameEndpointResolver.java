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
package org.apache.servicemix.jbi.resolver;

import org.apache.servicemix.jbi.NoServiceAvailableException;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

/**
 * Resolves the endpoint using the service name with a pluggable {@link EndpointChooser} selection
 * policy if more than one endpoints are found.
 *
 * @version $Revision$
 */
public class ServiceNameEndpointResolver extends EndpointResolverSupport {

    private QName serviceName;

    public ServiceNameEndpointResolver() {
    }

    public ServiceNameEndpointResolver(QName serviceName) {
        this.serviceName = serviceName;
    }

    public ServiceEndpoint[] resolveAvailableEndpoints(ComponentContext context, MessageExchange exchange) {
        return context.getEndpointsForService(serviceName);
    }

    // Properties
    //-------------------------------------------------------------------------
    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected JBIException createServiceUnavailableException() {
        return new NoServiceAvailableException(serviceName);
    }
}
