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
package org.apache.servicemix.jbi.servicedesc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.framework.ComponentNameSpace;

public class InternalEndpointTest extends TestCase {

    public void testSerializeDeserialize() throws Exception {
        ComponentNameSpace cns = new ComponentNameSpace("myContainer", "myName");
        InternalEndpoint e = new InternalEndpoint(cns, "myEndpoint", new QName("myService"));
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(e);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object out = ois.readObject();
        
        assertNotNull(out);
        assertTrue(out instanceof InternalEndpoint);
        InternalEndpoint outE = (InternalEndpoint) out; 
        assertNotNull(outE.getComponentNameSpace());
        assertNotNull(outE.getServiceName());
        assertNotNull(outE.getEndpointName());
    }
    
    public void testEquals() throws Exception {
        ComponentNameSpace cns = new ComponentNameSpace("myContainer", "myName");
        InternalEndpoint e1 = new InternalEndpoint(cns, "myEndpoint1", new QName("myService"));
        InternalEndpoint e2 = new InternalEndpoint(cns, "myEndpoint2", new QName("myService"));
        assertFalse(e1.equals(e2));
        e2 = new InternalEndpoint(cns, "myEndpoint", new QName("myService2"));
        assertFalse(e1.equals(e2));
        ComponentNameSpace cns2 = new ComponentNameSpace("myContainer2", "myId2");
        e2 = new InternalEndpoint(cns2, "myEndpoint1", new QName("myService"));
        assertTrue(e1.equals(e2));
        cns2 = new ComponentNameSpace("myContainer", "myName");
        e2 = new InternalEndpoint(cns2, "myEndpoint1", new QName("myService"));
        assertTrue(e1.equals(e2));
    }

}
