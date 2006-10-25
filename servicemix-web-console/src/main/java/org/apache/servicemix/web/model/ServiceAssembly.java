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

import org.apache.servicemix.jbi.framework.ServiceAssemblyMBean;

public class ServiceAssembly {

    private final Registry registry;
    private final ServiceAssemblyMBean mbean;
    private final ObjectName objectName;
    
    public ServiceAssembly(Registry registry, ServiceAssemblyMBean mbean, ObjectName objectName) {
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
    
    public String getDescription() {
        return mbean.getDescription();
    }
    
    public String getStatus() {
        return mbean.getCurrentState();
    }
    
    public List<ServiceUnit> getServiceUnits() {
        return registry.getServiceUnits(this);
    }

    public boolean equals(Object o) {
        if (o instanceof ServiceAssembly) {
            return ((ServiceAssembly) o).objectName.equals(objectName);
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return objectName.hashCode();
    }

}
