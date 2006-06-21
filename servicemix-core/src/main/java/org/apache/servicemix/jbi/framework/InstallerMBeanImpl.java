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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.Component;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallerMBean;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.loaders.ClassLoaderUtil;
import org.apache.servicemix.jbi.loaders.DestroyableClassLoader;
import org.apache.servicemix.jbi.loaders.JarFileClassLoader;

/**
 * InstallerMBean defines standard installation and uninstallation controls for Binding Components and Service Engines.
 * Binding Components and Service Engines.
 * 
 * @version $Revision$
 */
public class InstallerMBeanImpl implements InstallerMBean {
    private static final Log log = LogFactory.getLog(InstallerMBeanImpl.class);
    private InstallationContextImpl context;
    private Bootstrap bootstrap;
    private boolean installed;
    private JBIContainer container;
    private ObjectName objectName;

    /**
     * Constructor for the InstallerMBean
     * 
     * @param container
     * @param ic
     * @param componentLoader
     * @param componentClassName
     * @param bootstrapLoader
     * @param bootstrapClassName
     * @throws DeploymentException
     */
    public InstallerMBeanImpl(JBIContainer container, 
                              InstallationContextImpl ic, 
                              boolean installed) throws DeploymentException {
        this.container = container;
        this.context = ic;
        this.installed = installed;
        if (!installed) {
            createBootstrap();
        }
    }
    
