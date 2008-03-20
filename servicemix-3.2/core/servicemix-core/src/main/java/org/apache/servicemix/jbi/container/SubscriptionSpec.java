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
package org.apache.servicemix.jbi.container;

import java.io.Serializable;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.messaging.ExchangePacket;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.resolver.SubscriptionFilter;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

/**
 * Represents a subscription to a JBI endpoint.
 * 
 * @org.apache.xbean.XBean element="subscriptionSpec"
 * 
 * @version $Revision$
 */
public class SubscriptionSpec implements Serializable {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 8458586342841647313L;

    private QName service;
    private QName interfaceName;
    private QName operation;
    private String endpoint;
    private transient SubscriptionFilter filter;
    private ComponentNameSpace name;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public SubscriptionFilter getFilter() {
        return filter;
    }

    public void setFilter(SubscriptionFilter filter) {
        this.filter = filter;
    }

    public QName getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    public QName getOperation() {
        return operation;
    }

    public void setOperation(QName operation) {
        this.operation = operation;
    }

    public QName getService() {
        return service;
    }

    public void setService(QName service) {
        this.service = service;
    }

    /**
     * @return Returns the name.
     */
    public ComponentNameSpace getName() {
        return name;
    }

    /**
     * @org.apache.xbean.XBean hide="true"
     * 
     * @param name
     *            The name to set.
     */
    public void setName(ComponentNameSpace name) {
        this.name = name;
    }

    /**
     * Returns true if this subscription matches the given message exchange
     * 
     * @param exchange
     *            the exchange to be matched
     * @return true if this subscription matches the exchange
     */
    public boolean matches(Registry registry, MessageExchangeImpl exchange) {
        boolean result = false;

        ExchangePacket packet = exchange.getPacket();
        ComponentNameSpace sourceId = packet.getSourceId();
        if (sourceId != null) {
            // get the list of services
            if (service != null) {
                ServiceEndpoint[] ses = registry.getEndpointsForService(service);
                if (ses != null) {
                    for (int i = 0; i < ses.length; i++) {
                        InternalEndpoint se = (InternalEndpoint) ses[i];
                        if (se.getComponentNameSpace() != null && se.getComponentNameSpace().equals(sourceId)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
            if (result && interfaceName != null) {
                ServiceEndpoint[] ses = registry.getEndpointsForInterface(interfaceName);
                if (ses != null) {
                    result = false;
                    for (int i = 0; i < ses.length; i++) {
                        InternalEndpoint se = (InternalEndpoint) ses[i];
                        if (se.getComponentNameSpace() != null && se.getComponentNameSpace().equals(sourceId)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
        }
        
        // allow a match all subscription
        if (service == null && interfaceName == null) {
            result = true;
        }
        if (result && filter != null) {
            result = filter.matches(exchange);
        }
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof SubscriptionSpec) {
            SubscriptionSpec other = (SubscriptionSpec) obj;
            result = (name == null && other.name == null || name.equals(other.name))
                    && (service == null && other.service == null)
                    || (service != null && other.service != null && service.equals(other.service))
                    && (interfaceName == null && other.interfaceName == null)
                    || (interfaceName != null && other.interfaceName != null && interfaceName
                            .equals(other.interfaceName)) && (endpoint == null && other.endpoint == null)
                    || (endpoint != null && other.endpoint != null && endpoint.equals(other.endpoint));

        }
        return result;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (name != null ? name.hashCode() : 0)
                ^ (service != null ? service.hashCode() : (interfaceName != null ? interfaceName.hashCode() : super
                        .hashCode()));
    }

}
