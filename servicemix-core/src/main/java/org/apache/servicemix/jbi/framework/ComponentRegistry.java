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
import java.util.Iterator;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Component;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for Components
 *
 * @version $Revision$
 */
public class ComponentRegistry {
    
    private Map idMap = new ConcurrentHashMap();
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
    public ComponentMBeanImpl registerComponent(ComponentNameSpace name, 
                                                String description, 
                                                Component component,
                                                boolean binding, 
                                                boolean service) {
        ComponentMBeanImpl result = null;
        if (!idMap.containsKey(name)) {
            result = new ComponentMBeanImpl(registry.getContainer(), name, description, component, binding, service);
            idMap.put(name, result);
        }
        return result;
    }
    
    /**
     * start components
     * @throws JBIException
     */
    public void start() throws JBIException{
        if (!setInitialRunningStateFromStart()) {
            for(Iterator i = getComponents().iterator(); i.hasNext();) {
                ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
                lcc.doStart();
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
        for (Iterator i = getComponents().iterator();i.hasNext();) {
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
            lcc.doStop();
        }
        runningStateInitialized = false;
    }
    
    /**
     * shutdown all Components
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        for (Iterator i = getComponents().iterator();i.hasNext();) {
            ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
            lcc.persistRunningState();
            lcc.doShutDown();
        }
    }
    
    

    
    /**
     * Deregister a Component
     * @param component
     * @return the deregistered component
     */
    public void deregisterComponent(ComponentMBeanImpl component) {
        idMap.remove(component.getComponentNameSpace());
    }
    
    /**
     * Get a registered ComponentConnector from it's id
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentMBeanImpl getComponent(ComponentNameSpace id) {
        return (ComponentMBeanImpl) idMap.get(id);
    }
    
    /**
     * 
     * @return Collection of ComponentConnectors held by the registry
     */
    public Collection getComponents() {
        return idMap.values();
    }

    private boolean setInitialRunningStateFromStart() throws JBIException{
        boolean result = !runningStateInitialized;
        if (!runningStateInitialized){
            runningStateInitialized = true;
            for (Iterator i = getComponents().iterator();i.hasNext();) {
                ComponentMBeanImpl lcc = (ComponentMBeanImpl) i.next();
                if(!lcc.isPojo() && !registry.isContainerEmbedded()){
                    lcc.setInitialRunningState();
                }else {
                    lcc.doStart();
                }
            }
        }
        return result;
    }
}