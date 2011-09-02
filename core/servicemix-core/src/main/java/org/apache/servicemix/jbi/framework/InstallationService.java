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

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallationServiceMBean;
import javax.jbi.management.InstallerMBean;
import javax.management.Attribute;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.servicemix.jbi.container.ComponentEnvironment;
import org.apache.servicemix.jbi.container.EnvironmentContext;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.deployment.Component;
import org.apache.servicemix.jbi.deployment.Descriptor;
import org.apache.servicemix.jbi.deployment.DescriptorFactory;
import org.apache.servicemix.jbi.deployment.SharedLibrary;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.management.ParameterHelper;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.jbi.util.FileVersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installation Service - installs/uninstalls archives
 * 
 * @version $Revision$
 */
public class InstallationService extends BaseSystemService implements InstallationServiceMBean {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(InstallationService.class);

    private EnvironmentContext environmentContext;

    private ManagementContext managementContext;

    private Map<String, InstallerMBeanImpl> installers = new ConcurrentHashMap<String, InstallerMBeanImpl>();

    private Map<String, InstallerMBeanImpl> nonLoadedInstallers = new ConcurrentHashMap<String, InstallerMBeanImpl>();

    /**
     * Get Description
     * 
     * @return description of this item
     */
    public String getDescription() {
        return "installs/uninstalls Components";
    }

