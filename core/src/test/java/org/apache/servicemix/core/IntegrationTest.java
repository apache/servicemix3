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

import org.apache.servicemix.api.*;
import org.apache.servicemix.api.service.ServiceHelper;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test
 */
public class IntegrationTest {

    private NMR nmr;

    @Before
    public void setUp() {
        ServiceMix smx = new ServiceMix();
        smx.setListenerRegistry(new ListenerRegistryImpl());
        smx.setEndpointRegistry(new EndpointRegistryImpl(smx));
        smx.setFlowRegistry(new FlowRegistryImpl());
        smx.getFlowRegistry().register(new StraightThroughFlow(), ServiceHelper.createMap());
        nmr = smx;
    }

    @Test
    public void testSendExchangeToEndpointUsingClient() throws Exception {
        MyEndpoint endpoint = new MyEndpoint();
        nmr.getEndpointRegistry().register(endpoint, ServiceHelper.createMap(Endpoint.ID, "id"));
        Channel client = nmr.createChannel();
        Exchange e = client.createExchange(Pattern.InOnly);
        e.setTarget(nmr.getEndpointRegistry().lookup(ServiceHelper.createMap(Endpoint.ID, "id")));
        e.getIn().setContent("Hello");
        boolean res = client.sendSync(e);
        assertTrue(res);
        assertNotNull(endpoint.getExchange());
        assertEquals(Status.Done, e.getStatus());
    }


    public static class MyEndpoint implements Endpoint {

        private Channel channel;
        private Exchange exchange;

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public void process(Exchange exchange) {
            this.exchange = exchange;
            exchange.setStatus(Status.Done);
            channel.send(exchange);
        }

        public Exchange getExchange() {
            return exchange;
        }
    }
}
