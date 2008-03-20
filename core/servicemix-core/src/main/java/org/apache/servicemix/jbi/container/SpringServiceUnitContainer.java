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
package org.apache.servicemix.jbi.container;

import java.util.EventListener;
import java.util.Map;

import org.apache.servicemix.jbi.management.BaseSystemService;

/**
 * Used to hold a Server Unit configuration.  The components
 * are registered into the JBI container using the Service Unit
 * Manager life cycle methods.
 *
 * @org.apache.xbean.XBean element="serviceunit" rootElement="true"
 *                  description="A deployable service unit container"
 * @version $Revision$
 */
public class SpringServiceUnitContainer {
    
    private ActivationSpec[] activationSpecs;
    private Map components;
    private Map endpoints;
    private EventListener[] listeners;
    private BaseSystemService[] services;

    public ActivationSpec[] getActivationSpecs() {
        return activationSpecs;
    }

    public void setActivationSpecs(ActivationSpec[] activationSpecs) {
        this.activationSpecs = activationSpecs;
    }

    /**
     * @org.apache.xbean.Map flat="true" keyName="name"
     */
    public Map getComponents() {
        return components;
    }

    public void setComponents(Map components) {
        this.components = components;
    }

    /**
     * @org.apache.xbean.Map flat="true" dups="always" keyName="component" defaultKey=""
     */
    public Map getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(Map endpoints) {
        this.endpoints = endpoints;
    }

    public EventListener[] getListeners() {
        return listeners;
    }

    public void setListeners(EventListener[] listeners) {
        this.listeners = listeners;
    }

    public BaseSystemService[] getServices() {
        return services;
    }

    public void setServices(BaseSystemService[] services) {
        this.services = services;
    }
}
