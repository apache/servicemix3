/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.jbi.resolver;

import org.servicemix.jbi.NoServiceEndpointAvailableException;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

/**
 * Resolves the endpoint using the service name and endpoint name to resolve the {@link ServiceEndpoint}
 *
 * @version $Revision$
 */
public class ServiceAndEndpointNameResolver implements EndpointResolver {
    private QName serviceName;
    private String endpointName;
    private boolean failIfUnavailable = true;

    public ServiceAndEndpointNameResolver() {
    }

    public ServiceAndEndpointNameResolver(QName serviceName, String endpointName) {
        this.serviceName = serviceName;
        this.endpointName = endpointName;
    }

    public ServiceEndpoint resolveEndpoint(ComponentContext context, MessageExchange exchange, EndpointFilter filter) throws JBIException {
        ServiceEndpoint endpoint = context.getEndpoint(serviceName, endpointName);
        if (!filter.evaluate(endpoint, exchange)) {
            endpoint = null;
        }
        if (endpoint == null && failIfUnavailable) {
            throw new NoServiceEndpointAvailableException(serviceName, endpointName);
        }
        return endpoint;
    }

    public ServiceEndpoint[] resolveAvailableEndpoints(ComponentContext context, MessageExchange exchange) throws JBIException {
        ServiceEndpoint endpoint = context.getEndpoint(serviceName, endpointName);
        if (endpoint != null) {
            return new ServiceEndpoint[]{endpoint};
        }
        else {
            return new ServiceEndpoint[0];
        }
    }

    public QName getServiceName() {
        return serviceName;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public boolean isFailIfUnavailable() {
        return failIfUnavailable;
    }

    public void setFailIfUnavailable(boolean failIfUnavailable) {
        this.failIfUnavailable = failIfUnavailable;
    }
}
