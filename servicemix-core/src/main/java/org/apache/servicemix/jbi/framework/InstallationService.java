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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
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
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.ComponentEnvironment;
import org.apache.servicemix.jbi.container.EnvironmentContext;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.loaders.ClassLoaderService;
import org.apache.servicemix.jbi.loaders.InstallationClassLoader;
import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.ManagementContext;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.management.ParameterHelper;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.servicemix.schemas.deployment.ClassPath;
import org.apache.servicemix.schemas.deployment.Component;
import org.apache.servicemix.schemas.deployment.Descriptor;
import org.apache.servicemix.schemas.deployment.DescriptorFactory;
import org.apache.servicemix.schemas.deployment.Descriptor.SharedLibrary;
import org.w3c.dom.DocumentFragment;

/**
 * Installation Service - installs/uninstalls archives
 * 
 * @version $Revision$
 */
public class InstallationService extends BaseSystemService implements InstallationServiceMBean {
    private static final Log log=LogFactory.getLog(InstallationService.class);
    private EnvironmentContext environmentContext;
    private ManagementContext managementContext;
    private ClassLoaderService classLoaderService=new ClassLoaderService();
    private Map<String, InstallerMBeanImpl> installers = new ConcurrentHashMap<String, InstallerMBeanImpl>();
    private Map<String, InstallerMBeanImpl> nonLoadedInstallers = new ConcurrentHashMap<String, InstallerMBeanImpl>();

    /**
     * Get Description
     * 
     * @return description of this item
     */
    public String getDescription(){
        return "installs/uninstalls Components";
    }

