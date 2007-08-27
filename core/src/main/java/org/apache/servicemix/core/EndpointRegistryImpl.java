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
package org.apache.servicemix.core;

import org.apache.servicemix.api.Endpoint;
import org.apache.servicemix.api.EndpointRegistry;
import org.apache.servicemix.api.NMR;
import org.apache.servicemix.api.Reference;
import org.apache.servicemix.api.internal.InternalEndpoint;
import org.apache.servicemix.api.service.ServiceRegistry;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @version $Revision: $
 * @since 4.0
 */
public class EndpointRegistryImpl implements EndpointRegistry {

    private NMR nmr;
    private Map<Endpoint, InternalEndpoint> endpoints = new ConcurrentHashMap<Endpoint, InternalEndpoint>();
    private ServiceRegistry<InternalEndpoint> registry = new ServiceRegistryImpl<InternalEndpoint>();

    /**
     * Register the given endpoint in the registry.
     * In an OSGi world, this would be performed automatically by a ServiceTracker.
     * Upon registration, a {@link org.apache.servicemix.api.Channel} will be injected onto the Endpoint using
     * the {@link org.apache.servicemix.api.Endpoint#setChannel(org.apache.servicemix.api.Channel)} method.
     *
     * @param endpoint   the endpoint to register
     * @param properties the metadata associated with this endpoint
     * @see org.apache.servicemix.api.Endpoint
     */
    public void register(Endpoint endpoint, Map<String, ?> properties) {
        Executor executor = Executors.newCachedThreadPool();
        InternalEndpointWrapper wrapper = new InternalEndpointWrapper(endpoint);
        ChannelImpl channel = new ChannelImpl(wrapper, executor, nmr);
        wrapper.setChannel(channel);
        endpoints.put(endpoint, wrapper);
        registry.register(wrapper, properties);
    }

    /**
     * Unregister a previously register enpoint.
     * In an OSGi world, this would be performed automatically by a ServiceTracker.
     *
     * @param endpoint the endpoint to unregister
     */
    public void unregister(Endpoint endpoint) {
        InternalEndpoint wrapper = endpoints.remove(endpoint);
        registry.unregister(wrapper);
    }

    /**
     * Get a set of registered services.
     *
     * @return the registered services
     */
    public Set<Endpoint> getServices() {
        return null;  // TODO
    }

    /**
     * Retrieve the metadata associated to a registered service.
     *
     * @param service the service for which to retrieve metadata
     * @return the metadata associated with the service
     */
    public Map<String, ?> getProperties(Endpoint service) {
        return null;  // TODO
    }

    /**
     * From a given amount of metadata which could include interface name, service name
     * policy data and so forth, choose an available endpoint reference to use
     * for invocations.
     * <p/>
     * This could return actual endpoints, or a dynamic proxy to a number of endpoints
     */
    public Reference lookup(Map<String, ?> properties) {
        List<InternalEndpoint> endpoints = new ArrayList<InternalEndpoint>();
        for (InternalEndpoint e : registry.getServices()) {
            boolean match = true;
            for (String name : properties.keySet()) {
                if (!properties.get(name).equals(registry.getProperties(e).get(name))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                endpoints.add(e);
            }
        }
        return new ReferenceImpl(endpoints);
    }

    /**
     * This methods creates a Reference from its xml representation.
     *
     * @param xml the xml document describing this reference
     * @return a new Reference
     * @see org.apache.servicemix.api.Reference#toXml()
     */
    public synchronized Reference lookup(Document xml) {
        // TODO: implement
        return null;
    }

}
