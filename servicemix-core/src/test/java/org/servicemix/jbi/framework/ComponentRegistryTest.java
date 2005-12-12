/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.jbi.framework;

import org.servicemix.tck.SenderComponent;

import javax.jbi.component.Component;

import junit.framework.TestCase;

public class ComponentRegistryTest extends TestCase {
    
    public void testContainerName() throws Exception {
        ComponentRegistry reg = new ComponentRegistry(null);
        reg.setContainerName("containerName");
        assertEquals("containerName", reg.getContainerName());
    }
    
    public void testRegister() throws Exception {
        ComponentRegistry reg = new ComponentRegistry(null);
        Component component = new SenderComponent();
        LocalComponentConnector con = reg.registerComponent(
                              new ComponentNameSpace("container", "name", "id"),
                              "description",
                              component,
                              null,
                              false,
                              false);
        assertNotNull(con);
        assertEquals(con, reg.getComponentConnector(component));
        assertEquals(con, reg.getComponentConnector(new ComponentNameSpace("container", null, "id")));
        assertEquals(component, reg.getComponent(new ComponentNameSpace("container", null, "id")));
        assertEquals(1, reg.getComponentConnectors().size());
        assertEquals(1, reg.getLocalComponentConnectors().size());
        assertEquals(1, reg.getComponents().size());
    }

}
