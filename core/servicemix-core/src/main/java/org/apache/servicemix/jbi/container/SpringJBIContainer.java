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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Semaphore;

import javax.jbi.JBIException;
import javax.jbi.component.Component;

import org.apache.servicemix.components.util.ComponentAdaptor;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.xbean.spring.context.impl.NamespaceHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.ClassUtils;

/**
 * An enhanced JBI container which adds some Spring helper methods for
 * easier configuration through spring's XML configuration file.
 *
 * @org.apache.xbean.XBean element="container" rootElement="true"
 * description="The ServiceMix JBI Container"
 * 
 * @version $Revision$
 */
public class SpringJBIContainer extends JBIContainer implements InitializingBean, DisposableBean, 
                                                                BeanFactoryAware, ApplicationContextAware {

    private String[] componentNames;
    private ActivationSpec[] activationSpecs;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private String[] deployArchives;
    private DeploySupport[] deployments;
    private Map components;
    private Map endpoints;
    private Runnable onShutDown;
    private Semaphore block = new Semaphore(0);

    public void afterPropertiesSet() throws Exception {
        init();

        // lets iterate through all the component names and register them
        if (componentNames != null) {
            for (int i = 0; i < componentNames.length; i++) {
                String componentName = componentNames[i];
                activateComponent(new ActivationSpec(componentName, lookupBean(componentName)));
            }
        }

        if (activationSpecs != null) {
            for (int i = 0; i < activationSpecs.length; i++) {
                ActivationSpec activationSpec = activationSpecs[i];
                activateComponent(activationSpec);
            }
        }

        if (deployArchives != null) {
            for (int i = 0; i < deployArchives.length; i++) {
                String archive = deployArchives[i];
                installArchive(archive);
            }
        }

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
                activateComponent((Component) e.getValue(), name);
                getComponent(name).init();
            }
        }

        if (endpoints != null) {
            initEndpoints();
        }

        if (deployments != null) {
            for (DeploySupport deployment : deployments) {
                deployment.deploy(this);
            }
        }

        start();
    }

    private void initEndpoints() throws Exception {
        if (components == null) {
            components = new LinkedHashMap();
        }
        Class componentClass = Class.forName("org.apache.servicemix.common.DefaultComponent");
        Class endpointClass = Class.forName("org.apache.servicemix.common.Endpoint");
        Method addEndpointMethod = componentClass.getDeclaredMethod("addEndpoint", new Class[] {endpointClass });
        addEndpointMethod.setAccessible(true);
        Method getEndpointClassesMethod = componentClass.getDeclaredMethod("getEndpointClasses", null);
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
                addEndpointMethod.invoke(c, new Object[] {endpoint });
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
                    activateComponent(comp, name);
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
            if (registry.getComponent(name) == null) {
                return name;
            }
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

    public void stop() throws JBIException {
        if (beanFactory instanceof DisposableBean) {
            DisposableBean disposable = (DisposableBean) beanFactory;
            try {
                disposable.destroy();
            } catch (Exception e) {
                throw new JBIException("Failed to dispose of the Spring BeanFactory due to: " + e, e);
            }
        }
        super.stop();
    }

    /**
     * Returns the compoment or POJO registered with the given component ID.
     *
     * @param id
     * @return the Component
     */
    public Object getBean(String id) {
        ComponentMBeanImpl component = getComponent(id);
        Object bean = component != null ? component.getComponent() : null;
        if (bean instanceof ComponentAdaptor) {
            bean = ((ComponentAdaptor) bean).getLifeCycle();
        }
        return bean;
    }

    // Properties
    //-------------------------------------------------------------------------
    /**
     * @org.apache.xbean.Property hidden="true"
     */
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public String[] getComponentNames() {
        return componentNames;
    }

    public void setComponentNames(String[] componentNames) {
        this.componentNames = componentNames;
    }

    public ActivationSpec[] getActivationSpecs() {
        return activationSpecs;
    }

    public void setActivationSpecs(ActivationSpec[] activationSpecs) throws JBIException {
        this.activationSpecs = activationSpecs;
    }

    public String[] getDeployArchives() {
        return deployArchives;
    }

    public void setDeployArchives(String[] deployArchives) {
        this.deployArchives = deployArchives;
    }

    public DeploySupport[] getDeployments() {
        return deployments;
    }

    public void setDeployments(DeploySupport[] deployments) {
        this.deployments = deployments;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected Object lookupBean(String componentName) {
        Object bean = beanFactory.getBean(componentName);
        if (bean == null) {
            throw new IllegalArgumentException("Component name: " + componentName + " is not found in the Spring BeanFactory");
        }
        return bean;
    }

    /**
     * @return
     * @org.apache.xbean.Property hidden="true"
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void destroy() throws Exception {
        super.shutDown();
        block.release();
    }

    public void shutDown() throws JBIException {
        if (onShutDown != null) {
            onShutDown.run();
        } else {
            //no shutdown handler has been set
            //shutting down the container ourselves
            super.shutDown();
        }
    }

    public void block() throws InterruptedException {
        block.acquire();
    }

    /**
     * Set a {@link Runnable} which can handle the shutdown of the container
     * 
     * @param runnable the shutdown handler
     */
    public void onShutDown(Runnable runnable) {
        this.onShutDown = runnable;
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

}
