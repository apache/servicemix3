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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;

import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.jbi.util.FileVersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holder for environment information
 * 
 * <component-name> (component root dir)
 *   |-> version_X (versionned dir)
 *   \-> workspace (workspace dir)
 *   
 * ServiceAssembly root
 *   \-> version_X (versionned dir)
 *     |-> install (unzip dir)
 *     \-> sus (service units dir)
 *       |-> <component-name>
 *         |-> <service-unit-name>
 * 
 * @version $Revision$
 */
public class EnvironmentContext extends BaseSystemService implements EnvironmentContextMBean {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentContext.class);

    private File jbiRootDir;
    private File componentsDir;
    private File installationDir;
    private File deploymentDir;
    private File sharedLibDir;
    private File serviceAssembliesDir;
    private File tmpDir;
    private Map envMap = new ConcurrentHashMap();
    private AtomicBoolean started = new AtomicBoolean(false);

    /**
     * @return the current version of servicemix
     */
    public static String getVersion() {
        String answer = null;
        Package p = Package.getPackage("org.apache.servicemix");
        if (p != null) {
            answer = p.getImplementationVersion();
        }
        return answer;
    }

    /**
     * Get Description
     * @return description
     */
    public String getDescription() {
        return "Manages Environment for the Container";
    }

    /**
     * @return Returns the componentsDir.
     */
    public File getComponentsDir() {
        return componentsDir;
    }

    /**
     * @return Returns the installationDir.
     */
    public File getInstallationDir() {
        return installationDir;
    }

    /**
     * Set the installationDir - rge default location
     * is root/<container name>/installation
     * @param installationDir
     */
    public void setInstallationDir(File installationDir) {
        this.installationDir = installationDir;
    }

    /**
     * @return Returns the deploymentDir.
     */
    public File getDeploymentDir() {
        return deploymentDir;
    }

    /**
     * @param deploymentDir The deploymentDir to set.
     */
    public void setDeploymentDir(File deploymentDir) {
        this.deploymentDir = deploymentDir;
    }

    /**
     * 
     * @return Returns the shared library directory
     */
    public File getSharedLibDir() {
        return sharedLibDir;
    }

    /**
     * @return Returns the tmpDir
     */
    public File getTmpDir() {
        if (tmpDir != null) {
            FileUtil.buildDirectory(tmpDir);
        }
        return tmpDir;
    }

    /**
     * @return Returns service asseblies directory
     */
    public File getServiceAssembliesDir() {
        return serviceAssembliesDir;
    }

    /**
     * Initialize the Environment
     * 
     * @param container
     * @param rootDirPath
     * @exception javax.jbi.JBIException if the root directory informed could not be created or it is not a directory
     */
    public void init(JBIContainer container, String rootDirPath) throws JBIException {
        super.init(container);
        jbiRootDir = new File(rootDirPath);
        buildDirectoryStructure();
    }

    protected Class getServiceMBean() {
        return EnvironmentContextMBean.class;
    }

    /**
     * Start the item.
     * 
     * @exception javax.jbi.JBIException if the item fails to start.
     */
    public void start() throws javax.jbi.JBIException {
        if (started.compareAndSet(false, true)) {
            super.start();
        }
    }

    /**
     * Stop the item. This suspends current messaging activities.
     * 
     * @exception javax.jbi.JBIException if the item fails to stop.
     */
    public void stop() throws javax.jbi.JBIException {
        if (started.compareAndSet(true, false)) {
            super.stop();
        }
    }

    /**
     * Shut down the item. The releases resources, preparatory to uninstallation.
     * 
     * @exception javax.jbi.JBIException if the item fails to shut down.
     */
    public void shutDown() throws javax.jbi.JBIException {
        super.shutDown();
        envMap.clear();
        container.getManagementContext().unregisterMBean(this);
    }

    /**
     * register the ComponentConnector
     * 
     * @param connector
     * @return the CompponentEnvironment
     * @throws JBIException
     */
    public ComponentEnvironment registerComponent(ComponentMBeanImpl connector) throws JBIException {
        return registerComponent(null, connector);
    }

    /**
     * register the ComponentConnector
     * 
     * @param connector
     * @return the CompponentEnvironment
     * @throws JBIException
     */
    public ComponentEnvironment registerComponent(ComponentEnvironment result, ComponentMBeanImpl connector) throws JBIException {
        if (result == null) {
            result = new ComponentEnvironment();
        }
        if (!connector.isPojo()) {
            if (container.isEmbedded()) {
                throw new JBIException("JBI component can not be installed in embedded mode");
            }
            // add workspace root and stats root ..
            try {
                String name = connector.getComponentNameSpace().getName();
                if (result.getComponentRoot() == null) {
                    File componentRoot = getComponentRootDir(name);
                    FileUtil.buildDirectory(componentRoot);
                    result.setComponentRoot(componentRoot);
                }
                if (result.getWorkspaceRoot() == null) {
                    File privateWorkspace = createWorkspaceDirectory(name);
                    result.setWorkspaceRoot(privateWorkspace);
                }
                if (result.getStateFile() == null) {
                    File stateFile = FileUtil.getDirectoryPath(result.getComponentRoot(), "state.xml");
                    result.setStateFile(stateFile);
                }
            } catch (IOException e) {
                throw new JBIException(e);
            }
        }
        result.setLocalConnector(connector);
        envMap.put(connector, result);
        return result;
    }

    /**
     * Get root directory for a Component
     * 
     * @param componentName
     * @return directory for deployment/workspace etc
     * @throws IOException
     */
    public File getComponentRootDir(String componentName) {
        if (getComponentsDir() == null) {
            return null;
        }
        return FileUtil.getDirectoryPath(getComponentsDir(), componentName);
    }

    /**
     * Create root directory for a Component
     * 
     * @param componentName
     * @return directory for deployment/workspace etc
     * @throws IOException
     */
    public File createComponentRootDir(String componentName) throws IOException {
        if (getComponentsDir() == null) {
            return null;
        }
        return FileUtil.getDirectoryPath(getComponentsDir(), componentName);
    }

    /**
     * Get a new versionned directory for installation
     * 
     * @param componentName
     * @return
     * @throws IOException
     */
    public File getNewComponentInstallationDir(String componentName) throws IOException {
        File result = getComponentRootDir(componentName);
        // get new version dir
        return FileVersionUtil.getNewVersionDirectory(result);
    }

    /**
     * Create installation directory for a Component
     * 
     * @param componentName
     * @return directory to deploy in
     * @throws IOException
     */
    public File getComponentInstallationDir(String componentName) throws IOException {
        File result = getComponentRootDir(componentName);
        // get the version directory
        return FileVersionUtil.getLatestVersionDirectory(result);
    }

    public ComponentEnvironment getNewComponentEnvironment(String compName) throws IOException {
        File rootDir = FileUtil.getDirectoryPath(getComponentsDir(), compName);
        File instDir = FileVersionUtil.getNewVersionDirectory(rootDir);
        File workDir = FileUtil.getDirectoryPath(rootDir, "workspace");
        File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
        ComponentEnvironment env = new ComponentEnvironment();
        env.setComponentRoot(rootDir);
        env.setInstallRoot(instDir);
        env.setWorkspaceRoot(workDir);
        env.setStateFile(stateFile);
        return env;
    }

    public ComponentEnvironment getComponentEnvironment(String compName) throws IOException {
        File rootDir = FileUtil.getDirectoryPath(getComponentsDir(), compName);
        File instDir = FileVersionUtil.getLatestVersionDirectory(rootDir);
        File workDir = FileUtil.getDirectoryPath(rootDir, "workspace");
        File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
        ComponentEnvironment env = new ComponentEnvironment();
        env.setComponentRoot(rootDir);
        env.setInstallRoot(instDir);
        env.setWorkspaceRoot(workDir);
        env.setStateFile(stateFile);
        return env;
    }

    public ServiceAssemblyEnvironment getNewServiceAssemblyEnvironment(String saName) throws IOException {
        File rootDir = FileUtil.getDirectoryPath(getServiceAssembliesDir(), saName);
        File versDir = FileVersionUtil.getNewVersionDirectory(rootDir);
        File instDir = FileUtil.getDirectoryPath(versDir, "install");
        File susDir = FileUtil.getDirectoryPath(versDir, "sus");
        File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
        ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
        env.setRootDir(rootDir);
        env.setInstallDir(instDir);
        env.setSusDir(susDir);
        env.setStateFile(stateFile);
        return env;
    }

    public ServiceAssemblyEnvironment getServiceAssemblyEnvironment(String saName) {
        File rootDir = FileUtil.getDirectoryPath(getServiceAssembliesDir(), saName);
        File versDir = FileVersionUtil.getLatestVersionDirectory(rootDir);
        File instDir = FileUtil.getDirectoryPath(versDir, "install");
        File susDir = FileUtil.getDirectoryPath(versDir, "sus");
        File stateFile = FileUtil.getDirectoryPath(rootDir, "state.xml");
        ServiceAssemblyEnvironment env = new ServiceAssemblyEnvironment();
        env.setRootDir(rootDir);
        env.setInstallDir(instDir);
        env.setSusDir(susDir);
        env.setStateFile(stateFile);
        return env;
    }

    /**
     * Create workspace directory for a Component
     * 
     * @param componentName
     * @return directory workspace
     * @throws IOException
     */
    public File createWorkspaceDirectory(String componentName) throws IOException {
        File result = FileUtil.getDirectoryPath(getComponentsDir(), componentName);
        result = FileUtil.getDirectoryPath(result, "workspace");
        FileUtil.buildDirectory(result);
        return result;
    }

    /**
     * deregister the ComponentConnector
     * 
     * @param connector
     */
    public void unreregister(ComponentMBeanImpl connector) {
        this.envMap.remove(connector);
    }

    /**
     * Remove the Component root directory from the local file system
     * 
     * @param componentName
     */
    public void removeComponentRootDirectory(String componentName) {
        File file = getComponentRootDir(componentName);
        if (file != null) {
            if (!FileUtil.deleteFile(file)) {
                LOGGER.warn("Failed to remove directory structure for component [version]: {} [{}]", componentName, file.getName());
            } else {
                LOGGER.info("Removed directory structure for component [version]: {} [{}]", componentName, file.getName());
            }
        }
    }

    /**
     * create a shared library directory
     * 
     * @param name
     * @return directory
     * @throws IOException
     */
    public File createSharedLibraryDirectory(String name) {
        File result = FileUtil.getDirectoryPath(getSharedLibDir(), name);
        FileUtil.buildDirectory(result);
        return result;
    }

    /**
     * remove shared library directory
     * @param name
     * @throws IOException
     */
    public void removeSharedLibraryDirectory(String name) {
        File result = FileUtil.getDirectoryPath(getSharedLibDir(), name);
        FileUtil.deleteFile(result);
    }

    private void buildDirectoryStructure() throws JBIException {
        // We want ServiceMix to be able to run embedded
        // so do not create the directory structure if the root does not exist
        if (container.isEmbedded()) {
            return;
        }
        try {
            jbiRootDir = jbiRootDir.getCanonicalFile();
            if (!jbiRootDir.exists()) {
                if (!jbiRootDir.mkdirs()) {
                    throw new JBIException("Directory could not be created: " + jbiRootDir.getCanonicalFile());
                }
            } else if (!jbiRootDir.isDirectory()) {
                throw new JBIException("Not a directory: " + jbiRootDir.getCanonicalFile());
            }
            if (installationDir == null) {
                installationDir = FileUtil.getDirectoryPath(jbiRootDir, "install");
            }
            installationDir = installationDir.getCanonicalFile();
            if (deploymentDir == null) {
                deploymentDir = FileUtil.getDirectoryPath(jbiRootDir, "deploy");
            }
            deploymentDir = deploymentDir.getCanonicalFile();
            componentsDir = FileUtil.getDirectoryPath(jbiRootDir, "components").getCanonicalFile();
            tmpDir = FileUtil.getDirectoryPath(jbiRootDir, "tmp").getCanonicalFile();
            sharedLibDir = FileUtil.getDirectoryPath(jbiRootDir, "sharedlibs").getCanonicalFile();
            serviceAssembliesDir = FileUtil.getDirectoryPath(jbiRootDir, "service-assemblies").getCanonicalFile();
            //actually create the sub directories
            FileUtil.buildDirectory(installationDir);
            FileUtil.buildDirectory(deploymentDir);
            FileUtil.buildDirectory(componentsDir);
            FileUtil.buildDirectory(tmpDir);
            FileUtil.buildDirectory(sharedLibDir);
            FileUtil.buildDirectory(serviceAssembliesDir);
        } catch (IOException e) {
            throw new JBIException(e);
        }
    }

    public File getJbiRootDir() {
        return jbiRootDir;
    }

}
