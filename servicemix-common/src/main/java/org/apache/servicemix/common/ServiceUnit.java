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
package org.apache.servicemix.common;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ServiceUnit {

    protected BaseComponent component;
    protected String name;
    protected String rootPath;
    protected String status = LifeCycleMBean.STOPPED;
    protected Map endpoints = new HashMap();
    
    public ServiceUnit() {
    }
    
    public void start() throws Exception {
        // Activate endpoints
        for (Iterator iter = getEndpoints().iterator(); iter.hasNext();) {
            Endpoint endpoint = (Endpoint) iter.next();
            endpoint.activate();
        }
        this.status = LifeCycleMBean.STARTED;
    }
    
    public void stop() throws Exception {
        this.status = LifeCycleMBean.STOPPED;
        // Activate endpoints
        for (Iterator iter = getEndpoints().iterator(); iter.hasNext();) {
            Endpoint endpoint = (Endpoint) iter.next();
            endpoint.deactivate();
        }
    }
    
    public void shutDown() throws JBIException {
        this.status = LifeCycleMBean.SHUTDOWN;
    }
    
    public String getCurrentState() {
        return status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    /**
     * @return Returns the component.
     */
    public BaseComponent getComponent() {
        return component;
    }
    
    /**
     * @param component The component to set.
     */
    public void setComponent(BaseComponent component) {
        this.component = component;
    }
    
    public Collection getEndpoints() {
        return this.endpoints.values();
    }
    
    public void addEndpoint(Endpoint endpoint) {
        this.endpoints.put(EndpointSupport.getKey(endpoint), endpoint);
    }
    
    public Endpoint getEndpoint(String key) {
        return (Endpoint) this.endpoints.get(key);
    }


}
