/**
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package org.servicemix.jbi.framework;
import java.io.File;
import java.io.IOException;
import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.management.DeploymentServiceMBean;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.jbi.container.EnvironmentContext;
import org.servicemix.jbi.container.JBIContainer;
import org.servicemix.jbi.deployment.Connection;
import org.servicemix.jbi.deployment.Consumer;
import org.servicemix.jbi.deployment.Descriptor;
import org.servicemix.jbi.deployment.Provider;
import org.servicemix.jbi.deployment.ServiceAssembly;
import org.servicemix.jbi.deployment.ServiceUnit;
import org.servicemix.jbi.deployment.Target;
import org.servicemix.jbi.management.BaseLifeCycle;
import org.servicemix.jbi.management.OperationInfoHelper;
import org.servicemix.jbi.management.ParameterHelper;
import org.servicemix.jbi.servicedesc.InternalEndpoint;
import org.servicemix.jbi.util.FileUtil;

/**
 * The deployment service MBean allows administrative tools to manage service assembly deployments.
 * 
 * @version $Revision$
 */
public class DeploymentService extends BaseLifeCycle implements DeploymentServiceMBean {
    private static final Log log = LogFactory.getLog(DeploymentService.class);
    private JBIContainer container;
    private EnvironmentContext environmentContext;
    

    /**
     * Initialize the Service
     * 
     * @param container
     * @throws JBIException 
     * @throws DeploymentException
     */
    public void init(JBIContainer container) throws JBIException {
        this.container = container;
        this.environmentContext = container.getEnvironmentContext();
        buildState();
        container.getManagementContext().registerSystemService(this, DeploymentServiceMBean.class);
    }

    public void shutDown() throws JBIException {
        super.shutDown();
        container.getManagementContext().unregisterMBean(this);
    }
    
    /**
     * Get the description
     * 
     * @return description
     */
    public String getDescription() {
        return "Allows admin tools to manage service deployments";
    }

    /**
     * Deploys the given SA to the JBI environment.
     * 
     * @param saZipURL String containing the location of the Service Assembly zip file.
     * @return Result/Status of the SA deployment.
     * @throws Exception if complete deployment fails.
     */
    public String deploy(String saZipURL) throws Exception {
        try {
            String result = null;
            File tmpDir = AutoDeploymentService.unpackLocation(environmentContext.getTmpDir(), saZipURL);
            Descriptor root = AutoDeploymentService.buildDescriptor(tmpDir);
            ServiceAssembly sa = root.getServiceAssembly();
            if (sa != null) {
                result = deploy(tmpDir, sa);
            }
            else {
                throw new DeploymentException("Not an assembly: " + saZipURL);
            }
            return result;
        } catch (Exception e) {
            log.info("Unable to deploy assembly", e);
            throw e;
        }
    }

