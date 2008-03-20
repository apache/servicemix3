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
package org.apache.servicemix.jbi.installation;

import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public class ComponentDelegate implements Component {

    private Component delegate;

    public ComponentDelegate(Component delegate) {
        this.delegate = delegate;
    }

    public Component getDelegate() {
        return delegate;
    }

    public void setDelegate(Component delegate) {
        this.delegate = delegate;
    }

    public ComponentLifeCycle getLifeCycle() {
        return delegate.getLifeCycle();
    }

    public Document getServiceDescription(ServiceEndpoint endpoint) {
        return delegate.getServiceDescription(endpoint);
    }

    public ServiceUnitManager getServiceUnitManager() {
        return delegate.getServiceUnitManager();
    }

    public boolean isExchangeWithConsumerOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return delegate.isExchangeWithConsumerOkay(endpoint, exchange);
    }

    public boolean isExchangeWithProviderOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return delegate.isExchangeWithProviderOkay(endpoint, exchange);
    }

    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        return delegate.resolveEndpointReference(epr);
    }

}
