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
package org.apache.servicemix.jbi.framework;

import java.util.concurrent.atomic.AtomicInteger;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.messaging.DeliveryChannelImpl;
import org.apache.servicemix.jbi.resolver.URIResolver;
import org.apache.servicemix.tck.ReceiverComponent;

public class RegistryTest extends TestCase {

    public void testResolveEPR() throws Exception {
        JBIContainer container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
        
        EchoComponent component = new EchoComponent();
        component.setService(new QName("http://foo.bar.com", "myService"));
        container.activateComponent(component, "component");
        ServiceEndpoint ep = component.getContext().activateEndpoint(new QName("http://foo.bar.com", "myService"), "myEndpoint");
        DocumentFragment epr = ep.getAsReference(null);
        ServiceEndpoint ep2 = component.getContext().resolveEndpointReference(epr);
        assertSame(ep, ep2);
    }
    
    public void testResolveWSAEPR() throws Exception {
        JBIContainer container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
        
        EchoComponent component = new EchoComponent();
        component.setService(new QName("http://foo.bar.com", "myService"));
        container.activateComponent(component, "component");
        ServiceEndpoint ep = component.getContext().activateEndpoint(new QName("http://foo.bar.com", "myService"), "myEndpoint");
        DocumentFragment epr = URIResolver.createWSAEPR("endpoint:http://foo.bar.com/myService/myEndpoint");
        ServiceEndpoint ep2 = component.getContext().resolveEndpointReference(epr);
        assertSame(ep, ep2);
    }
    
    /**
     * Test canceling exchanges on the Registry will cancel pending exchanges in all the known components' DeliveryChannels
     */
    public void testCancelPendingExchanges() throws Exception {
        JBIContainer container = new JBIContainer();
        container.init();
        
        ActivationSpec spec = new ActivationSpec("component1", new ReceiverComponent());
        spec.setService(new QName("urn:test", "service1"));
        container.activateComponent(spec);
        
        container.start();
        
        final AtomicInteger canceled = new AtomicInteger();
        
        // injecting mock delivery channels to check if pending exchanges get canceled
        for (ComponentMBeanImpl mbean : container.getRegistry().getComponents()) {
            mbean.setDeliveryChannel(new DeliveryChannelImpl(mbean) {
                @Override
                public void cancelPendingExchanges() {
                    canceled.incrementAndGet();
                }
            });
        }
        
        // now let's try to cancel pending exchanges on the registry 
        container.getRegistry().cancelPendingExchanges();
        assertEquals("Should have canceled exchanges in all the delivery channels", 
                     container.getRegistry().getComponents().size(), canceled.get());
    }
    
}