    /**
     * Load the installer for a new component from a component installation package.
     * 
     * @param installJarURL -
     *            URL locating a jar file containing a JBI Installable Component.
     * @return - the JMX ObjectName of the InstallerMBean loaded from installJarURL.
     */
    public synchronized ObjectName loadNewInstaller(String installJarURL){
        try{
            ObjectName result=null;
            if(log.isDebugEnabled()){
                log.debug("Loading new installer from "+installJarURL);
            }
            File tmpDir=AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(),installJarURL);
            if(tmpDir!=null){
                Descriptor root=DescriptorFactory.buildDescriptor(tmpDir);
                if(root!=null&&root.getComponent()!=null){
                    String componentName=root.getComponent().getIdentification().getName();
                    if(!installers.containsKey(componentName)){
                        InstallerMBeanImpl installer=doInstallArchive(tmpDir,root);
                        if(installer!=null){
                            result=installer.getObjectName();
                            installers.put(componentName,installer);
                        }
                    }else{
                        throw new RuntimeException("An installer already exists for "+componentName);
                    }
                }else{
                    throw new RuntimeException("Could not find Component from: "+installJarURL);
                }
            }else{
                throw new RuntimeException("location: "+installJarURL+" isn't valid");
            }
            return result;
        }catch(Throwable t){
            log.error("Deployment failed",t);
            if(t instanceof Error){
                throw (Error) t;
            }
            if(t instanceof RuntimeException){
                throw (RuntimeException) t;
            }else{
                throw new RuntimeException("Deployment failed: "+t.getMessage());
            }
        }
    }

    /**
     * Load the InstallerMBean for a previously installed component.
     * 
     * @param aComponentName -
     *            the component name identifying the installer to load.
     * @return - the JMX ObjectName of the InstallerMBean loaded from an existing installation context.
     */
    public ObjectName loadInstaller(String aComponentName) {
        InstallerMBeanImpl installer = (InstallerMBeanImpl) installers.get(aComponentName);
        if (installer == null) {
            installer = (InstallerMBeanImpl) nonLoadedInstallers.get(aComponentName);
            if (installer != null) {
                try {
                    // create an MBean for the installer
                    ObjectName objectName = managementContext.createCustomComponentMBeanName("Installer", aComponentName);
                    installer.setObjectName(objectName);
                    managementContext.registerMBean(objectName, installer,
                            InstallerMBean.class,
                            "standard installation controls for a Component");
                } catch (Exception e) {
                    throw new RuntimeException("Could not load installer", e);
                }
                return installer.getObjectName();
            }
        }
        return null;
    }
    
    private InstallerMBeanImpl createInstaller(String componentName) throws IOException, DeploymentException {
        File installationDir = environmentContext.getComponentInstallationDir(componentName);
        Descriptor root = DescriptorFactory.buildDescriptor(installationDir);
        Component descriptor = root.getComponent();

        String name = descriptor.getIdentification().getName();
        InstallationContextImpl installationContext = new InstallationContextImpl();
        installationContext.setInstall(false);
        installationContext.setComponentName(name);
        installationContext.setComponentDescription(descriptor.getIdentification().getDescription());
        installationContext.setInstallRoot(installationDir);
        installationContext.setComponentClassName(descriptor.getComponentClassName().getContent());
        ClassPath cp = descriptor.getComponentClassPath();
        if (cp != null) {
            installationContext.setClassPathElements(cp.getPathElement());
        }
        // now build the ComponentContext
        File componentRoot = environmentContext.getComponentRootDir(componentName);
        ComponentContextImpl context = buildComponentContext(componentRoot, componentName);
        installationContext.setContext(context);
        DocumentFragment desc = DescriptorFactory.getDescriptorExtension(descriptor);
        if (desc != null) {
            installationContext.setDescriptorExtension(desc);
        }
        installationContext.setBinding(DescriptorFactory.isBindingComponent(descriptor));
        installationContext.setEngine(DescriptorFactory.isServiceEngine(descriptor));
        // now we must initialize the boot strap class
        String bootstrapClassName = descriptor.getBootstrapClassName();
        ClassPath bootStrapClassPath = descriptor.getBootstrapClassPath();
        InstallationClassLoader bootstrapLoader = null;
        if (bootstrapClassName != null && bootstrapClassName.length() > 0) {
            boolean parentFirst = DescriptorFactory.isBCLParentFirst(descriptor);
            bootstrapLoader = classLoaderService.buildClassLoader(
                    installationDir, bootStrapClassPath.getPathElement(), parentFirst);
        }
        List<Component.SharedLibrary> lists = descriptor.getSharedLibraryList();
        String componentClassName = descriptor.getComponentClassName().getContent();
        ClassPath componentClassPath = descriptor.getComponentClassPath();
        boolean parentFirst = DescriptorFactory.isCCLParentFirst(descriptor);
        ClassLoader componentClassLoader = classLoaderService.buildClassLoader(
        			installationDir, componentClassPath.getPathElement(), parentFirst, lists);
        InstallerMBeanImpl installer = new InstallerMBeanImpl(container,
                installationContext, componentClassLoader,
                componentClassName, bootstrapLoader,
                bootstrapClassName, true);
        return installer;
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
        boolean result=false;
        try {
            container.getBroker().suspend();
            InstallerMBeanImpl installer = (InstallerMBeanImpl) installers.remove(componentName);
            result = installer != null;
            if(result) {
                container.getManagementContext().unregisterMBean(installer);
                if (isToBeDeleted) {
                    installer.uninstall();
                } else {
                    nonLoadedInstallers.put(componentName, installer);
                }
            }
        } catch(JBIException e) {
            String errStr = "Problem shutting down Component: " + componentName;
            log.error(errStr, e);
        } finally {
            container.getBroker().resume();
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
    public String installSharedLibrary(String aSharedLibURI){
        String result="";
        try{
            File tmpDir=AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(),aSharedLibURI);
            if(tmpDir!=null){
                Descriptor root=DescriptorFactory.buildDescriptor(tmpDir);
                SharedLibrary sl = root.getSharedLibrary();
                if(sl!=null){
                    result=doInstallSharedLibrary(tmpDir,sl);
                }
            }else{
                log.warn("location: "+aSharedLibURI+" isn't valid");
            }
        }catch(DeploymentException e){
            log.error("Deployment failed",e);
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
        classLoaderService.removeSharedLibrary(aSharedLibName);
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
     * @throws DeploymentException
     */
    public void install(String location) throws DeploymentException {
        install(location,false);
    }

    public void install(String location, Properties props) throws DeploymentException {
        install(location, props, false);
    }

    /**
     * Install an archive
     * 
     * @param location
     * @param autoStart
     * @throws DeploymentException
     */
    public void install(String location, boolean autoStart) throws DeploymentException {
        install(location, null, autoStart);
    }
    
    public void install(String location, Properties props, boolean autoStart) throws DeploymentException {
        File tmpDir=AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(),location);
        if(tmpDir!=null){
            Descriptor root=DescriptorFactory.buildDescriptor(tmpDir);
            if(root!=null){
                install(tmpDir, props, root, autoStart);
            }else{
                log.error("Could not find Descriptor from: "+location);
            }
        }else{
            log.warn("location: "+location+" isn't valid");
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
    protected void install(File tmpDir, Descriptor root, boolean autoStart) throws DeploymentException {
        install(tmpDir, null, root, autoStart);
    }

    protected void install(File tmpDir, Properties props, Descriptor root, boolean autoStart) throws DeploymentException {
        if (root.getComponent() != null) {
            String componentName = root.getComponent().getIdentification().getName();
            if (!installers.containsKey(componentName)) {
                InstallerMBeanImpl installer = doInstallArchive(tmpDir,root);
                if (installer != null) {
                    try {
                        if (props != null && props.size() > 0) {
                            ObjectName on = installer.getInstallerConfigurationMBean();
                            if (on == null ) {
                                log.warn("Could not find installation configuration MBean. Installation properties will be ignored.");
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
                    } catch(JBIException e) {
                        throw new DeploymentException(e);
                    }
                    if (autoStart) {
                        try{
                            ComponentMBeanImpl lcc = container.getComponent(componentName);
                            if (lcc != null) {
                                lcc.start();
                            }else{
                                log.warn("No ComponentConnector found for Component "+componentName);
                            }
                        }catch(JBIException e){
                            String errStr="Failed to start Component: "+componentName;
                            log.error(errStr,e);
                            throw new DeploymentException(e);
                        }
                    }
                    installers.put(componentName,installer);
                }
            }else{
                log.warn("Component "+componentName+" is already installed");
            }
        }
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException{
        OperationInfoHelper helper=new OperationInfoHelper();
        ParameterHelper ph=helper.addOperation(getObjectToManage(),"loadNewInstaller",1,"load a new Installer ");
        ph.setDescription(0,"installJarURL","URL locating the install Jar");
        ph=helper.addOperation(getObjectToManage(),"loadInstaller",1,
                        "load installer for a previously installed component");
        ph.setDescription(0,"componentName","Name of the Component");
        ph=helper.addOperation(getObjectToManage(),"unloadInstaller",2,"unload an installer");
        ph.setDescription(0,"componentName","Name of the Component");
        ph.setDescription(1,"isToBeDeleted","true if component is to be deleted");
        ph=helper.addOperation(getObjectToManage(),"installSharedLibrary",1,"Install a shared library jar");
        ph.setDescription(0,"sharedLibURI","URI for the jar to be installed");
        ph=helper.addOperation(getObjectToManage(),"uninstallSharedLibrary",1,"Uninstall a shared library jar");
        ph.setDescription(0,"sharedLibName","name of the shared library");
        ph=helper.addOperation(getObjectToManage(),"install",1,"install and deplot an archive");
        ph.setDescription(0,"location","location of archive");
        ph=helper.addOperation(getObjectToManage(),"install",2,"install and deplot an archive");
        ph.setDescription(0,"location","location of archive");
        ph.setDescription(1,"autostart","automatically start the Component");
        return OperationInfoHelper.join(super.getOperationInfos(),helper.getOperationInfos());
    }

    protected InstallerMBeanImpl doInstallArchive(File tmpDirectory,Descriptor descriptor) throws DeploymentException{
        InstallerMBeanImpl installer=null;
        Component component=descriptor.getComponent();
        if(component!=null){
            installer=doInstallComponent(tmpDirectory,component);
        }
        return installer;
    }

    protected String doInstallSharedLibrary(File tmpDirectory,SharedLibrary descriptor) throws DeploymentException{
        String result=null;
        if(descriptor!=null){
            try{
                result=descriptor.getIdentification().getName();
                File installationDir=environmentContext.createSharedLibraryDirectory(result);
                if(installationDir.exists()){
                    FileUtil.deleteFile(installationDir);
                }
                if(!tmpDirectory.renameTo(installationDir)){
                    throw new DeploymentException("Unable to rename " + tmpDirectory + " to " + installationDir);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Moved " + tmpDirectory + " to " + installationDir);
                }
                classLoaderService.addSharedLibrary(installationDir, descriptor);
            }catch(IOException e){
                log.error("Deployment of Shared Library failed",e);
                // remove any files created for installation
                environmentContext.removeComponentRootDirectory(descriptor.getIdentification().getName());
                throw new DeploymentException(e);
            }
        }
        return result;
    }

    protected InstallerMBeanImpl doInstallComponent(File tmpDirectory,Component descriptor) throws DeploymentException{
        // move archive to Component directory
        InstallerMBeanImpl result=null;
        String name=descriptor.getIdentification().getName();
        try{
            File oldInstallationDir=environmentContext.getComponentInstallationDir(name);
            // try and delete the old version ? - maybe should leave around ??
            if(!FileUtil.deleteFile(oldInstallationDir)){
                log.warn("Failed to delete old installation directory: " + oldInstallationDir.getPath());
            }
            File componentRoot=environmentContext.createComponentRootDir(name);
            // this will get the new one
            File installationDir=environmentContext.getNewComponentInstallationDir(name);
            tmpDirectory.renameTo(installationDir);
            if (log.isDebugEnabled()) {
                log.debug("Moved " + tmpDirectory + " to " + installationDir);
            }
            result=initializeInstaller(installationDir,componentRoot,descriptor);
            return result;
        }catch(IOException e){
            throw new DeploymentException(e);
        }
    }

    private InstallerMBeanImpl initializeInstaller(File installationDir,File componentRoot,Component descriptor)
                    throws DeploymentException{
        InstallerMBeanImpl result=null;
        try{
            String name=descriptor.getIdentification().getName();
            InstallationContextImpl installationContext=new InstallationContextImpl();
            installationContext.setInstall(true);
            installationContext.setComponentName(name);
            installationContext.setComponentDescription(descriptor.getIdentification().getDescription());
            installationContext.setInstallRoot(installationDir);
            installationContext.setComponentClassName(descriptor.getComponentClassName().getContent());
            ClassPath cp=descriptor.getComponentClassPath();
            if(cp!=null){
                installationContext.setClassPathElements(cp.getPathElement());
            }
            // now build the ComponentContext
            installationContext.setContext(buildComponentContext(componentRoot,name));
            DocumentFragment desc = DescriptorFactory.getDescriptorExtension(descriptor);
            if (desc != null) {
                installationContext.setDescriptorExtension(desc);
            }
            installationContext.setBinding(DescriptorFactory.isBindingComponent(descriptor));
            installationContext.setEngine(DescriptorFactory.isServiceEngine(descriptor));
            // now we must initialize the boot strap class
            String bootstrapClassName=descriptor.getBootstrapClassName();
            ClassPath bootStrapClassPath=descriptor.getBootstrapClassPath();
            InstallationClassLoader bootstrapLoader=null;
            if (bootstrapClassName != null && bootstrapClassName.length() > 0) {
                boolean parentFirst = DescriptorFactory.isBCLParentFirst(descriptor);
                bootstrapLoader = classLoaderService.buildClassLoader(
                		installationDir, bootStrapClassPath.getPathElement(), parentFirst);
            }
            List<Component.SharedLibrary> lists = descriptor.getSharedLibraryList();
            String componentClassName=descriptor.getComponentClassName().getContent();
            ClassPath componentClassPath=descriptor.getComponentClassPath();
            boolean parentFirst = DescriptorFactory.isCCLParentFirst(descriptor);
            ClassLoader componentClassLoader=classLoaderService.buildClassLoader(
            			installationDir, componentClassPath.getPathElement(), parentFirst, lists);
            result=new InstallerMBeanImpl(container,installationContext,componentClassLoader,componentClassName,
                            bootstrapLoader,bootstrapClassName, false);
            // create an MBean for the installer
            ObjectName objectName = managementContext.createCustomComponentMBeanName("Installer", name);
            result.setObjectName(objectName);
            managementContext.registerMBean(objectName,result,InstallerMBean.class,
                            "standard installation controls for a Component");
        }catch(Throwable e){
            log.error("Deployment of Component failed",e);
            // remove any files created for installation
            environmentContext.removeComponentRootDirectory(descriptor.getIdentification().getName());
            throw new DeploymentException(e);
        }
        return result;
    }

    protected void buildState(){
        buildSharedLibs();
        buildComponents();
    }

    /**
     * returns true if a shared library is already installed
     * 
     * @param name
     * @return true/false
     */
    protected boolean containsSharedLibrary(String name){
        return classLoaderService.containsSharedLibrary(name);
    }

    protected void buildSharedLibs(){
        // walk through shared libaries and add then to the ClassLoaderService
        File top=environmentContext.getSharedLibDir();
        if(top!=null&&top.exists()&&top.isDirectory()){
            // directory structure is sharedlibraries/<lib name>stuff ...
            File[] files=top.listFiles();
            if(files!=null){
                for(int i=0;i<files.length;i++){
                    if(files[i].isDirectory()){
                        Descriptor root=DescriptorFactory.buildDescriptor(files[i]);
                        if(root!=null){
                            SharedLibrary sl=root.getSharedLibrary();
                            if(sl!=null){
                                try{
                                    classLoaderService.addSharedLibrary(files[i],sl);
                                }catch(MalformedURLException e){
                                    log.error("Failed to initialize sharted library",e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected void buildComponents(){
        // walk through components and add then to the ClassLoaderService
        File top=environmentContext.getComponentsDir();
        if(top!=null&&top.exists()&&top.isDirectory()){
            // directory structure is components/<component name>/installation ...
            File[] files=top.listFiles();
            if(files!=null){
                for(int i=0;i<files.length;i++){
                    if(files[i].isDirectory()){
                        final File directory=files[i];
                        try{
                            container.getWorkManager().doWork(new Work(){
                                public void release(){}

                                public void run(){
                                    try{
                                        buildComponent(directory);
                                    }catch(DeploymentException e){
                                        log.error("Could not build Component: "+directory.getName(),e);
                                        log.warn("Deleting Component directory: "+directory);
                                        FileUtil.deleteFile(directory);
                                    }
                                }
                            });
                        }catch(WorkException e){
                            log.error("Could not build Component: "+directory.getName(),e);
                            log.warn("Deleting Component directory: "+directory);
                            FileUtil.deleteFile(directory);
                        }
                    }
                }
            }
        }
    }

    protected void buildComponent(File componentDirectory)
            throws DeploymentException {
        try {
            String componentName = componentDirectory.getName();
            ComponentEnvironment env = container.getEnvironmentContext().getComponentEnvironment(componentName);
            if (!env.getStateFile().exists()) {
                // An installer has been created but the component has not been installed
                // So remove it
                FileUtil.deleteFile(componentDirectory);
            } else {
                InstallerMBeanImpl installer = createInstaller(componentName); 
                installer.activateComponent();
                nonLoadedInstallers.put(componentName, installer);
            }
        } catch (Throwable e) {
            log.error("Failed to deploy component: "
                    + componentDirectory.getName(), e);
            throw new DeploymentException(e);
        }
    }

    protected ComponentContextImpl buildComponentContext(File componentRoot,String name) throws IOException{
        ComponentNameSpace cns=new ComponentNameSpace(container.getName(),name);
        ComponentContextImpl context=new ComponentContextImpl(container,cns);
        ComponentEnvironment env=new ComponentEnvironment();
        FileUtil.buildDirectory(componentRoot);
        File privateWorkspace=environmentContext.createWorkspaceDirectory(name);
        env.setWorkspaceRoot(privateWorkspace);
        env.setComponentRoot(componentRoot);
        context.setEnvironment(env);
        return context;
    }

}
