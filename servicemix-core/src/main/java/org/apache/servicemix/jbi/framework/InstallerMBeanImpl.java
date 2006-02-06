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

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.Component;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallerMBean;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.JBIContainer;

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
    private ClassLoader componentClassLoader;
    private String componentClassName;
    private ClassLoader bootstrapClassLoader;
    private String bootstrapClassName;
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
                              ClassLoader componentLoader,
                              String componentClassName, 
                              ClassLoader bootstrapLoader, 
                              String bootstrapClassName,
                              boolean installed) throws DeploymentException {
        this.container = container;
        this.context = ic;
        this.componentClassLoader = componentLoader;
        this.componentClassName = componentClassName;
        this.bootstrapClassLoader = bootstrapLoader;
        this.bootstrapClassName = bootstrapClassName;
        this.installed = installed;
        //initialize the bootstrap
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        if (bootstrapLoader != null && bootstrapClassName != null && bootstrapClassName.length() > 0){
            
            
                Class bootstrapClass;
                try {
                    bootstrapClass = bootstrapClassLoader.loadClass(bootstrapClassName);
                    if (bootstrapClass == null){
                        throw new DeploymentException("Could not find bootstrap class: " + bootstrapClassName);
                    }
                    this.bootstrap = (Bootstrap) bootstrapClass.newInstance();
                    this.bootstrap.init(this.context);
                }
                catch (ClassNotFoundException e) {
                    log.error("Class not found: " + bootstrapClassName,e);
                    throw new DeploymentException(e);
                }
                catch (InstantiationException e) {
                    log.error("Could not instantiate : " + bootstrapClassName,e);
                    throw new DeploymentException(e);
                }
                catch (IllegalAccessException e) {
                    log.error("Illegal access on: " + bootstrapClassName,e);
                    throw new DeploymentException(e);
                }
                catch (JBIException e) {
                    log.error("Could not initialize : " + bootstrapClassName,e);
                    throw new DeploymentException(e);
                }
                
           
            
            
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
        if (bootstrap != null) {
            bootstrap.onInstall();
        }
        ObjectName result = null;
        try {
            result = activateComponent();
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
        try {
            Class componentClass = componentClassLoader.loadClass(context.getComponentClassName());
            if (componentClass != null){
                Component component = (Component) componentClass.newInstance();
                result = container.activateComponent(context.getInstallRootAsDir(), component, context.getComponentDescription(),(ComponentContextImpl) context.getContext(), context
                        .isBinding(), context.isEngine());
            }
            else {
                String err = "component class " + context.getComponentClassName() + " not found";
                log.error(err);
                throw new DeploymentException(err);
            }
        }
        catch (ClassNotFoundException e) {
            log.error("component class " + context.getComponentClassName() + " not found");
            throw new DeploymentException(e);
        }
        catch (InstantiationException e) {
            throw new DeploymentException(e);
        }
        catch (IllegalAccessException e) {
            throw new DeploymentException(e);
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
        if (!installed) {
            throw new DeploymentException("Component is not installed");
        }
        if (bootstrap != null){
            bootstrap.onUninstall();
        }
        String componentName = context.getComponentName();
        container.deactivateComponent(componentName);
        installed = false;
        if (bootstrap != null){
            bootstrap.cleanUp();
        }
        ClassLoaderUtil.destroy(componentClassLoader);
        ClassLoaderUtil.destroy(bootstrapClassLoader);
        componentClassLoader = null;
        bootstrapClassLoader = null;
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
     * 
     * @return Returns the Bootstrap Class Name
     */
	public String getBootstrapClassName() {
		return bootstrapClassName;
	}

	/**
	 * 
	 * @return Returns the Component Class Name
	 */
	public String getComponentClassName() {
		return componentClassName;
	}

}
