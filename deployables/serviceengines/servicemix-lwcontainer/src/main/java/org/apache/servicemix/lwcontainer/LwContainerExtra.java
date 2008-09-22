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
package org.apache.servicemix.lwcontainer;

import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.common.endpoints.AbstractEndpoint;
import org.apache.servicemix.id.IdGenerator;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.xbean.spring.context.impl.NamespaceHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;

/**
 * Fake endpoint that hold components, endpoints, listeners and services.
 */
public class LwContainerExtra extends AbstractEndpoint {

    private Map components;
    private Map endpoints;
    private EventListener[] listeners;
    private BaseSystemService[] services;
    private IdGenerator idGenerator = new IdGenerator();
    private Map<Object, Component> endpointToComponent = new IdentityHashMap<Object, Component>();

    public LwContainerExtra(Map components, Map endpoints, EventListener[] listeners, BaseSystemService[] services) {
        this.service = new QName("http://servicemix.apache.org/lwcontainer", "extra");
        this.endpoint = idGenerator.generateSanitizedId();
        this.components = components;
        this.endpoints = endpoints;
        this.listeners = listeners;
        this.services = services;
    }

    public void activate() throws Exception {
        if (components != null) {
            for (Iterator it = components.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                if (!(e.getKey() instanceof String)) {
                    throw new JBIException("Component must have a non null string name");
                }
                if (!(e.getValue() instanceof Component)) {
                    throw new JBIException("Component is not a known component");
                }
                String name = (String) e.getKey();
                getContainer().activateComponent((Component) e.getValue(), name);
                getContainer().getComponent(name).init();
            }
        }

        if (endpoints != null) {
            initEndpoints();
        }
        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                getContainer().addListener(listeners[i]);
            }
        }
        if (services != null) {
            for (int i = 0; i < services.length; i++) {
                services[i].init(getContainer());
                services[i].start();
            }
        }
    }

    public void deactivate() throws Exception {
        // Remove endpoints
        if (endpoints != null) {
            for (Iterator it = endpoints.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                List l = (List) e.getValue();
                for (Iterator itEp = l.iterator(); itEp.hasNext();) {
                    Object endpoint = itEp.next();
                    Component c = endpointToComponent.remove(endpoint);
                    ((DefaultComponent) c).removeEndpoint((Endpoint) endpoint);
                }
            }
        }
        // Deactivate components
        if (components != null) {
            for (Iterator it = components.entrySet().iterator(); it.hasNext();) {
                Map.Entry e = (Map.Entry) it.next();
                String name = (String) e.getKey();
                getContainer().deactivateComponent(name);
            }
        }
        // Remove listeners
        if (listeners != null) {
            for (int i = 0; i < listeners.length; i++) {
                getContainer().removeListener(listeners[i]);
            }
        }
        // Remove services
        if (services != null) {
            for (int i = 0; i < services.length; i++) {
                services[i].stop();
                services[i].shutDown();
            }
        }
    }

    public ExchangeProcessor getProcessor() {
        return null;
    }

    public MessageExchange.Role getRole() {
        return null;
    }

    private void initEndpoints() throws Exception {
        if (components == null) {
            components = new LinkedHashMap();
        }
        Method getEndpointClassesMethod = DefaultComponent.class.getDeclaredMethod("getEndpointClasses", null);
        getEndpointClassesMethod.setAccessible(true);
        for (Iterator it = endpoints.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();
            String key = (String) e.getKey();
            List l = (List) e.getValue();
            for (Iterator itEp = l.iterator(); itEp.hasNext();) {
                Object endpoint = itEp.next();
                Component c = null;
                if (key.length() > 0) {
                    Component comp = (Component) components.get(key);
                    if (comp == null) {
                        throw new JBIException("Could not find component '" + key + "' specified for endpoint");
                    }
                    c = comp;
                } else {
                    for (Iterator itCmp = components.values().iterator(); itCmp.hasNext();) {
                        Component comp = (Component) itCmp.next();
                        Class[] endpointClasses = (Class[]) getEndpointClassesMethod.invoke(comp, null);
                        if (isKnownEndpoint(endpoint, endpointClasses)) {
                            c = comp;
                            break;
                        }
                    }
                    if (c == null) {
                        c = getComponentForEndpoint(getEndpointClassesMethod, endpoint);
                        if (c == null) {
                            throw new JBIException("Unable to find a component for endpoint class: " + endpoint.getClass());
                        }
                    }
                }
                ((DefaultComponent) c).addEndpoint((Endpoint) endpoint);
                endpointToComponent.put(endpoint, c);
            }
        }
    }

    private Component getComponentForEndpoint(Method getEndpointClassesMethod, Object endpoint) throws Exception {
        Properties namespaces = PropertiesLoaderUtils.loadAllProperties("META-INF/spring.handlers");
        for (Iterator itNs = namespaces.keySet().iterator(); itNs.hasNext();) {
            String namespaceURI = (String) itNs.next();
            String uri = NamespaceHelper.createDiscoveryPathName(namespaceURI);
            Properties props = PropertiesLoaderUtils.loadAllProperties(uri);
            String compClassName = props.getProperty("component");
            if (compClassName != null) {
                Class compClass = ClassUtils.forName(compClassName);
                Component comp = (Component) BeanUtils.instantiateClass(compClass);
                Class[] endpointClasses = (Class[]) getEndpointClassesMethod.invoke(comp, null);
                if (isKnownEndpoint(endpoint, endpointClasses)) {
                    String name = chooseComponentName(comp);
                    getContainer().activateComponent(comp, name);
                    components.put(name, comp);
                    return comp;
                }
            }
        }
        return null;
    }

    private String chooseComponentName(Object c) {
        String className = c.getClass().getName();
        if (className.startsWith("org.apache.servicemix.")) {
            int idx1 = className.lastIndexOf('.');
            int idx0 = className.lastIndexOf('.', idx1 - 1);
            String name = "servicemix-" + className.substring(idx0 + 1, idx1);
            return name + "-" + createComponentID();
        }
        return createComponentID();
    }

    private boolean isKnownEndpoint(Object endpoint, Class[] knownClasses) {
        if (knownClasses != null) {
            for (int i = 0; i < knownClasses.length; i++) {
                if (knownClasses[i].isInstance(endpoint)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JBIContainer getContainer() {
        ComponentContext context = getServiceUnit().getComponent().getComponentContext();
        if (context instanceof ComponentContextImpl) {
            return ((ComponentContextImpl) context).getContainer();
        }
        throw new IllegalStateException("LwContainer component can only be deployed in ServiceMix");
    }

    protected String createComponentID() {
        return idGenerator.generateId();
    }

    @Override
    public void process(MessageExchange exchange) throws Exception {
        getProcessor().process(exchange);
    }

    @Override
    public void start() throws Exception {
        // gracefully do nothing
    }

    @Override
    public void stop() throws Exception {
        // gracefully do nothing
    }

}
