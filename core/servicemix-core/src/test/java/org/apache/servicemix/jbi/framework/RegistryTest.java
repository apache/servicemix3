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

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.resolver.URIResolver;
import org.w3c.dom.DocumentFragment;

public class RegistryTest extends TestCase {

    public void testResolveEPR() throws Exception {
        JBIContainer container = new JBIContainer();
        container.setEmbedded(true);
        container.init();
        container.start();
        
        EchoComponent component = new EchoComponent();
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
        container.activateComponent(component, "component");
        ServiceEndpoint ep = component.getContext().activateEndpoint(new QName("http://foo.bar.com", "myService"), "myEndpoint");
        DocumentFragment epr = URIResolver.createWSAEPR("endpoint:http://foo.bar.com/myService/myEndpoint");
        ServiceEndpoint ep2 = component.getContext().resolveEndpointReference(epr);
        assertSame(ep, ep2);
    }
    
}
