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

import java.io.Serializable;
import java.util.Set;

import org.apache.servicemix.jbi.container.SubscriptionSpec;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

/**
 * ComponentPacket - potentially passed around clusters
 *
 * @version $Revision$
 */
public class ComponentPacket implements Serializable {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 6229348132456423265L;
    
    private ComponentNameSpace componentName;
    private String description = "POJO Component";
    private Set subscriptionSpecs = new CopyOnWriteArraySet();
    private boolean binding;
    private boolean service;
    
    
    /**
     * Default Constructor
     *
     */
    public ComponentPacket() {
    }
    
    /**
     * Construct with it's id and delivery channel Id
     * @param componentName
     */
    public ComponentPacket(ComponentNameSpace componentName) {
        this.componentName = componentName;
    }
    
    /**
     * @return Returns the componentName.
     */
    public ComponentNameSpace getComponentNameSpace() {
        return componentName;
    }
    /**
     * @param componentName The componentName to set.
     */
    public void setComponentNameSpace(ComponentNameSpace componentName) {
        this.componentName = componentName;
    }
    
    public void addSubscriptionSpec(SubscriptionSpec ss) {
        subscriptionSpecs.add(ss);
    }
    
    public void removeSubscriptionSpec(SubscriptionSpec ss) {
        subscriptionSpecs.remove(ss);
    }
    
    /**
     * Get the Set of activated endpoints
     * @return the activated endpoint Set
     */
    public Set getSubscriptionSpecs() {
        return subscriptionSpecs;
    }
    
    /**
     * @return Returns the binding.
     */
    public boolean isBinding() {
        return binding;
    }
    /**
     * @param binding The binding to set.
     */
    public void setBinding(boolean binding) {
        this.binding = binding;
    }
    /**
     * @return Returns the service.
     */
    public boolean isService() {
        return service;
    }
    /**
     * @param service The service to set.
     */
    public void setService(boolean service) {
        this.service = service;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Test for equivalence
     * @param obj
     * @return true if obj equivalent to this
     */
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && obj instanceof ComponentPacket) {
            ComponentPacket other =(ComponentPacket) obj;
            result = other.getComponentNameSpace().equals(this.getComponentNameSpace());
        }
        return result;
    }
    
    /**
     * @return hashCodde
     */
    public int hashCode() {
        return getComponentNameSpace().hashCode();
    }
    
    public String toString() {
    	return "ComponentPacket[" + componentName + "]";
    }
    
}
