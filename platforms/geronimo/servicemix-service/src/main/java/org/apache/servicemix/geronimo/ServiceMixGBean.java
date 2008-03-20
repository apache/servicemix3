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
package org.apache.servicemix.geronimo;

import java.io.File;
import java.util.Set;

import javax.jbi.JBIException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.servicemix.jbi.container.ComponentEnvironment;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.container.ServiceAssemblyEnvironment;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.framework.ServiceAssemblyLifeCycle;

public class ServiceMixGBean implements GBeanLifecycle, Container {

    private Log log = LogFactory.getLog(getClass().getName());
    
    private JBIContainer container;
    private String name;
    private String directory;
    private final AbstractNameQuery transactionManagerName;
    private Kernel kernel;

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("ServiceMix JBI Container", ServiceMixGBean.class, "JBIContainer");
        infoFactory.addInterface(Container.class);
        infoFactory.addAttribute("name", String.class, true);
        infoFactory.addAttribute("directory", String.class, true);
        infoFactory.addAttribute("transactionManager", AbstractNameQuery.class, true, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.setConstructor(new String[]{"name", "directory", "transactionManager", "kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public ServiceMixGBean(String name, 
                           String directory, 
                           AbstractNameQuery transactionManagerName, 
                           Kernel kernel) {
        this.name = name;
        this.directory = directory;
        this.transactionManagerName = transactionManagerName;
        this.kernel = kernel;
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean created");
        }
    }
    
    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     *
     * @throws Exception if the target failed to start; this will cause a transition to the failed state
     */
    public void doStart() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean doStart");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ServiceMixGBean.class.getClassLoader());
        try {
            if (container == null) {
                container = createContainer();
                container.init();
                RootContext.setComponentContext(container.getNamingContext());
                container.start();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     *
     * @throws Exception if the target failed to stop; this will cause a transition to the failed state
     */
    public void doStop() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean doStop");
        }
        try {
            if (container != null) {
                container.shutDown();
            }
        } finally {
            container = null;
        }
    }

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    public void doFail() {
        if (log.isDebugEnabled()) {
            log.debug("ServiceMixGBean doFail");
        }
        try {
            if (container != null) {
                try {
                    container.shutDown();
                }
                catch (JBIException e) {
                    log.info("Caught while closing due to failure: " + e, e);
                }
            }
        } finally {
            container = null;
        }
    }

    private JBIContainer createContainer() throws GBeanNotFoundException {
        JBIContainer container = new JBIContainer();
        container.setUseShutdownHook(false);
        container.setName(name);
        container.setRootDir(directory);
        container.setUseMBeanServer(true);
        TransactionManager tm = getTransactionManager();
        container.setTransactionManager(tm);
        container.setMonitorInstallationDirectory(false);
        container.setMonitorDeploymentDirectory(false);
        return container;
    }
    
    /**
     * Determines the TM from Geronimo
     * 
     * @return TransactionManager
     * @throws GBeanNotFoundException
     */
    private TransactionManager getTransactionManager() throws GBeanNotFoundException {
        Set listGBeans = kernel.listGBeans(transactionManagerName);
        AbstractName tmName = (AbstractName) listGBeans.iterator().next();
        TransactionManager tm = (TransactionManager) kernel.getGBean(tmName);
        return tm;
    }

    /**
     * Returns the JBIContainer
     * 
     * @return JBIContainer
     */
    public JBIContainer getJBIContainer() {
        return container;
    }
    
    public void register(Component component) throws Exception {
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), component.getName());
        ComponentContextImpl context = new ComponentContextImpl(container, cns);
        ComponentEnvironment env = new ComponentEnvironment();
        env.setComponentRoot(new File(component.getRootDir()));
        env.setInstallRoot(new File(component.getInstallDir()));
        env.setWorkspaceRoot(new File(component.getWorkDir()));
        context.setEnvironment(env);
        
        container.activateComponent(null,
                                    component.getComponent(),
                                    component.getDescription(),
                                    context,
                                    component.getType().equals("binding-component"),
                                    component.getType().equals("service-engine"),
                                    null);
        ComponentMBeanImpl cmb = container.getComponent(component.getName());
        File stateFile = cmb.getContext().getEnvironment().getStateFile();
        if (stateFile.isFile()) {
            cmb.setInitialRunningState();
        } else {
            cmb.start();
        }
    }

    public void unregister(Component component) throws Exception {
        container.deactivateComponent(component.getName());
    }
    
    public void register(ServiceAssembly assembly) throws Exception {
        File rootDir = new File(assembly.getRootDir());
        ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
        env.setRootDir(rootDir);
        env.setInstallDir(new File(rootDir, "install"));
        env.setSusDir(new File(rootDir, "sus"));
        env.setStateFile(new File(rootDir, "state.xml"));
        ServiceAssemblyLifeCycle salc = container.getRegistry().registerServiceAssembly(assembly.getDescriptor().getServiceAssembly(), env);
        if (env.getStateFile().isFile()) {
            salc.restore();
        } else {
            salc.start();
        }
    }
    
    public void unregister(ServiceAssembly assembly) throws Exception {
        ServiceAssemblyLifeCycle salc = container.getRegistry().getServiceAssembly(assembly.getName());
        salc.shutDown(false);
        assembly.undeploySus();
        container.getRegistry().unregisterServiceAssembly(assembly.getName());
    }

}
