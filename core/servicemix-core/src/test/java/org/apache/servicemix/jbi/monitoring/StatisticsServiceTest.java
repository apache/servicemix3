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
package org.apache.servicemix.jbi.monitoring;

import java.util.concurrent.CountDownLatch;

import javax.jbi.JBIException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.event.EndpointAdapter;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.messaging.DeliveryChannelImplTest.TestComponent;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;

/**
 * Test case for {@link StatisticsService}
 */
public class StatisticsServiceTest extends TestCase {
    
    private static final String COMPONENT = "component";
    private static final String ENDPOINT = "endpoint";
    private static final QName SERVICE = new QName("service");

    protected JBIContainer container;
    private StatisticsService service;

    protected void setUp() throws Exception {
        // set up a test container instance
        container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
        // and a test StatisticsService
        service = new StatisticsService();
    }
    
    public void testAddEndpointStatsByListener() throws JBIException, InterruptedException {
        // setup a latch to keep track of events being fired by a background thread
        final CountDownLatch latch = new CountDownLatch(1);
        container.addListener(new EndpointAdapter() {
            @Override
            public void internalEndpointRegistered(EndpointEvent event) {
                latch.countDown();
            } 
        });
        
        // initialize and start the StatisticsService
        service.init(container);
        service.start();
        
        // now register a new endpoint
        ServiceEndpoint endpoint = registerEndpoint();
        
        // ensure that the event has been fired by the event dispatch thread
        latch.await();

        // StatisticsService should know about the endpoint/component through listener callbacks
        assertNotNull(service.getComponentStats().get(COMPONENT));
        assertNotNull(service.getEndpointStats().get(EndpointSupport.getUniqueKey(endpoint)));
    }
    
    public void testAddEndpointStatsAtStartup() throws JBIException {
        // first register the endpoint
        ServiceEndpoint endpoint = registerEndpoint();
        
        // initialize and start the StatisticsService
        service.init(container);
        service.start();

        // StatisticsService should have learn about existing endpoints/components at startup
        assertNotNull(service.getComponentStats().get(COMPONENT));
        assertNotNull(service.getEndpointStats().get(EndpointSupport.getUniqueKey(endpoint)));
    }
    
    public void testInitByContainer() throws Exception {
        JBIContainer con = new JBIContainer();
        con.setEmbedded(true);
        BaseSystemService[] services = new BaseSystemService[] {new StatisticsService()};
        con.setServices(services);
        con.init();
        for (BaseSystemService srv : services) {
            assertTrue(srv.isInitialized());
        }
    }

    private ServiceEndpoint registerEndpoint() throws JBIException {
        TestComponent component = new TestComponent(SERVICE, ENDPOINT);
        container.activateComponent(new ActivationSpec(COMPONENT, component));
        return container.getEndpoint((ComponentContextImpl)component.getContext(), SERVICE, ENDPOINT);
    }

}
