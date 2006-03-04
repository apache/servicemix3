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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.component.Component;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Components
 *
 * @version $Revision$
 */
public class ComponentRegistry {
    
    private String containerName;
    private Map componentMap = new ConcurrentHashMap();
    private Map componentByNameMap = new ConcurrentHashMap();
    private Map idMap = new ConcurrentHashMap();
    private Map localIdMap = new ConcurrentHashMap();
    private Map loadBalancedComponentMap = new ConcurrentHashMap();
    private boolean runningStateInitialized = false;
    private Registry registry;
    
    
    protected ComponentRegistry(Registry reg){
        this.registry = reg;
    }
    /**
     * Register a Component
     * @param name
     * @param description
     * @param component
     * @param dc
     * @param binding
     * @param service
     * @return an associated ComponentConnector or null if it already exists
     */
    public LocalComponentConnector registerComponent(ComponentNameSpace name, 
                                                     String description, 
                                                     Component component,
                                                     boolean binding, 
                                                     boolean service) {
        LocalComponentConnector result = null;
        if (!componentMap.containsKey(component)) {
            result = new LocalComponentConnector(registry.getContainer(), name, description, component, binding, service);
            componentMap.put(component, result);
            localIdMap.put(name, result);
            addComponentConnector(result);
            componentByNameMap.put(name, component);
            addToLoadBalancedMap(name);
        }
        return result;
    }
    
    /**
     * start components
     * @throws JBIException
     */
    public void start() throws JBIException{
        if(!setInitialRunningStateFromStart()){
            for(Iterator i=getLocalComponentConnectors().iterator();i.hasNext();){
                LocalComponentConnector lcc=(LocalComponentConnector) i.next();
                lcc.getComponentMBean().doStart();
            }
        }
    }

    /**
     * stop components
     * @throws JBIException 
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException  {
        for (Iterator i = getLocalComponentConnectors().iterator();i.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) i.next();
            lcc.getComponentMBean().doStop();
        }
        runningStateInitialized = false;
    }
    
    /**
     * shutdown all Components
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        for (Iterator i = getLocalComponentConnectors().iterator();i.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) i.next();
            lcc.getComponentMBean().persistRunningState();
            lcc.getComponentMBean().doShutDown();
        }
    }
    
    

    
    /**
     * Deregister a Component
     * @param component
     * @return the deregistered component
     */
    public ComponentConnector deregisterComponent(Component component) {
        ComponentConnector result = (ComponentConnector) componentMap.remove(component);
        if (result != null) {
            removeComponentConnector(result.getComponentNameSpace());
            localIdMap.remove(result.getComponentNameSpace());
            componentByNameMap.remove(result.getComponentNameSpace());
            removeFromLoadBalancedMap(result.getComponentNameSpace());
        }
        return result;
    }
    
    /**
     * @param cns
     * @return the Component
     */
    public Component getComponent(ComponentNameSpace cns) {
        return (Component) componentByNameMap.get(cns);
    }
    
    /**
     * Get set of Components
     * @return a Set of Component objects
     */
    public Set getComponents(){
        return Collections.unmodifiableSet(componentMap.keySet());
    }
    
    /**
     * Get the ComponentConnector associated with the componet
     * @param component
     * @return the associated ComponentConnector
     */
    public LocalComponentConnector getComponentConnector(Component component) {
        return (LocalComponentConnector) componentMap.get(component);
    }
    
    /**
     * Get a registered ComponentConnector from it's id
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentConnector getComponentConnector(ComponentNameSpace id) {
        return (ComponentConnector) idMap.get(id);
    }
    
    /**
     * For distributed containers, get a ComponentConnector by round-robin
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentConnector getLoadBalancedComponentConnector(ComponentNameSpace id){
        return getComponentConnector(getLoadBalancedComponentName(id));
    }
    
    /**
     * Add a ComponentConnector
     * @param connector
     */
    public void addComponentConnector(ComponentConnector connector) {
        if (connector != null && !idMap.containsKey(connector.getComponentNameSpace())) {
            idMap.put(connector.getComponentNameSpace(), connector);
        }
    }
    
    /**
     * remove a ComponentConnector by id
     * @param id
     */
    public void removeComponentConnector(ComponentNameSpace id) {
        idMap.remove(id);
    }
    
    /**
     * Update a ComponentConnector
     * @param connector
     */
    public void updateConnector(ComponentConnector connector) {
        if (connector != null){
            idMap.put(connector.getComponentNameSpace(), connector);
        }
    }
    
    
    /**
     * Get a locally create ComponentConnector
     * @param id - id of the ComponentConnector
     * @return ComponentConnector or null if not found
     */
    public LocalComponentConnector getLocalComponentConnector(ComponentNameSpace id) {
       
        LocalComponentConnector result = (LocalComponentConnector) localIdMap.get(id);
        return result;
    }
    
    
    /**
     * Find existence of a Component locally registered to this Container
     * @param componentName
     * @return true if the Component exists
     */
    public boolean isLocalComponentRegistered(String componentName) {
        ComponentNameSpace cns = new ComponentNameSpace(containerName, componentName,
                componentName);
        return localIdMap.containsKey(cns);
    }
    
    /**
     * @return all local ComponentConnectors 
     */
    public Collection getLocalComponentConnectors() {
        return localIdMap.values();
    }
    
    /**
     * 
     * @return Collection of ComponentConnectors held by the registry
     */
    public Collection getComponentConnectors() {
        return idMap.values();
    }

    /**
     * @return Returns the containerName.
     */
    public String getContainerName() {
        return containerName;
    }
    /**
     * @param containerName The containerName to set.
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }
    
    private synchronized void addToLoadBalancedMap(ComponentNameSpace cns){
        String key = cns.getName();
        LinkedList list = (LinkedList)loadBalancedComponentMap.get(key);
        if (list == null){
            list = new LinkedList();
        }
        list.add(cns);
        
    }
    
    private synchronized void removeFromLoadBalancedMap(ComponentNameSpace cns){
        String key = cns.getName();
        LinkedList list = (LinkedList)loadBalancedComponentMap.get(key);
        if (list != null){
            list.remove(cns);
            if (list.isEmpty()){
                loadBalancedComponentMap.remove(key);
            }
        }
    }
    
    private synchronized ComponentNameSpace getLoadBalancedComponentName(ComponentNameSpace cns){
        ComponentNameSpace result = null;
        String key = cns.getName();
        LinkedList list = (LinkedList)loadBalancedComponentMap.get(key);
        if (list != null && !list.isEmpty()){
            result = (ComponentNameSpace) list.removeFirst();
            list.addLast(result);
        }
        return result;
    }
    
    private boolean setInitialRunningStateFromStart() throws JBIException{
        boolean result = !runningStateInitialized;
        if (!runningStateInitialized){
            runningStateInitialized = true;
            for (Iterator i = getLocalComponentConnectors().iterator();i.hasNext();) {
                LocalComponentConnector lcc = (LocalComponentConnector) i.next();
                if(!lcc.isPojo() && !registry.isContainerEmbedded()){
                    lcc.getComponentMBean().setInitialRunningState();
                }else {
                    lcc.getComponentMBean().doStart();
                }
            }
        }
        return result;
    }
}