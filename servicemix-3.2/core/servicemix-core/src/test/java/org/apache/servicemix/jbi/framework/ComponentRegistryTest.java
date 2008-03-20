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

import javax.jbi.component.Component;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.tck.SenderComponent;

public class ComponentRegistryTest extends TestCase {
    
    public void testRegister() throws Exception {
        JBIContainer container = new JBIContainer();
        container.setEmbedded(true);
        container.setUseMBeanServer(false);
        container.init();
        ComponentRegistry reg = new ComponentRegistry(container.getRegistry());
        Component component = new SenderComponent();
        ComponentMBeanImpl con = reg.registerComponent(
                              new ComponentNameSpace("container", "name"),
                              "description",
                              component,
                              false,
                              false,
                              null);
        assertNotNull(con);
        assertEquals(con, reg.getComponent(new ComponentNameSpace("container", "name")));
        assertEquals(component, reg.getComponent(new ComponentNameSpace("container", "name")).getComponent());
        assertEquals(1, reg.getComponents().size());
    }

}