    /**
     * Deploy an SA
     * 
     * @param tmpDir
     * @param sa
     * @return result/status of the deployment
     * @throws Exception
     */
    protected String deploy(File tmpDir, ServiceAssembly sa) throws Exception {
        String result = "";
        String assemblyName = sa.getIdentification().getName();
        File oldSaDirectory = environmentContext.getSARootDirectory(assemblyName);
        FileUtil.deleteFile(oldSaDirectory);
        File saDirectory = environmentContext.createSARootDirectory(assemblyName);
        log.info(assemblyName + " Moving " + tmpDir.getAbsolutePath() + " to " + saDirectory.getAbsolutePath());
        
        if (tmpDir.renameTo(saDirectory)) {
            // move the assembly to a well-named holding area
            ServiceUnit[] sus = sa.getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                    if (i > 0) {
                        result += " ; ";
                    }
                    result += deployServiceUnit(saDirectory, sus[i]);
                }
            }
            buildConnections(sa);
            container.getRegistry().registerServiceAssembly(sa);
        }
        else {
            log.error("Failed to rename " + tmpDir + " TO " + saDirectory);
        }
        return result;
    }

    /**
     * Undeploys the given SA from the JBI environment.
     * 
     * @param saName name of the SA that has to be undeployed.
     * @return Result/Status of the SA undeployment.
     * @throws Exception if compelete undeployment fails.
     */
    public String undeploy(String saName) throws Exception {
        String result = null;
        ServiceAssembly sa = container.getRegistry().getServiceAssembly(saName);
        if (sa != null) {
            container.getRegistry().unregisterServiceAssembly(sa);
            String assemblyName = sa.getIdentification().getName();
            File saDirectory = environmentContext.getSARootDirectory(assemblyName);
            ServiceUnit[] sus = sa.getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                    undeployServiceUnit(sus[i]);
                }
            }
            FileUtil.deleteFile(saDirectory);
        }
        return result;
    }

    /**
     * Returns a list of Service Units that are currently deployed to the given component.
     * 
     * @param componentName name of the component.
     * @return List of deployed ASA Ids.
     */
    public String[] getDeployedServiceUnitList(String componentName)  {
        return container.getRegistry().getSADeployedServiceUnitList(componentName);
    }

    /**
     * Returns a list of Service Assemblies deployed to the JBI enviroment.
     * 
     * @return list of Service Assembly Name's.
     */
    public String[] getDeployedServiceAssemblies()  {
        return container.getRegistry().getDeployedServiceAssemblies();
    }

    /**
     * Returns the descriptor of the Service Assembly that was deployed to the JBI enviroment.
     * 
     * @param saName name of the service assembly.
     * @return descriptor of the Service Assembly.
     */
    public String getServiceAssemblyDescriptor(String saName)  {
        ServiceAssembly sa =  container.getRegistry().getServiceAssembly(saName);
        return sa != null ? sa.getIdentification().getDescription() : "";
    }

    /**
     * See if an Sa is already deployed
     * 
     * @param saName
     * @return true if already deployed
     */
    protected boolean isSaDeployed(String saName) {
        return container.getRegistry().getServiceAssembly(saName) != null;
    }

    /**
     * Returns a list of Service Assemblies that contain SUs for the given component.
     * 
     * @param componentName name of the component.
     * @return list of Service Assembly names.
     * @throws Exception if unable to retrieve service assembly list.
     */
    public String[] getDeployedServiceAssembliesForComponent(String componentName) throws Exception {
        return container.getRegistry().getDeployedServiceAssembliesForComponent(componentName);
    }

    /**
     * Returns a list of components(to which SUs are targeted for) in a Service Assembly.
     * 
     * @param saName name of the service assembly.
     * @return list of component names.
     * @throws Exception if unable to retrieve component list.
     */
    public String[] getComponentsForDeployedServiceAssembly(String saName) throws Exception {
        return container.getRegistry().getComponentsForDeployedServiceAssembly(saName);
    }

    /**
     * Returns a boolean value indicating whether the SU is currently deployed.
     * 
     * @param componentName - name of component.
     * @param suName - name of the Service Unit.
     * @return boolean value indicating whether the SU is currently deployed.
     * @throws Exception if unable to return status of service unit.
     */
    public boolean isDeployedServiceUnit(String componentName, String suName) throws Exception {
        return container.getRegistry().isSADeployedServiceUnit(componentName, suName);
    }

    /**
     * Returns a boolean value indicating whether the SU can be deployed to a component.
     * 
     * @param componentName - name of the component.
     * @return boolean value indicating whether the SU can be deployed.
     */
    public boolean canDeployToComponent(String componentName) {
        boolean result = false;
        result = container.getComponent(componentName) != null;
        return result;
    }

    /**
     * Starts the service assembly and puts it in RUNNING state.
     * 
     * @param serviceAssemblyName - name of the service assembly.
     * @return Result/Status of this operation.
     * @throws Exception if operation fails.
     */
    public String start(String serviceAssemblyName) throws Exception {
        return container.getRegistry().startServiceAssembly(serviceAssemblyName);
    }
    
    

    /**
     * Stops the service assembly and puts it in STOPPED state.
     * 
     * @param serviceAssemblyName - name of the service assembly.
     * @return Result/Status of this operation.
     * @throws Exception if operation fails.
     */
    public String stop(String serviceAssemblyName) throws Exception {
        return container.getRegistry().stopServiceAssembly(serviceAssemblyName);
    }

    /**
     * Shutdown the service assembly and puts it in SHUTDOWN state.
     * 
     * @param serviceAssemblyName - name of the service assembly.
     * @return Result/Status of this operation.
     * @throws Exception if operation fails.
     */
    public String shutDown(String serviceAssemblyName) throws Exception {
        return container.getRegistry().shutDownServiceAssembly(serviceAssemblyName);
    }

    /**
     * Returns the state of service assembly.
     * 
     * @param serviceAssemblyName - name of the service assembly.
     * @return State of the service assembly.
     * @throws Exception if operation fails.
     */
    public String getState(String serviceAssemblyName) throws Exception {
        return container.getRegistry().getServiceAssemblyState(serviceAssemblyName);
    }

    protected String getComponentName(ServiceAssembly sa) {
        String result = "";
        if (sa.getServiceUnits() != null && sa.getServiceUnits().length > 0) {
            result = sa.getServiceUnits()[0].getTarget().getComponentName();
        }
        return result;
    }

    protected String deployServiceUnit(File location, ServiceUnit su) throws DeploymentException {
        String result = null;
        String name = su.getIdentification().getName();
        Target target = su.getTarget();
        String componentName = target.getComponentName();
        String artifact = target.getArtifactsZip();
        try {
            File targetDir = environmentContext.getServiceUnitDirectory(componentName, name);
            // unpack the artifact
            if (artifact != null) {
                File artifactFile = new File(location, artifact);
                log.debug("Artifact is [" + artifactFile.getAbsolutePath() + "]");
                if (artifactFile.exists()) {
                    log.info("deployServiceUnit: unpack archive " + artifactFile + " to " + targetDir);
                    // FileUtil.moveFile(artifactFile, targetDir);
                    FileUtil.unpackArchive(artifactFile, targetDir);
                    // now get the component and give it a SA
                }
                else {
                    throw new DeploymentException("artifact: " + artifact + "(" + artifactFile.getAbsolutePath()
                            + ") doesn't exist");
                }
            }
            Component component = container.getComponent(componentName);
            if (component != null) {
                ServiceUnitManager sum = component.getServiceUnitManager();
                if (sum != null) {
                    result = sum.deploy(name, targetDir.getAbsolutePath());
                    sum.init(name, targetDir.getAbsolutePath());
                    // register active endpoints
                }
                else {
                    FileUtil.deleteFile(targetDir);
                    throw new DeploymentException("Component " + componentName + " doesn't have a ServiceUnitManager");
                }
            }
            else {
                FileUtil.deleteFile(targetDir);
                throw new DeploymentException("Component " + componentName + " doesn't exist");
            }
        }
        catch (IOException e) {
            log.error("Could not deploy ServiceUnit: " + name + " to component " + componentName, e);
            throw new DeploymentException(e);
        }
        log.info("Deployed ServiceUnit " + name + " to Component: " + componentName);
        return result;
    }

    protected void undeployServiceUnit(ServiceUnit su) throws DeploymentException {
        String name = su.getIdentification().getName();
        Target target = su.getTarget();
        String componentName = target.getComponentName();
        try {
            File targetDir = environmentContext.getServiceUnitDirectory(componentName, name);
            // unpack the artifact
            // now get the component and give it a SA
            Component component = container.getComponent(componentName);
            if (component != null) {
                ServiceUnitManager sum = component.getServiceUnitManager();
                if (sum != null) {
                    sum.undeploy(name, targetDir.getAbsolutePath());
                    FileUtil.deleteFile(targetDir);
                }
            }
            else {
                FileUtil.deleteFile(targetDir);
            }
        }
        catch (IOException e) {
            throw new DeploymentException(e);
        }
        log.info("UnDeployed ServiceUnit " + name + " from Component: " + componentName);
    }

    /**
     * Find runnning state and things deployed before shutdown
     */
    protected void buildState() {
        // walk through deployed SA's
        File top = environmentContext.getServiceAssembiliesDirectory();
        if (top != null && top.exists() && top.isDirectory()) {
            File[] files = top.listFiles();
            if (files != null) {
                for (int i = 0;i < files.length;i++) {
                    if (files[i].isDirectory()) {
                        String assemblyName = files[i].getName();
                        log.info("initializing assembly " + assemblyName);
                        try {
                        File assemblyDir = environmentContext.getSARootDirectory(assemblyName);
                        Descriptor root = AutoDeploymentService.buildDescriptor(assemblyDir);
                        if (root != null) {
                            ServiceAssembly sa = root.getServiceAssembly();
                            if (sa != null && sa.getIdentification() != null) {
                                initSA(sa);
                                sa.setState(DeploymentServiceMBean.STARTED);
                                container.getRegistry().registerServiceAssembly(sa);
                                buildConnections(sa);
                                
                            }
                        }
                        }catch(Exception e){
                            log.error("Failed to initialized service assembly: " + assemblyName,e);
                        }
                    }
                }
            }
        }
    }

    protected void initSA(ServiceAssembly sa) throws DeploymentException {
        if (sa != null) {
            ServiceUnit[] sus = sa.getServiceUnits();
            if (sus != null) {
                for (int i = 0;i < sus.length;i++) {
                    ServiceUnit su = sus[i];
                    String name = su.getIdentification().getName();
                    Target target = su.getTarget();
                    String componentName = target.getComponentName();
                    try {
                        File targetDir = environmentContext.getServiceUnitDirectory(componentName, name);
                        // now get the component and give it a SA
                        Component component = container.getComponent(componentName);
                        if (component != null) {
                            ServiceUnitManager sum = component.getServiceUnitManager();
                            if (sum != null) {
                                sum.deploy(name, targetDir.getAbsolutePath());
                                sum.init(name, targetDir.getAbsolutePath());
                                sum.start(name);
                            }
                            else {
                                FileUtil.deleteFile(targetDir);
                                throw new DeploymentException("Component " + componentName
                                        + " doesn't have a ServiceUnitManager");
                            }
                        }
                        else {
                            FileUtil.deleteFile(targetDir);
                            throw new DeploymentException("Component " + componentName + " doesn't exist");
                        }
                    }
                    catch (IOException e) {
                        throw new DeploymentException(e);
                    }
                    log.info("Deployed ServiceUnit " + name + " to Component: " + componentName);
                }
            }
        }
    }

    protected void buildConnections(ServiceAssembly sa) throws JBIException {
        if (sa != null) {
            Connection[] connections = sa.getConnections().getConnection();
            if (connections != null) {
                for (int i = 0; i < connections.length; i++) {
                    Connection connection = connections[i];
                    Consumer consumer = connection.getConsumer();
                    Provider provider = connection.getProvider();
                    QName suName = consumer.getInterfaceName();
                    if (suName != null) {
                        LocalComponentConnector lcc = (LocalComponentConnector) container.getRegistry()
                                .getComponentConnector(suName);
                        if (lcc != null) {
                            lcc.getActivationSpec().setDestinationEndpoint(provider.getEndpointName());
                            lcc.getActivationSpec().setDestinationService(provider.getServiceName());
                        }
                        else {
                            throw new DeploymentException("Unable to build connections, can't find consumer interface "
                                    + suName);
                        }
                    }
                    else {
                        // We didn't have the interface so we will go after
                        // the service name and endpoint
                        InternalEndpoint endPoint = (InternalEndpoint) container.getRegistry().getEndpoint(
                                consumer.getServiceName(), consumer.getEndpointName());
                        if (endPoint != null) {
                            LocalComponentConnector lcc = (LocalComponentConnector) container.getRegistry()
                                    .getComponentConnector(endPoint.getComponentNameSpace());
                            if (lcc != null) {
                                lcc.getActivationSpec().setDestinationEndpoint(provider.getEndpointName());
                                lcc.getActivationSpec().setDestinationService(provider.getServiceName());
                            }
                            else {
                                throw new DeploymentException(
                                        "Unable to build connections, can't find consumer based on component name space "
                                                + endPoint.getComponentNameSpace());
                            }
                        }
                        else {
                            throw new DeploymentException(
                                    "Unable to build connections, can't find consumer with servicename "
                                            + consumer.getServiceName() + " and endpoint " + consumer.getEndpointName());
                        }
                    }
                }
            }
        }
    }

    protected ServiceUnit getServiceUnit(String serviceUnitName, ServiceUnit[] sus) {
        ServiceUnit result = null;
        if (sus != null) {
            for (int i = 0;i < sus.length;i++) {
                if (sus[i].getIdentification().getName().equals(serviceUnitName)) {
                    result = sus[i];
                    break;
                }
            }
        }
        return result;
    }

    protected String buildStatusString(String taskName, String componentName, String message) {
        StringBuffer result = new StringBuffer();
        result.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        result.append("jbi-task xmlns = \"http://java/sun.com/xml/ns/management-message\">");
        result.append("<component-task-result>");
        result.append("<component-name>");
        result.append(componentName);
        result.append("</component-name>");
        result.append("<component-task-result-details>");
        result.append("<task-result-details>");
        result.append("<task-id>");
        result.append(taskName);
        result.append("</task-id>");
        result.append("<task-result>FAILED</task-result>");
        result.append("<message-type>ERROR</message-type>");
        result.append("<task-status-msg>");
        result.append("<msg-loc-info>");
        result.append("<loc-token>");
        result.append(" ");
        result.append("</loc-token>");
        result.append("<loc-message>");
        result.append(message);
        result.append("</loc-message>");
        result.append("</msg-loc-info>");
        result.append("</task-status-msg>");
        result.append("</task-result-details>");
        result.append(message);
        result.append("</component-task-result-details>");
        result.append("</component-task-result>");
        return result.toString();
    }

    /**
     * Get an array of MBeanOperationInfo
     * 
     * @return array of OperationInfos
     * @throws JMException
     */
    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph = helper.addOperation(getObjectToManage(), "deploy", 1, "deploy An SA");
        ph.setDescription(0, "saZipURL", "location of SA zip file");
        ph = helper.addOperation(getObjectToManage(), "undeploy", 1, "undeploy An SA");
        ph.setDescription(0, "saName", "SA name");
        ph = helper.addOperation(getObjectToManage(), "getDeployedServiceUnitList", 1,
                "list of SU's currently deployed");
        ph.setDescription(0, "componentName", "Component name");
        helper.addOperation(getObjectToManage(), "getDeployedServiceAssemblies", "list of deployed SAs");
        ph = helper.addOperation(getObjectToManage(), "getServiceAssemblyDescriptor", 1, "Get descriptor for a SA");
        ph.setDescription(0, "saName", "SA name");
        ph = helper.addOperation(getObjectToManage(), "getDeployedServiceAssembliesForComponent", 1,
                "list of SA's for a Component");
        ph.setDescription(0, "componentName", "Component name");
        ph = helper.addOperation(getObjectToManage(), "getComponentsForDeployedServiceAssembly", 1,
                "list of Components  for a SA");
        ph.setDescription(0, "saName", "SA name");
        ph = helper.addOperation(getObjectToManage(), "isDeployedServiceUnit", 2, "is SU deployed at a Component ?");
        ph.setDescription(0, "componentName", "Component name");
        ph.setDescription(1, "suName", "SU name");
        ph = helper
                .addOperation(getObjectToManage(), "canDeployToComponent", 1, "Can a SU be deployed to a Component?");
        ph.setDescription(0, "componentName", "Component name");
        ph = helper.addOperation(getObjectToManage(), "start", 1, "start an SA");
        ph.setDescription(0, "saName", "SA name");
        ph = helper.addOperation(getObjectToManage(), "stop", 1, "stop an SA");
        ph.setDescription(0, "saName", "SA name");
        ph = helper.addOperation(getObjectToManage(), "shutDown", 1, "shutDown an SA");
        ph.setDescription(0, "saName", "SA name");
        ph = helper.addOperation(getObjectToManage(), "getState", 1, "Running state of an SA");
        ph.setDescription(0, "saName", "SA name");
        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }
}