    /**
     * Load the installer for a new component from a component installation
     * package.
     * 
     * @param installJarURL -
     *            URL locating a jar file containing a JBI Installable
     *            Component.
     * @return - the JMX ObjectName of the InstallerMBean loaded from
     *         installJarURL.
     */
    public synchronized ObjectName loadNewInstaller(String installJarURL) {
        try {
            ObjectName result = null;
            LOGGER.debug("Loading new installer from {}", installJarURL);
            File tmpDir = AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(), installJarURL);
            if (tmpDir == null) {
                throw new RuntimeException("loation " + installJarURL + " isn't valid");
            }
            Descriptor root = DescriptorFactory.buildDescriptor(tmpDir);
            if (root == null || root.getComponent() == null) {
                throw new RuntimeException("Could not find Component from " + installJarURL);
            }
            String componentName = root.getComponent().getIdentification().getName();
            if (installers.containsKey(componentName)) {
                throw new RuntimeException("An installer already exists for " + componentName);
            }
            InstallerMBeanImpl installer = doInstallArchive(tmpDir, root);
            if (installer != null) {
                result = installer.getObjectName();
                LOGGER.debug("Registering installer as loaded for component {}", componentName);
                installers.put(componentName, installer);
            }
            return result;
        } catch (Throwable t) {
            LOGGER.error("Deployment failed", t);
            if (t instanceof Error) {
                throw (Error) t;
            }
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RuntimeException("Deployment failed: " + t.getMessage());
            }
        }
    }

    /**
     * Load the InstallerMBean for a previously installed component.
     * 
     * @param aComponentName -
     *            the component name identifying the installer to load.
     * @return - the JMX ObjectName of the InstallerMBean loaded from an
     *         existing installation context.
     */
    public ObjectName loadInstaller(String aComponentName) {
        LOGGER.debug("Going to load installer for component {}", aComponentName);
        InstallerMBeanImpl installer = installers.get(aComponentName);
        LOGGER.debug("Found installer for component {}", aComponentName);
        if (installer == null) {
            LOGGER.debug("Loaded installer for component {} was not found. Trying non-loaded installers", aComponentName);
            installer = nonLoadedInstallers.get(aComponentName);
            if (installer != null) {
                LOGGER.debug("Found non-loaded installer for component {}", aComponentName);
                try {
                    // create an MBean for the installer
                    ObjectName objectName = managementContext.createCustomComponentMBeanName("Installer", aComponentName);
                    installer.setObjectName(objectName);
                    managementContext.registerMBean(objectName, installer, InstallerMBean.class,
                                    "standard installation controls for a Component");
                } catch (Exception e) {
                    throw new RuntimeException("Could not load installer", e);
                }
                return installer.getObjectName();
            } else {
                LOGGER.debug("Did not found any installer for component " + aComponentName);
            }
        }
        return null;
    }

    private InstallerMBeanImpl createInstaller(String componentName) throws IOException, DeploymentException {
        File installationDir = environmentContext.getComponentInstallationDir(componentName);
        Descriptor root = DescriptorFactory.buildDescriptor(installationDir);
        Component descriptor = root.getComponent();

        InstallationContextImpl installationContext = new InstallationContextImpl(descriptor);
        installationContext.setInstall(false);
        installationContext.setInstallRoot(installationDir);
        // now build the ComponentContext
        File componentRoot = environmentContext.getComponentRootDir(componentName);
        ComponentContextImpl context = buildComponentContext(componentRoot, installationDir, componentName);
        installationContext.setContext(context);
        return new InstallerMBeanImpl(container, installationContext);
    }

    /**
     * Unload a JBI Installable Component installer.
     * 
     * @param componentName -
     *            the component name identifying the installer to unload.
     * @param isToBeDeleted -
     *            true if the component is to be deleted as well.
     * @return - true if the operation was successful, otherwise false.
     */
    public boolean unloadInstaller(String componentName, boolean isToBeDeleted) {
        LOGGER.debug("Going to unload installer for component {}", componentName);
        boolean result = false;
        try {
            InstallerMBeanImpl installer = installers.remove(componentName);
            result = installer != null;
            if (result) {
                LOGGER.debug("Found installer for component {}", componentName);
                container.getManagementContext().unregisterMBean(installer);
                if (isToBeDeleted) {
                    LOGGER.debug("Uninstalling component {}", componentName);
                    installer.uninstall();
                } else {
                    LOGGER.debug("Registering installer as non-loaded for component {}", componentName);
                    nonLoadedInstallers.put(componentName, installer);
                }
            } else {
                LOGGER.debug("Did not found installer for component {}", componentName);
            }
        } catch (JBIException e) {
            String errStr = "Problem unloading installer for component " + componentName;
            LOGGER.error(errStr, e);
        }
        return result;
    }

    /**
     * Install a shared library jar.
     * 
     * @param aSharedLibURI -
     *            URI locating a jar file containing a shared library.
     * @return - the name of the shared library loaded from aSharedLibURI.
     */
    public String installSharedLibrary(String aSharedLibURI) {
        String result = "";
        try {
            File tmpDir = AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(), aSharedLibURI);
            if (tmpDir != null) {
                Descriptor root = DescriptorFactory.buildDescriptor(tmpDir);
                if (root == null) {
                    throw new DeploymentException("Could not find JBI descriptor");
                }
                SharedLibrary sl = root.getSharedLibrary();
                if (sl != null) {
                    result = doInstallSharedLibrary(tmpDir, sl);
                } else {
                    throw new DeploymentException("JBI descriptor is not a SharedLibrary descriptor");
                }
            } else {
                throw new DeploymentException("Could not find JBI descriptor");
            }
        } catch (DeploymentException e) {
            LOGGER.error("Deployment failed", e);
        }
        return result;
    }

    /**
     * Uninstall a shared library.
     * 
     * @param aSharedLibName -
     *            the name of the shared library to uninstall.
     * @return - true iff the uninstall was successful.
     */
    public boolean uninstallSharedLibrary(String aSharedLibName) {
        // TODO: should check existence of shared library
        // and that it is not currently in use
        container.getRegistry().unregisterSharedLibrary(aSharedLibName);
        environmentContext.removeSharedLibraryDirectory(aSharedLibName);
        return true;
    }

    /**
     * Initialize the Service
     * 
     * @param container
     * @throws JBIException
     * @throws DeploymentException
     */
    public void init(JBIContainer container) throws JBIException {
        super.init(container);
        this.environmentContext = container.getEnvironmentContext();
        this.managementContext = container.getManagementContext();
        buildState();
    }

    protected Class getServiceMBean() {
        return InstallationServiceMBean.class;
    }

    /**
     * Install an archive
     * 
     * @param location
     * @param props
     * @param autoStart
     * @throws DeploymentException
     */
    public void install(String location, Properties props, boolean autoStart) throws DeploymentException {
        File tmpDir = AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(), location);
        if (tmpDir != null) {
            Descriptor root = DescriptorFactory.buildDescriptor(tmpDir);
            if (root != null) {
                if (root.getComponent() == null) {
                    throw new DeploymentException("JBI descriptor is not a component descriptor");
                }
                install(tmpDir, props, root, autoStart);
            } else {
                throw new DeploymentException("Could not find JBI descriptor");
            }
        } else {
            throw new DeploymentException("Could not find JBI descriptor");
        }
    }

    /**
     * Install an archive
     * 
     * @param tmpDir
     * @param root
     * @param autoStart
     * @throws DeploymentException
     */
    protected void install(File tmpDir, Properties props, Descriptor root, boolean autoStart) throws DeploymentException {
        LOGGER.debug("Going to install component from {}", tmpDir.getPath());
        if (root.getComponent() != null) {
            String componentName = root.getComponent().getIdentification().getName();
            if (installers.containsKey(componentName)) {
                throw new DeploymentException("Component " + componentName + " is already installed");
            }
            InstallerMBeanImpl installer = doInstallArchive(tmpDir, root);
            if (installer != null) {
                try {
                    if (props != null && props.size() > 0) {
                        ObjectName on = installer.getInstallerConfigurationMBean();
                        if (on == null) {
                            LOGGER.warn("Could not find installation configuration MBean. Installation properties will be ignored.");
                        } else {
                            MBeanServer mbs = managementContext.getMBeanServer();
                            for (Iterator it = props.keySet().iterator(); it.hasNext();) {
                                String key = (String) it.next();
                                String val = props.getProperty(key);
                                try {
                                    mbs.setAttribute(on, new Attribute(key, val));
                                } catch (JMException e) {
                                    throw new DeploymentException("Could not set installation property: (" + key + " = " + val, e);
                                }
                            }
                        }
                    }
                    installer.install();
                } catch (JBIException e) {
                    throw new DeploymentException(e);
                }
                if (autoStart) {
                    try {
                        ComponentMBeanImpl lcc = container.getComponent(componentName);
                        if (lcc != null) {
                            lcc.start();
                        } else {
                            LOGGER.warn("No ComponentConnector found for Component {}", componentName);
                        }
                    } catch (JBIException e) {
                        String errStr = "Failed to start Component: " + componentName;
                        LOGGER.error(errStr, e);
                        throw new DeploymentException(e);
                    }
                }
                LOGGER.debug("Registering installer as loaded for component {}", componentName);
                installers.put(componentName, installer);
            }
        }
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph = helper.addOperation(getObjectToManage(), "loadNewInstaller", 1, "load a new Installer ");
        ph.setDescription(0, "installJarURL", "URL locating the install Jar");
        ph = helper.addOperation(getObjectToManage(), "loadInstaller", 1, "load installer for a previously installed component");
        ph.setDescription(0, "componentName", "Name of the Component");
        ph = helper.addOperation(getObjectToManage(), "unloadInstaller", 2, "unload an installer");
        ph.setDescription(0, "componentName", "Name of the Component");
        ph.setDescription(1, "isToBeDeleted", "true if component is to be deleted");
        ph = helper.addOperation(getObjectToManage(), "installSharedLibrary", 1, "Install a shared library jar");
        ph.setDescription(0, "sharedLibURI", "URI for the jar to be installed");
        ph = helper.addOperation(getObjectToManage(), "uninstallSharedLibrary", 1, "Uninstall a shared library jar");
        ph.setDescription(0, "sharedLibName", "name of the shared library");
        ph = helper.addOperation(getObjectToManage(), "install", 1, "install and deplot an archive");
        ph.setDescription(0, "location", "location of archive");
        ph = helper.addOperation(getObjectToManage(), "install", 2, "install and deplot an archive");
        ph.setDescription(0, "location", "location of archive");
        ph.setDescription(1, "autostart", "automatically start the Component");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

    protected InstallerMBeanImpl doInstallArchive(File tmpDirectory, Descriptor descriptor) throws DeploymentException {
        InstallerMBeanImpl installer = null;
        Component component = descriptor.getComponent();
        if (component != null) {
            installer = doInstallComponent(tmpDirectory, component);
        }
        return installer;
    }

    protected String doInstallSharedLibrary(File tmpDirectory, SharedLibrary descriptor) throws DeploymentException {
        String result = null;
        if (descriptor != null) {
            File installationDir = null;
            try {
                result = descriptor.getIdentification().getName();
                File rootDir = environmentContext.createSharedLibraryDirectory(result);
                installationDir = FileVersionUtil.getNewVersionDirectory(rootDir);
                if (!tmpDirectory.renameTo(installationDir)) {
                    throw new DeploymentException("Unable to rename " + tmpDirectory + " to " + installationDir);
                }
                LOGGER.debug("Moved {} to {}", tmpDirectory, installationDir);
                container.getRegistry().registerSharedLibrary(descriptor, installationDir);
            } catch (Exception e) {
                LOGGER.error("Deployment of Shared Library failed", e);
                // remove any files created for installation
                FileUtil.deleteFile(installationDir);
                throw new DeploymentException(e);
            } finally {
                FileUtil.deleteFile(tmpDirectory);
            }
        }
        return result;
    }

    protected InstallerMBeanImpl doInstallComponent(File tmpDirectory, Component descriptor) throws DeploymentException {
        // move archive to Component directory
        InstallerMBeanImpl result = null;
        String name = descriptor.getIdentification().getName();
        try {
            File oldInstallationDir = environmentContext.getComponentInstallationDir(name);
            // try and delete the old version ? - maybe should leave around ??
            if (!FileUtil.deleteFile(oldInstallationDir)) {
                LOGGER.warn("Failed to delete old installation directory: {}", oldInstallationDir.getPath());
            }
            File componentRoot = environmentContext.createComponentRootDir(name);
            // this will get the new one
            File installationDir = environmentContext.getNewComponentInstallationDir(name);
            tmpDirectory.renameTo(installationDir);
            LOGGER.debug("Moved {} to {}", tmpDirectory, installationDir);
            result = initializeInstaller(installationDir, componentRoot, descriptor);
            return result;
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    private InstallerMBeanImpl initializeInstaller(File installationDir, File componentRoot, 
                                                   Component descriptor) throws DeploymentException {
        InstallerMBeanImpl result = null;
        try {
            String name = descriptor.getIdentification().getName();
            InstallationContextImpl installationContext = new InstallationContextImpl(descriptor);
            installationContext.setInstall(true);
            installationContext.setInstallRoot(installationDir);
            // now build the ComponentContext
            ComponentContextImpl context = buildComponentContext(componentRoot, installationDir, name);
            installationContext.setContext(context);
            result = new InstallerMBeanImpl(container, installationContext);
            // create an MBean for the installer
            ObjectName objectName = managementContext.createCustomComponentMBeanName("Installer", name);
            result.setObjectName(objectName);
            managementContext.registerMBean(objectName, result, InstallerMBean.class, "standard installation controls for a Component");
        } catch (Throwable e) {
            LOGGER.error("Deployment of Component failed", e);
            // remove any files created for installation
            environmentContext.removeComponentRootDirectory(descriptor.getIdentification().getName());
            throw new DeploymentException(e);
        }
        return result;
    }

    protected void buildState() {
        buildSharedLibs();
        buildComponents();
    }

    /**
     * returns true if a shared library is already installed
     * 
     * @param name
     * @return true/false
     */
    protected boolean containsSharedLibrary(String name) {
        return container.getRegistry().getSharedLibrary(name) != null;
    }

    protected void buildSharedLibs() {
        // walk through shared libaries and add then to the ClassLoaderService
        File top = environmentContext.getSharedLibDir();
        if (top != null && top.exists() && top.isDirectory()) {
            // directory structure is sharedlibraries/<lib name>/version_x/stuff
            // ...
            File[] files = top.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory()) {
                        continue;
                    }
                    File dir = FileVersionUtil.getLatestVersionDirectory(files[i]);
                    if (dir == null) {
                        continue;
                    }
                    Descriptor root = DescriptorFactory.buildDescriptor(dir);
                    if (root == null) {
                        continue;
                    }
                    SharedLibrary sl = root.getSharedLibrary();
                    if (sl == null) {
                        continue;
                    }
                    try {
                        container.getRegistry().registerSharedLibrary(sl, dir);
                    } catch (Exception e) {
                        LOGGER.error("Failed to initialize sharted library", e);
                    }
                }
            }
        }
    }

    protected void buildComponents() {
        // walk through components and add then to the ClassLoaderService
        File top = environmentContext.getComponentsDir();
        if (top != null && top.exists() && top.isDirectory()) {
            // directory structure is components/<component name>/installation
            // ...
            File[] files = top.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory()) {
                        continue;
                    }
                    final File directory = files[i];
                    try {
                        buildComponent(directory);
                    } catch (DeploymentException e) {
                        LOGGER.error("Could not build Component: {}", directory.getName(), e);
                        LOGGER.warn("Deleting Component directory: {}", directory);
                        FileUtil.deleteFile(directory);
                    }
                }
            }
        }
    }

    protected void buildComponent(File componentDirectory) throws DeploymentException {
        LOGGER.debug("Building component from directory {}" + componentDirectory.getPath());
        try {
            String componentName = componentDirectory.getName();
            ComponentEnvironment env = container.getEnvironmentContext().getComponentEnvironment(componentName);
            if (!env.getStateFile().exists()) {
                LOGGER.debug("Removing directory {} as component has not been installed (there is not state file)",
                        componentDirectory.getPath());
                // An installer has been created but the component has not been
                // installed
                // So remove it
                FileUtil.deleteFile(componentDirectory);
            } else {
                InstallerMBeanImpl installer = createInstaller(componentName);
                installer.activateComponent();
                nonLoadedInstallers.put(componentName, installer);
                LOGGER.debug("Registering installer as loaded for component {}", componentName);
            }
        } catch (Throwable e) {
            LOGGER.error("Failed to deploy component: {}", componentDirectory.getName(), e);
            throw new DeploymentException(e);
        }
    }

    protected ComponentContextImpl buildComponentContext(File componentRoot, File installRoot, String name) throws IOException {
        ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name);
        ComponentContextImpl context = new ComponentContextImpl(container, cns);
        ComponentEnvironment env = new ComponentEnvironment();
        FileUtil.buildDirectory(componentRoot);
        File privateWorkspace = environmentContext.createWorkspaceDirectory(name);
        env.setWorkspaceRoot(privateWorkspace);
        env.setComponentRoot(componentRoot);
        env.setInstallRoot(installRoot);
        context.setEnvironment(env);
        return context;
    }

}
