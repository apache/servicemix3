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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

public class ComponentNameSpaceTest extends TestCase {

    /*
     * Test method for 'org.apache.servicemix.jbi.framework.ComponentNameSpace.getComponentId()'
     */
    public void testAccessors() {
        ComponentNameSpace cns = new ComponentNameSpace();
        assertNull(cns.getContainerName());
        assertNull(cns.getName());
        cns.setContainerName("container");
        assertEquals("container", cns.getContainerName());
        cns.setName("name");
        assertEquals("name", cns.getName());
    }

    /*
     * Test method for 'org.apache.servicemix.jbi.framework.ComponentNameSpace.equals(Object)'
     * Test method for 'org.apache.servicemix.jbi.framework.ComponentNameSpace.hashCode()'
     */
    public void testHashCodeEqualsObject() {
        ComponentNameSpace cns1 = new ComponentNameSpace("container", "name");
        ComponentNameSpace cns2 = new ComponentNameSpace("container", "name");
        assertTrue(cns1.equals(cns2));
        assertTrue(cns1.hashCode() == cns2.hashCode());

        ComponentNameSpace cns3 = new ComponentNameSpace("container1", "name");
        ComponentNameSpace cns4 = new ComponentNameSpace("container2", "name");
        assertFalse(cns3.equals(cns4));
        assertFalse(cns3.hashCode() == cns4.hashCode());

        ComponentNameSpace cns5 = new ComponentNameSpace("container", "name1");
        ComponentNameSpace cns6 = new ComponentNameSpace("container", "name2");
        assertFalse(cns5.equals(cns6));
        assertFalse(cns5.hashCode() == cns6.hashCode());
    }

    /*
     * Test method for 'org.apache.servicemix.jbi.framework.ComponentNameSpace.writeExternal(ObjectOutput)'
     * Test method for 'org.apache.servicemix.jbi.framework.ComponentNameSpace.readExternal(ObjectInput)'
     */
    public void testSerialize() throws Exception {
        ComponentNameSpace cns = new ComponentNameSpace("container", "name");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cns);
        oos.close();
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object out = ois.readObject();
        
        assertNotNull(out);
        assertTrue(out instanceof ComponentNameSpace);
        ComponentNameSpace cnsOut = (ComponentNameSpace) out;
        assertEquals(cns, cnsOut);
        assertEquals(cns.getName(), cnsOut.getName());
    }

    /*
     * Test method for 'org.apache.servicemix.jbi.framework.ComponentNameSpace.copy()'
     */
    public void testCopy() {
        ComponentNameSpace cns1 = new ComponentNameSpace("container", "name");
        ComponentNameSpace cns2 = cns1.copy();
        assertEquals(cns1, cns1);
        assertEquals(cns1.getName(), cns2.getName());
    }

}
