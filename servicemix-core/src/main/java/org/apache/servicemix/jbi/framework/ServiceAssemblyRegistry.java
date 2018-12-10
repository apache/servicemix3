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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.JMException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ServiceAssemblyEnvironment;
import org.apache.servicemix.schemas.deployment.ServiceAssembly;
import org.apache.servicemix.schemas.deployment.ServiceUnit;

/**
 * Registry for Components
 * 
 * @version $Revision$
 */
public class ServiceAssemblyRegistry {
    
    private static final Log log = LogFactory.getLog(ServiceAssemblyRegistry.class);
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
    public void stop(){
    }
    
    /**
     * shutDown the service
     */
    public void shutDown(){
    }

    public ServiceAssemblyLifeCycle register(ServiceAssembly sa, 
                                             String[] suKeys,
                                             ServiceAssemblyEnvironment env) throws DeploymentException {
        String saName = sa.getIdentification().getName();
        if (!serviceAssemblies.containsKey(saName)) {
            ServiceAssemblyLifeCycle salc = new ServiceAssemblyLifeCycle(sa, env, registry);
            List<ServiceUnitLifeCycle> sus = new ArrayList<ServiceUnitLifeCycle>();
            for (int i = 0; i < suKeys.length; i++) {
                sus.add(registry.getServiceUnit(suKeys[i]));
            }
            salc.setServiceUnits(sus.toArray(new ServiceUnitLifeCycle[sus.size()]));
            serviceAssemblies.put(saName, salc);
            try {
                ObjectName objectName = registry.getContainer().getManagementContext().createObjectName(salc);
                registry.getContainer().getManagementContext().registerMBean(objectName, salc, ServiceAssemblyMBean.class);
            } catch (JMException e) {
                log.error("Could not register MBean for service assembly", e);
            }
            return salc;
        }
        return null;
    }
    
    public ServiceAssemblyLifeCycle register(ServiceAssembly sa, ServiceAssemblyEnvironment env) throws DeploymentException {
        List<String> sus = new ArrayList<String>();
        if (sa.getServiceUnit() != null) {
        	for (ServiceUnit su : sa.getServiceUnit()) {
                String suKey = registry.registerServiceUnit(
                                        su,
                                        sa.getIdentification().getName(),
                                        env.getServiceUnitDirectory(su.getTarget().getComponentName(),
                                                                    su.getIdentification().getName()));
                sus.add(suKey);
            }
        }
        return register(sa,
                        sus.toArray(new String[sus.size()]),
                        env);
    }
    
    /**
     * unregister a service assembly
     * @param name
     * @return true if successful
     */
    public boolean unregister(String name) {
        ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) serviceAssemblies.remove(name);
        if (salc != null) {
            try {
                registry.getContainer().getManagementContext().unregisterMBean(salc);
            } catch (JBIException e) {
                log.error("Unable to unregister MBean for service assembly", e);
            }
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Get a named ServiceAssembly
     * @param name
     * @return the ServiceAssembly or null if it doesn't exist
     */
    public ServiceAssemblyLifeCycle getServiceAssembly(String saName) {
        return (ServiceAssemblyLifeCycle) serviceAssemblies.get(saName);
    }
    
   /**
    * Returns a list of Service Assemblies deployed to the JBI enviroment.
    * 
    * @return list of Service Assembly Name's.
    */
   public String[] getDeployedServiceAssemblies()  {
       String[] result = null;
       Set keys = serviceAssemblies.keySet();
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
    	   for (ServiceUnit su : salc.getServiceAssembly().getServiceUnit()) {
               if (su.getTarget().getComponentName().equals(componentName)) {
                   tmpList.add(salc.getServiceAssembly().getIdentification().getName());
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
   public String[] getComponentsForDeployedServiceAssembly(String saName)  {
       Set<String> tmpList = new HashSet<String>();
       ServiceAssemblyLifeCycle sa = getServiceAssembly(saName);
       if (sa != null) {
    	   for (ServiceUnit su : sa.getServiceAssembly().getServiceUnit()) {
               tmpList.add(su.getTarget().getComponentName());
           }
       }
       return tmpList.toArray(new String[tmpList.size()]);
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
    	   for (ServiceUnit su : salc.getServiceAssembly().getServiceUnit()) {
               if (su.getTarget().getComponentName().equals(componentName) &&
                   su.getIdentification().getName().equals(suName)) {
                   result = true;
                   break;
               }
           }
       }
       return result;
   }
   
}
