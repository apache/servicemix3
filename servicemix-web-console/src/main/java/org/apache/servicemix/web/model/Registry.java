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

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectName;

import org.apache.servicemix.jbi.framework.ComponentMBean;
import org.apache.servicemix.jbi.framework.EndpointMBean;
import org.apache.servicemix.jbi.framework.RegistryMBean;
import org.apache.servicemix.jbi.framework.ServiceAssemblyMBean;
import org.apache.servicemix.jbi.framework.ServiceUnitMBean;
import org.apache.servicemix.jbi.framework.SharedLibraryMBean;

public class Registry {

    private final RegistryMBean mbean;
    private final ProxyManager proxyManager;
    
    public Registry(RegistryMBean mbean, ProxyManager proxyManager) {
        this.mbean = mbean;
        this.proxyManager = proxyManager;
    }

    public List<Component> getComponents() {
        List<Component> components = new ArrayList<Component>();
        ObjectName[] names = mbean.getComponentNames();
        for (int i = 0; i < names.length; i++) {
            ComponentMBean mbean = proxyManager.getProxy(names[i], ComponentMBean.class);
            if (!"#SubscriptionManager#".equals(mbean.getName())) {
                components.add(new Component(this, mbean, names[i]));
            }
        }
        return components;
    }
    
    public Component getComponent(String name) {
        for (Component component : getComponents()) {
            if (component.getName().equals(name)) {
                return component;
            }
        }
        return null;
    }
    
    public List<Component> getComponent(SharedLibrary library) {
        List<Component> components = new ArrayList<Component>();
        for (Component component : getComponents()) {
            // TODO
        }
        return null;
    }

    public List<Endpoint> getEndpoints() {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        ObjectName[] names = mbean.getEndpointNames();
        for (int i = 0; i < names.length; i++) {
            EndpointMBean mbean = proxyManager.getProxy(names[i], EndpointMBean.class);
            endpoints.add(new Endpoint(this, mbean, names[i]));
        }
        return endpoints;
    }

    public Endpoint getEndpoint(String name) {
        for (Endpoint endpoint : getEndpoints()) {
            if (endpoint.getName().equals(name)) {
                return endpoint;
            }
        }
        return null;
    }

    public List<Endpoint> getEndpoints(Component component) throws Exception {
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        for (Endpoint endpoint : getEndpoints()) {
            if (endpoint.getComponent().equals(component)) {
                endpoints.add(endpoint);
            }
        }
        return endpoints;
    }

    public List<ServiceUnit> getServiceUnits() {
        List<ServiceUnit> serviceUnits = new ArrayList<ServiceUnit>();
        ObjectName[] names = mbean.getServiceUnitNames();
        for (int i = 0; i < names.length; i++) {
            ServiceUnitMBean mbean = proxyManager.getProxy(names[i], ServiceUnitMBean.class);
            serviceUnits.add(new ServiceUnit(this, mbean, names[i]));
        }
        return serviceUnits;
    }
    
    public ServiceUnit getServiceUnit(String name) {
        for (ServiceUnit serviceUnit : getServiceUnits()) {
            if (serviceUnit.getName().equals(name)) {
                return serviceUnit;
            }
        }
        return null;
    }
    
    public List<ServiceUnit> getServiceUnits(Component component) {
        List<ServiceUnit> serviceUnits = new ArrayList<ServiceUnit>();
        for (ServiceUnit serviceUnit : getServiceUnits()) {
            if (serviceUnit.getComponent().equals(component)) {
                serviceUnits.add(serviceUnit);
            }
        }
        return serviceUnits;
    }

    public List<ServiceUnit> getServiceUnits(ServiceAssembly assembly) {
        List<ServiceUnit> serviceUnits = new ArrayList<ServiceUnit>();
        for (ServiceUnit serviceUnit : getServiceUnits()) {
            if (serviceUnit.getServiceAssembly().equals(assembly)) {
                serviceUnits.add(serviceUnit);
            }
        }
        return serviceUnits;
    }
    
    
    public List<ServiceAssembly> getServiceAssemblies() {
        List<ServiceAssembly> serviceAssemblies = new ArrayList<ServiceAssembly>();
        ObjectName[] names = mbean.getServiceAssemblyNames();
        for (int i = 0; i < names.length; i++) {
            ServiceAssemblyMBean mbean = proxyManager.getProxy(names[i], ServiceAssemblyMBean.class);
            serviceAssemblies.add(new ServiceAssembly(this, mbean, names[i]));
        }
        return serviceAssemblies;
    }

    public ServiceAssembly getServiceAssembly(String name) {
        List<ServiceAssembly> serviceAssemblies = getServiceAssemblies();
        for (ServiceAssembly serviceAssembly : serviceAssemblies) {
            if (serviceAssembly.getName().equals(name)) {
                return serviceAssembly;
            }
        }
        return null;
    }
    
    public List<SharedLibrary> getSharedLibraries() {
        List<SharedLibrary> sharedLibraries = new ArrayList<SharedLibrary>();
        ObjectName[] names = mbean.getSharedLibraryNames();
        for (int i = 0; i < names.length; i++) {
            SharedLibraryMBean mbean = proxyManager.getProxy(names[i], SharedLibraryMBean.class);
            sharedLibraries.add(new SharedLibrary(this, mbean, names[i]));
        }
        return sharedLibraries;
    }

    public SharedLibrary getSharedLibrary(String name) {
        for (SharedLibrary sharedLibrary : getSharedLibraries()) {
            if (sharedLibrary.getName().equals(name)) {
                return sharedLibrary;
            }
        }
        return null;
    }

}
