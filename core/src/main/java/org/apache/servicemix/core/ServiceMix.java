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

import org.apache.servicemix.api.Channel;
import org.apache.servicemix.api.EndpointRegistry;
import org.apache.servicemix.api.NMR;
import org.apache.servicemix.api.event.ListenerRegistry;
import org.apache.servicemix.api.internal.FlowRegistry;

/**
 *
 */
public class ServiceMix implements NMR {

    private EndpointRegistry endpoints;
    private ListenerRegistry listeners;
    private FlowRegistry flows;

    /**
     * Access the endpoint registry.
     *
     * @return the endpoint registry
     */
    public EndpointRegistry getEndpointRegistry() {
        return endpoints;
    }

    /**
     * Access the listener registry.
     *
     * @return the listener registry
     */
    public ListenerRegistry getListenerRegistry() {
        return listeners;
    }

    /**
     * Access the flow registry.
     *
     * @return the flow registry
     */
    public FlowRegistry getFlowRegistry() {
        return flows;
    }

    /**
     * Create a channel to interact with the NMR without exposing an endpoint.
     *
     * @return a channel
     */
    public Channel createChannel() {
        return new ClientChannel(this);
    }
}
