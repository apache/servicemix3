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
package org.apache.servicemix.jbi.framework;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

public class ComponentPacketTest extends TestCase {
    
    public void testRegisterTwoEndpoints() throws Exception {
        /*
        ComponentPacket packet = new ComponentPacket();
        ComponentNameSpace cns = new ComponentNameSpace("container", "component", null);
        ServiceEndpoint ep1 = new InternalEndpoint(cns, "endpoint", new QName("urn:foo", "service1"));
        ServiceEndpoint ep2 = new InternalEndpoint(cns, "endpoint", new QName("urn:foo", "service2"));
        packet.addActiveEndpoint(ep1);
        packet.addActiveEndpoint(ep2);
        assertEquals(2, packet.getActiveEndpoints().size());
        */
    }

}
