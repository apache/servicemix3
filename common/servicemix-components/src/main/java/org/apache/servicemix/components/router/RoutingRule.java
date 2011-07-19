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
package org.apache.servicemix.components.router;

import org.apache.servicemix.jbi.resolver.EndpointFilter;
import org.apache.servicemix.jbi.resolver.EndpointResolver;
import org.apache.servicemix.jbi.resolver.NullEndpointFilter;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;

/**
 * A simple routing rule which if the predicate is matched then the message is forwarded to
 * the given endpoint.
 *
 * @version $Revision$
 */
public class RoutingRule {

    private Predicate predicate;
    private EndpointResolver resolver;
    private EndpointFilter filter = NullEndpointFilter.getInstance();

    public EndpointFilter getFilter() {
        return filter;
    }

    public void setFilter(EndpointFilter filter) {
        this.filter = filter;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public EndpointResolver getResolver() {
        return resolver;
    }

    public void setResolver(EndpointResolver resolver) {
        this.resolver = resolver;
    }

    public void onMessageExchange(ComponentContext context, MessageExchange exchange) throws JBIException {
        if (predicate.evaluate(context, exchange)) {

            // lets forward the message on to the endpoint
            ServiceEndpoint endpoint = resolver.resolveEndpoint(context, exchange, filter);
            exchange.setEndpoint(endpoint);
        }
    }

}
