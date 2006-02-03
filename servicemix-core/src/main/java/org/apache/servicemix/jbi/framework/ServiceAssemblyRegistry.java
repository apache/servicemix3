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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jbi.component.Component;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.deployment.ServiceAssembly;
import org.apache.servicemix.jbi.deployment.ServiceUnit;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Components
 * 
 * @version $Revision$
 */
public class ServiceAssemblyRegistry {
    
    private static final Log log = LogFactory.getLog(ServiceAssemblyRegistry.class);
    private Map serviceAssembilies = new ConcurrentHashMap();
   

    private Registry registry;

    /**
     * Constructor
     * @param registry 
     */
    public ServiceAssemblyRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     *  initialize service assembilies to their persisted running state
     */
    public void start() {
        for (Iterator i = serviceAssembilies.values().iterator(); i.hasNext();){
            ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) i.next();
            salc.getCurrentState();
            if (salc.isStarted()){
                try{
                    start(salc);
                }catch(DeploymentException e){
                    log.error("Failed to start: " + salc);
                }
            }
        }
    }
    
    /**
     * Stop service assembilies 
     */
    public void stop(){
        for (Iterator i = serviceAssembilies.values().iterator(); i.hasNext();){
            ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) i.next();
            if (salc.isStarted()){
                try{
                    stop(salc);
                }catch(DeploymentException e){
                    log.error("Failed to start: " + salc);
                }
            }
        }
    }
    
    /**
     * shutDown the service
     */
    public void shutDown(){
        for (Iterator i = serviceAssembilies.values().iterator(); i.hasNext();){
            ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) i.next();
            if (!salc.isShutDown()){
                try{
                    shutDown(salc);
                }catch(DeploymentException e){
                    log.error("Failed to start: " + salc);
                }
            }
        }
    }

    /**
     * Register the Service Assembly
     * @param sa
     * @return true if successful
     * @throws DeploymentException 
     */
    public boolean register(ServiceAssembly sa) throws DeploymentException{
        boolean result=false;
        String saName=sa.getIdentification().getName();
        try{
            File stateFile=registry.getEnvironmentContext().getServiceAssemblyStateFile(saName);
            ServiceAssemblyLifeCycle salc=new ServiceAssemblyLifeCycle(sa,stateFile);
            init(salc);
            if(!serviceAssembilies.containsKey(saName)){
                serviceAssembilies.put(saName,salc);
                result=true;
            }
        }catch(IOException e){
            log.error("Failed to get state file for service assembly: "+saName);
            throw new DeploymentException(e);
        }
        return result;
    }
    
    /**
     * unregister a service assembly
     * 
     * @param sa
     * @return true if successful
     */
    public boolean unregister(ServiceAssembly sa){
        return unregister(sa.getIdentification().getName());
    }
    
    
    /**
     * unregister a service assembly
     * @param name
     * @return true if successful
     */
    public boolean unregister(String name){
        return serviceAssembilies.remove(name) != null;
    }
    
    /**
     * Get a named ServiceAssembly
     * @param name
     * @return the ServiceAssembly or null if it doesn't exist
     */
    public ServiceAssembly get(String name){
        ServiceAssemblyLifeCycle result = (ServiceAssemblyLifeCycle) serviceAssembilies.get(name);
        return result != null ? result.getServiceAssembly() : null;
    }
    
    
    /**
     * Start a Service Assembly
     * @param name
     * @return the status
     * @throws DeploymentException
     */
    public String start(String name) throws DeploymentException{
        String result=ServiceAssemblyLifeCycle.UNKNOWN;
        ServiceAssemblyLifeCycle salc=(ServiceAssemblyLifeCycle) serviceAssembilies.get(name);
        if(salc!=null){
            result=start(salc);
            salc.writeRunningState();
        }
        return result;
    }
    
    public String restore(String name) throws DeploymentException {
        String result = ServiceAssemblyLifeCycle.UNKNOWN;
        ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) serviceAssembilies.get(name);
        if (salc != null) {
        	result = salc.getRunningStateFromStore();
        	if (ServiceAssemblyLifeCycle.STARTED.equals(result)) {
        		start(salc);
        	} else if (ServiceAssemblyLifeCycle.SHUTDOWN.equals(result)) {
        		shutDown(salc);
        	}
        }
        return result;
    }
    
    void init(ServiceAssemblyLifeCycle salc) throws DeploymentException{
        if (salc != null) {
            ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                	String suName = sus[i].getIdentification().getName();
                    String componentName = sus[i].getTarget().getComponentName();
                    Component component = registry.getComponent(componentName);
                    if (component != null) {
                        ServiceUnitManager sum = component.getServiceUnitManager();
                        if (sum != null) {
                            try {
	                            File targetDir = registry.getEnvironmentContext().getServiceUnitDirectory(componentName, suName);
	                            sum.init(suName, targetDir.getAbsolutePath());
                            } catch (IOException e) {
                            	throw new DeploymentException(e);
                            }
                        }
                    }
                }
            }
        }
    }
    
    String start(ServiceAssemblyLifeCycle salc) throws DeploymentException{
        String result = ServiceAssemblyLifeCycle.UNKNOWN;
        if (salc != null) {
            ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                	String suName = sus[i].getIdentification().getName();
                    String componentName = sus[i].getTarget().getComponentName();
                    Component component = registry.getComponent(componentName);
                    if (component != null) {
                        ServiceUnitManager sum = component.getServiceUnitManager();
                        if (sum != null) {
                            sum.start(suName);
                        }
                    }
                }
            }
            salc.start();
            result = salc.getCurrentState();
            log.info("Started Service Assembly: " + salc.getName());
        }
        return result;
    }
    
    /**
     * Stops the service assembly and puts it in STOPPED state.
     * @param name 
     * 
     * @return Result/Status of this operation.
     * @throws DeploymentException 
     */
    public String stop(String name) throws DeploymentException{
        String result=ServiceAssemblyLifeCycle.UNKNOWN;
        ServiceAssemblyLifeCycle salc=(ServiceAssemblyLifeCycle) serviceAssembilies.get(name);
        if(salc!=null){
            result=stop(salc);
            salc.writeRunningState();
        }
        return result;
    }
    
    String stop(ServiceAssemblyLifeCycle salc) throws DeploymentException {
        String result = ServiceAssemblyLifeCycle.UNKNOWN;
        if (salc != null) {
            ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                    String componentName = sus[i].getTarget().getComponentName();
                    Component component = registry.getComponent(componentName);
                    if (component != null) {
                        ServiceUnitManager sum = component.getServiceUnitManager();
                        if (sum != null) {
                            sum.stop(sus[i].getIdentification().getName());
                        }
                    }
                }
            }
            salc.stop();
            result = salc.getCurrentState();
            log.info("Stopped Service Assembly: " + salc.getName());
        }
        return result;
    }
    
    /**
    * Shutdown the service assembly and puts it in SHUTDOWN state.
    * @param name 
    * 
    * @return Result/Status of this operation.
    * @throws DeploymentException 
    */
    public String shutDown(String name) throws DeploymentException{
        String result=ServiceAssemblyLifeCycle.UNKNOWN;
        ServiceAssemblyLifeCycle salc=(ServiceAssemblyLifeCycle) serviceAssembilies.get(name);
        if(salc!=null){
            result=shutDown(salc);
            salc.writeRunningState();
        }
        return result;
    }
    
    String shutDown(ServiceAssemblyLifeCycle salc) throws DeploymentException {
        String result = ServiceAssemblyLifeCycle.UNKNOWN;
        if (salc != null) {
            ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                    String componentName = sus[i].getTarget().getComponentName();
                    Component component = registry.getComponent(componentName);
                    if (component != null) {
                        ServiceUnitManager sum = component.getServiceUnitManager();
                        if (sum != null) {
                            sum.shutDown(sus[i].getIdentification().getName());
                        }
                    }
                }
            }
            salc.shutDown();
            result = salc.getCurrentState();
            log.info("Shutdown Service Assembly: " + salc.getName());
        }
        return result;
    }
   
   
   /**
    * Get the current state of the named service assembly
    * @param name
    * @return the state
    */
   public String getState(String name){
       String result = ServiceAssemblyLifeCycle.SHUTDOWN;
       ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) serviceAssembilies.get(name);
       if (salc != null) {
           result = salc.getCurrentState();
       }
       return result;
   }
   
   /**
    * Returns a list of Service Units that are currently deployed to the given component.
    * 
    * @param componentName name of the component.
    * @return List of deployed ASA Ids.
    */
   public String[] getDeployedServiceUnitList(String componentName) {
       String[] result = null;
       // iterate through the service assembilies
       List tmpList = new ArrayList();
       for (Iterator iter = serviceAssembilies.values().iterator();iter.hasNext();) {
           ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) iter.next();
           ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
           if (sus != null) {
               for (int i = 0;i < sus.length;i++) {
                   if (sus[i].getTarget().getComponentName().equals(componentName)) {
                       tmpList.add(sus[i].getIdentification().getName());
                   }
               }
           }
       }
       result = new String[tmpList.size()];
       tmpList.toArray(result);
       return result;
   }

   /**
    * Returns a list of Service Assemblies deployed to the JBI enviroment.
    * 
    * @return list of Service Assembly Name's.
    */
   public String[] getDeployedServiceAssemblies()  {
       String[] result = null;
       Set keys = serviceAssembilies.keySet();
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
       Set tmpList = new HashSet();
       for (Iterator iter = serviceAssembilies.values().iterator();iter.hasNext();) {
           ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) iter.next();
           ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
           if (sus != null) {
               for (int i = 0;i < sus.length;i++) {
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
   public String[] getComponentsForDeployedServiceAssembly(String saName)  {
       String[] result = null;
       Set tmpList = new HashSet();
       ServiceAssembly sa = get(saName);
       if (sa != null) {
           ServiceUnit[] sus = sa.getServiceUnits();
           if (sus != null) {
               for (int i = 0;i < sus.length;i++) {
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
       for (Iterator iter = serviceAssembilies.values().iterator();iter.hasNext();) {
           ServiceAssemblyLifeCycle salc = (ServiceAssemblyLifeCycle) iter.next();
           ServiceUnit[] sus = salc.getServiceAssembly().getServiceUnits();
           if (sus != null) {
               for (int i = 0;i < sus.length;i++) {
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

    
    
    
    
}
