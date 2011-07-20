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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.servicemix.jbi.container.ServiceAssemblyEnvironment;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.deployment.ServiceUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for Components
 * 
 * @version $Revision$
 */
public class ServiceAssemblyRegistry {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ServiceAssemblyRegistry.class);

    private Map<String, ServiceAssemblyLifeCycle> serviceAssemblies = new ConcurrentHashMap<String, ServiceAssemblyLifeCycle>();

    private Registry registry;

    /**
     * Constructor
     * @param registry 
     */
    public ServiceAssemblyRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     *  Start all registered service assemblies
     */
    public void start() {
    }

    /**
     * Stop service assembilies 
     */
    public void stop() {
    }

    /**
     * shutDown the service
     */
    public void shutDown() {
    }

    public ServiceAssemblyLifeCycle register(ServiceAssembly sa, String[] suKeys, 
                                             ServiceAssemblyEnvironment env) throws DeploymentException {
        String saName = sa.getIdentification().getName();
        if (!serviceAssemblies.containsKey(saName)) {
            ServiceAssemblyLifeCycle salc = new ServiceAssemblyLifeCycle(sa, env, registry);
            List<ServiceUnitLifeCycle> sus = new ArrayList<ServiceUnitLifeCycle>();
            for (int i = 0; i < suKeys.length; i++) {
                sus.add(registry.getServiceUnit(suKeys[i]));
            }
            salc.setServiceUnits((ServiceUnitLifeCycle[]) sus.toArray(new ServiceUnitLifeCycle[sus.size()]));
            serviceAssemblies.put(saName, salc);
            try {
                ObjectName objectName = registry.getContainer().getManagementContext().createObjectName(salc);
                registry.getContainer().getManagementContext().registerMBean(objectName, salc, ServiceAssemblyMBean.class);
            } catch (JMException e) {
                LOGGER.error("Could not register MBean for service assembly", e);
            }
            return salc;
        }
        return null;
    }

    public ServiceAssemblyLifeCycle register(ServiceAssembly sa, ServiceAssemblyEnvironment env) throws DeploymentException {
        List<String> sus = new ArrayList<String>();
        if (sa.getServiceUnits() != null) {
            for (int i = 0; i < sa.getServiceUnits().length; i++) {
                String suKey = registry.registerServiceUnit(sa.getServiceUnits()[i], sa.getIdentification().getName(), env
                                .getServiceUnitDirectory(sa.getServiceUnits()[i].getTarget().getComponentName(), sa.getServiceUnits()[i]
                                                .getIdentification().getName()));
                sus.add(suKey);
            }
        }
        return register(sa, sus.toArray(new String[sus.size()]), env);
    }

    /**
     * unregister a service assembly
     * @param name
     * @return true if successful
     */
    public boolean unregister(String name) {
        ServiceAssemblyLifeCycle salc = serviceAssemblies.remove(name);
        if (salc != null) {
            try {
                ServiceUnitLifeCycle[] sus = salc.getDeployedSUs();
                if (sus != null) {
                    for (int i = 0; i < sus.length; i++) {
                        registry.unregisterServiceUnit(sus[i].getKey());
                    }
                }
                registry.getContainer().getManagementContext().unregisterMBean(salc);
            } catch (JBIException e) {
                LOGGER.error("Unable to unregister MBean for service assembly", e);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a named ServiceAssembly
     * @param saName
     * @return the ServiceAssembly or null if it doesn't exist
     */
    public ServiceAssemblyLifeCycle getServiceAssembly(String saName) {
        return serviceAssemblies.get(saName);
    }

    /**
     * Returns a list of Service Assemblies deployed to the JBI enviroment.
     * 
     * @return list of Service Assembly Name's.
     */
    public String[] getDeployedServiceAssemblies() {
        String[] result = null;
        Set<String> keys = serviceAssemblies.keySet();
        result = new String[keys.size()];
        keys.toArray(result);
        return result;
    }

    /**
     * Returns a list of Service Assemblies that contain SUs for the given component.
     * 
     * @param componentName name of the component.
     * @return list of Service Assembly names.
     */
    public String[] getDeployedServiceAssembliesForComponent(String componentName) {
        String[] result = null;
        // iterate through the service assembilies
        Set<String> tmpList = new HashSet<String>();
        for (ServiceAssemblyLifeCycle salc : serviceAssemblies.values()) {
            ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0; i < sus.length; i++) {
                    if (sus[i].getTarget().getComponentName().equals(componentName)) {
                        tmpList.add(salc.getServiceAssembly().getIdentification().getName());
                    }
                }
            }
        }
        result = new String[tmpList.size()];
        tmpList.toArray(result);
        return result;
    }

    /**
     * Returns a list of components(to which SUs are targeted for) in a Service Assembly.
     * 
     * @param saName name of the service assembly.
     * @return list of component names.
     */
    public String[] getComponentsForDeployedServiceAssembly(String saName) {
        String[] result = null;
        Set<String> tmpList = new HashSet<String>();
        ServiceAssemblyLifeCycle sa = getServiceAssembly(saName);
        if (sa != null) {
            ServiceUnit[] sus = sa.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0; i < sus.length; i++) {
                    tmpList.add(sus[i].getTarget().getComponentName());
                }
            }
        }
        result = new String[tmpList.size()];
        tmpList.toArray(result);
        return result;
    }

    /**
     * Returns a boolean value indicating whether the SU is currently deployed.
     * 
     * @param componentName - name of component.
     * @param suName - name of the Service Unit.
     * @return boolean value indicating whether the SU is currently deployed.
     */
    public boolean isDeployedServiceUnit(String componentName, String suName) {
        boolean result = false;
        for (ServiceAssemblyLifeCycle salc : serviceAssemblies.values()) {
            ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0; i < sus.length; i++) {
                    if (sus[i].getTarget().getComponentName().equals(componentName)
                                    && sus[i].getIdentification().getName().equals(suName)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of service assemblies.
     * 
     * @return list of service assemblies
     */
    public Collection<ServiceAssemblyLifeCycle> getServiceAssemblies() {
        return serviceAssemblies.values();
    }

}
