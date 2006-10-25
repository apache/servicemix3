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
package org.apache.servicemix.web.model;

import java.util.List;

import javax.management.ObjectName;

import org.apache.servicemix.jbi.framework.ComponentMBean;

public class Component {
    
    private final Registry registry;
    private final ComponentMBean mbean;
    private final ObjectName objectName;

    public Component(Registry registry, ComponentMBean mbean, ObjectName objectName) {
        this.registry = registry;
        this.mbean = mbean;
        this.objectName = objectName;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public String getName() {
        return mbean.getName();
    }

    public String getType() {
        return mbean.getComponentType();
    }
    
    public String getStatus() throws Exception {
        return mbean.getCurrentState();
    }
    
    public List<ServiceUnit> getServiceUnits() throws Exception {
        return registry.getServiceUnits(this);
    }
    
    public List<Endpoint> getEndpoints() throws Exception {
        return registry.getEndpoints(this);
    }
    
    public boolean equals(Object o) {
        if (o instanceof Component) {
            return ((Component) o).objectName.equals(objectName);
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return objectName.hashCode();
    }

}
