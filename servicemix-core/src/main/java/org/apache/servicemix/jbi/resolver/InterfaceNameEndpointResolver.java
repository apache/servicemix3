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

import org.apache.servicemix.jbi.NoInterfaceAvailableException;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

/**
 * Resolves the endpoint using the interface name with a pluggable {@link EndpointChooser} selection
 * policy if more than one endpoints are found.
 *
 * @version $Revision$
 */
public class InterfaceNameEndpointResolver extends EndpointResolverSupport {

    private QName interfaceName;

    public InterfaceNameEndpointResolver() {
    }

    public InterfaceNameEndpointResolver(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    public ServiceEndpoint[] resolveAvailableEndpoints(ComponentContext context, MessageExchange exchange) {
        return context.getEndpoints(interfaceName);
    }


    // Properties
    //-------------------------------------------------------------------------
    public QName getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }
    protected JBIException createServiceUnavailableException() {
        return new NoInterfaceAvailableException(interfaceName);
    }
}
