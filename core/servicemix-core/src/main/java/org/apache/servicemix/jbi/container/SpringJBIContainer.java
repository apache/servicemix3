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

import javax.jbi.JBIException;

import org.apache.servicemix.components.util.ComponentAdaptor;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * An enhanced JBI container which adds some Spring helper methods for
 * easier configuration through spring's XML configuration file.
 *
 * @org.apache.xbean.XBean element="container" rootElement="true"
 * description="The ServiceMix JBI Container"
 * 
 * @version $Revision$
 */
public class SpringJBIContainer extends JBIContainer 
	implements InitializingBean, DisposableBean, BeanFactoryAware, ApplicationContextAware {
	
    private String[] componentNames;
    private ActivationSpec[] activationSpecs;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private String[] deployArchives;
    private Object shutdownLock;

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

        start();
    }

    public void stop() throws JBIException {
        if (beanFactory instanceof DisposableBean) {
            DisposableBean disposable = (DisposableBean) beanFactory;
            try {
                disposable.destroy();
            }
            catch (Exception e) {
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

    // Implementation methods
    //-------------------------------------------------------------------------
    protected Object lookupBean(String componentName) {
        Object bean = beanFactory.getBean(componentName);
        if (bean == null) {
            throw new IllegalArgumentException("Component name: " + componentName
                    + " is not found in the Spring BeanFactory");
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
    }

    public void shutDown() throws JBIException {
        if (shutdownLock != null) {
            synchronized (shutdownLock) {
                shutdownLock.notify();
            }
        }
    }

    /**
     * @param lock
     * @org.apache.xbean.Property hidden="true"
     */
    public void setShutdownLock(Object lock) {
        this.shutdownLock = lock;
    }

}