    protected void createBootstrap() throws DeploymentException {
        if (bootstrap != null) {
            return;
        }
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        org.apache.servicemix.jbi.deployment.Component descriptor = context.getDescriptor();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            ClassLoader cl = buildClassLoader(
                                    context.getInstallRootAsDir(),
                                    descriptor.getBootstrapClassPath().getPathElements(),
                                    descriptor.isBootstrapClassLoaderDelegationParentFirst(),
                                    null);
            Class bootstrapClass = cl.loadClass(descriptor.getBootstrapClassName());
            this.bootstrap = (Bootstrap) bootstrapClass.newInstance();
            this.bootstrap.init(this.context);
        }
        catch (MalformedURLException e) {
            log.error("Could not create class loader", e);
            throw new DeploymentException(e);
        }
        catch (ClassNotFoundException e) {
            log.error("Class not found: " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        }
        catch (InstantiationException e) {
            log.error("Could not instantiate : " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        }
        catch (IllegalAccessException e) {
            log.error("Illegal access on: " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        }
        catch (JBIException e) {
            log.error("Could not initialize : " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        } 
        finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    /**
     * Get the installation root directory path for this BC or SE.
     * 
     * @return the full installation path of this component.
     */
    public String getInstallRoot() {
        return context.getInstallRoot();
    }

    /**
     * Install a BC or SE.
     * 
     * @return JMX ObjectName representing the ComponentLifeCycle for the installed component, or null if the
     * installation did not complete.
     * @throws javax.jbi.JBIException if the installation fails.
     */
    public ObjectName install() throws JBIException {
        if (installed) {
            throw new DeploymentException("Component is already installed");
        }
        createBootstrap();
        if (bootstrap != null) {
            bootstrap.onInstall();
        }
        // TODO: the bootstrap may change the class path for the component,
        // so we need to persist it somehow
        ObjectName result = null;
        try {
            result = activateComponent();
            ComponentMBeanImpl lcc = container.getComponent(context.getComponentName());
            lcc.persistRunningState();
            installed = true;
        } finally {
            if (bootstrap != null) {
                bootstrap.cleanUp();
            }
        }
        return result;
    }
    
    public ObjectName activateComponent() throws JBIException {
        ObjectName result = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        org.apache.servicemix.jbi.deployment.Component descriptor = context.getDescriptor();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            ClassLoader cl = buildClassLoader(
                                    context.getInstallRootAsDir(),
                                    (String[]) context.getClassPathElements().toArray(new String[0]),
                                    descriptor.isComponentClassLoaderDelegationParentFirst(),
                                    context.getSharedLibraries());
            Class componentClass = cl.loadClass(descriptor.getComponentClassName());
            Component component = (Component) componentClass.newInstance();
            result = container.activateComponent(
                                    context.getInstallRootAsDir(), 
                                    component, 
                                    context.getComponentDescription(),
                                    (ComponentContextImpl) context.getContext(), 
                                    context.isBinding(), 
                                    context.isEngine(), 
                                    context.getSharedLibraries());
        }
        catch (MalformedURLException e) {
            log.error("Could not create class loader", e);
            throw new DeploymentException(e);
        }
        catch (ClassNotFoundException e) {
            log.error("Class not found: " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        }
        catch (InstantiationException e) {
            log.error("Could not instantiate : " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        }
        catch (IllegalAccessException e) {
            log.error("Illegal access on: " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        }
        catch (JBIException e) {
            log.error("Could not initialize : " + descriptor.getBootstrapClassName(), e);
            throw new DeploymentException(e);
        } 
        finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
        return result;
    }

    /**
     * Determine whether or not the component is installed.
     * 
     * @return true if this component is currently installed, false if not.
     */
    public boolean isInstalled() {
        return installed;
    }

    /**
     * Uninstall a BC or SE. This completely removes the component from the JBI system.
     * 
     * @throws javax.jbi.JBIException if the uninstallation fails.
     */
    public void uninstall() throws javax.jbi.JBIException {
        // TODO: check component status
        // the component must not be started and not have any SUs deployed
        if (!installed) {
            throw new DeploymentException("Component is not installed");
        }
        createBootstrap();
        if (bootstrap != null){
            bootstrap.onUninstall();
        }
        String componentName = context.getComponentName();
        container.deactivateComponent(componentName);
        installed = false;
        if (bootstrap != null){
            bootstrap.cleanUp();
        }
        ClassLoader cl = bootstrap.getClass().getClassLoader();
        if (cl instanceof DestroyableClassLoader) {
            ((DestroyableClassLoader) cl).destroy();
        } else {
            ClassLoaderUtil.destroy(cl);
        }
        // TODO: destroy component class loader
        bootstrap = null;
        context = null;
        System.gc();
        container.getEnvironmentContext().removeComponentRootDirectory(componentName);
    }

    /**
     * Get the installer configuration MBean name for this component.
     * 
     * @return the MBean object name of the Installer Configuration MBean.
     * @throws javax.jbi.JBIException if the component is not in the LOADED state or any error occurs during processing.
     */
    public ObjectName getInstallerConfigurationMBean() throws javax.jbi.JBIException {
        return bootstrap != null ? bootstrap.getExtensionMBeanName() : null;
    }
    /**
     * @return Returns the objectName.
     */
    public ObjectName getObjectName() {
        return objectName;
    }
    /**
     * @param objectName The objectName to set.
     */
    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    /**
     * Buld a Custom ClassLoader
     * 
     * @param dir
     * @param classPathNames
     * @param parentFirst
     * @param list
     * @return ClassLoader
     * @throws MalformedURLException
     * @throws MalformedURLException
     * @throws DeploymentException
     */
    protected ClassLoader buildClassLoader(
                    File dir,
                    String[] classPathNames, 
                    boolean parentFirst,
                    String[] list) throws MalformedURLException, DeploymentException {
        
        // Make the current ClassLoader the parent
        ClassLoader[] parents;
        
        // Create a new parent if there are some shared libraries
        if (list != null && list.length > 0) {
            parents = new ClassLoader[list.length];
            for (int i = 0; i < parents.length; i++) {
                org.apache.servicemix.jbi.framework.SharedLibrary sl = 
                    container.getRegistry().getSharedLibrary(list[i]);
                if (sl == null) {
                    throw new DeploymentException("Shared library " + list[i] + " is not installed");
                }
                parents[i] = sl.getClassLoader();
            }
        } else {
            parents = new ClassLoader[] { getClass().getClassLoader() };
        }
        
        List urls = new ArrayList();
        for (int i = 0; i < classPathNames.length; i++) {
            File file = new File(dir, classPathNames[i]);
            if (!file.exists()) {
                log.warn("Unable to add File " + file
                        + " to class path as it doesn't exist: "
                        + file.getAbsolutePath());
            }
            urls.add(file.toURL());
        }
        
        return new JarFileClassLoader(
                        "Componnent ClassLoader",
                        (URL[]) urls.toArray(new URL[urls.size()]),
                        parents, 
                        !parentFirst,
                        new String[0],
                        new String[] { "java.", "javax." });
    }

}
