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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for Components
 *
 * @version $Revision$
 */
public class ComponentRegistry {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ComponentRegistry.class);
    private final Map<ComponentNameSpace, ComponentMBeanImpl> idMap = new LinkedHashMap<ComponentNameSpace, ComponentMBeanImpl>();
    private boolean runningStateInitialized;
    private Registry registry;

    protected ComponentRegistry(Registry reg) {
        this.registry = reg;
    }

    /**
     * Register a Component
     *
     * @param name
     * @param description
     * @param component
     * @param binding
     * @param sharedLibraries
     * @param service
     * @return an associated ComponentConnector or null if it already exists
     */
    public synchronized ComponentMBeanImpl registerComponent(
                    ComponentNameSpace name, 
                    String description, 
                    Component component,
                    boolean binding, 
                    boolean service,
                    String[] sharedLibraries) {
        ComponentMBeanImpl result = null;
        if (!idMap.containsKey(name)) {
            result = new ComponentMBeanImpl(registry.getContainer(), name, description, component, binding, service, sharedLibraries);
            idMap.put(name, result);
        }
        return result;
    }
    
    /**
     * Start components
     *
     * @throws JBIException
     */
    public synchronized void start() throws JBIException {
        if (!setInitialRunningStateFromStart()) {
            for (ComponentMBeanImpl lcc : getComponents()) {
                try {
                    lcc.start();
                } catch (Exception e) {
                    LOGGER.error("Error during component startup.", e);
                }
            }
        }
    }

    /**
     * Stop components
     *
     * @throws JBIException
     */
    public synchronized void stop() throws JBIException  {
        for (ComponentMBeanImpl lcc : getReverseComponents()) {
            try {
                lcc.doStop();
            } catch (Exception e) {
                LOGGER.error("Error during component stop.", e);
            }
        }
        runningStateInitialized = false;
    }
    
    /**
     * Shutdown all Components
     * 
     * @throws JBIException
     */
    public synchronized void shutDown() throws JBIException {
        for (ComponentMBeanImpl lcc : getReverseComponents()) {
            lcc.persistRunningState();
            try {
                lcc.doShutDown();
            } catch (Exception e) {
                LOGGER.error("Error during component shutdown.", e);
            }
        }
    }
    
    private Collection<ComponentMBeanImpl> getReverseComponents() {
        synchronized (idMap) {
            List<ComponentMBeanImpl> l = new ArrayList<ComponentMBeanImpl>(idMap.values());
            Collections.reverse(l);
            return l;
        }
    }

    
    /**
     * Deregister a Component
     *
     * @param component
     */
    public synchronized void deregisterComponent(ComponentMBeanImpl component) {
        idMap.remove(component.getComponentNameSpace());
    }
    
    /**
     * Get a registered ComponentConnector from it's id
     *
     * @param id
     * @return the ComponentConnector or null
     */
    public ComponentMBeanImpl getComponent(ComponentNameSpace id) {
        synchronized (idMap) {
            return idMap.get(id);
        }
    }
    
    /**
     * 
     * @return Collection of ComponentConnectors held by the registry
     */
    public Collection<ComponentMBeanImpl> getComponents() {
        synchronized (idMap) {
            return new ArrayList<ComponentMBeanImpl>(idMap.values());
        }
    }

    private boolean setInitialRunningStateFromStart() throws JBIException {
        boolean result = !runningStateInitialized;
        if (!runningStateInitialized) {
            runningStateInitialized = true;
            for (ComponentMBeanImpl lcc : getComponents()) {
                try {
                    if (!lcc.isPojo() && !registry.isContainerEmbedded()) {
                        lcc.setInitialRunningState();
                    } else {
                        lcc.doStart();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error during set initial running state from start.", e);
                }
            }
        }
        return result;
    }

}
