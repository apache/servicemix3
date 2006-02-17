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

import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.management.ParameterHelper;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.management.MBeanOperationInfo;
import javax.management.JMException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class AdminCommandsService extends BaseSystemService implements AdminCommandsServiceMBean {

    /**
     * @return a description of this
     */
    public String getDescription() {
        return "Admin Commands Service";
    }
    
    protected Class getServiceMBean() {
        return AdminCommandsServiceMBean.class;
    }

    /**
     * Install a JBI component (a Service Engine or Binding Component)
     *
     * @param file
     * @return
     */
    public String installComponent(String file) throws Exception {
        return installComponent(file, null);
    }
    
    /**
     * Install a JBI component (a Service Engine or Binding Component)
     *
     * @param file jbi component archive to install
     * @param props installation properties
     * @return
     */
    public String installComponent(String file, Properties props) throws Exception {
        try {
            container.getInstallationService().install(file);
            return success("installComponent", file);

        } catch (DeploymentException e) {
            throw new RuntimeException(failure("installComponent", file, null, e));
        } catch (Exception e) {
            throw new RuntimeException(failure("installComponent", file, null, e));
        }
    }

    /**
     * Uninstalls a previously install JBI Component (a Service Engine or Binding Component)
     *
     * @param name
     * @return
     */
    public String uninstallComponent(String name) throws Exception {
        boolean success = container.getInstallationService().unloadInstaller(name, true);
        if (success) {
            return success("uninstallComponent", name);
        } else {
            return failure("uninstallComponent", name, "Failed", null);
        }
    }

    /**
     * Installs a Shared Library.
     *
     * @param file
     * @return
     */
    public String installSharedLibrary(String file) throws Exception {
        return container.getInstallationService().installSharedLibrary(file);
    }

    /**
     * Uninstalls a previously installed Shared Library.
     *
     * @param name
     * @return
     */
    public String uninstallSharedLibrary(String name) throws Exception {
        boolean success = container.getInstallationService().uninstallSharedLibrary(name);
        if (success) {
            return success("uninstallSharedLibrary", name);
        } else {
            return failure("uninstallSharedLibrary", name, "Failed", null);
        }
    }

    /**
     * Starts a particular Component (Service Engine or Binding Component).
     *
     * @param name
     * @return
     */
    public String startComponent(String name) throws Exception {
        try {
            ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
            LocalComponentConnector lcc = container.getRegistry().getLocalComponentConnector(cns);
            if (lcc == null) {
                throw new JBIException("Component " + name + " not found");
            }
            lcc.getComponentMBean().start();
            return success("startComponent", name);
        } catch (JBIException e) {
            throw new RuntimeException(failure("startComponent", name, null, e));
        }
    }

    /**
     * Stops a particular Component (Service Engine or Binding Component).
     *
     * @param name
     * @return
     */
    public String stopComponent(String name) throws Exception {
        try {
            ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
            LocalComponentConnector lcc = container.getRegistry().getLocalComponentConnector(cns);
            if (lcc == null) {
                throw new JBIException("Component " + name + " not found");
            }
            lcc.getComponentMBean().stop();
            return success("stopComponent", name);
        } catch (JBIException e) {
            throw new RuntimeException(failure("stopComponent", name, null, e));
        }
    }

    /**
     * Shuts down a particular Component.
     *
     * @param name
     * @return
     */
    public String shutdownComponent(String name) throws Exception {
        try {
            ComponentNameSpace cns = new ComponentNameSpace(container.getName(), name, name);
            LocalComponentConnector lcc = container.getRegistry().getLocalComponentConnector(cns);
            if (lcc == null) {
                throw new JBIException("Component " + name + " not found");
            }
            lcc.getComponentMBean().shutDown();
            return success("shutdownComponent", name);
        } catch (JBIException e) {
            throw new RuntimeException(failure("shutdownComponent", name, null, e));
        }
    }

    /**
     * Deploys a Service Assembly.
     *
     * @param file
     * @return
     */
    public String deployServiceAssembly(String file) throws Exception {
        try {
            container.getDeploymentService().deploy(file);
            return success("installComponent", file);

        } catch (Exception e) {
            throw new RuntimeException(failure("deployServiceAssembly", file, null, e));
        }
    }

    /**
     * Undeploys a previously deployed service assembly.
     *
     * @param name
     * @return
     */
    public String undeployServiceAssembly(String name) throws Exception {
        try {
            container.getDeploymentService().undeploy(name);
            return success("undeployServiceAssembly", name);

        } catch (Exception e) {
            throw new RuntimeException(failure("undeployServiceAssembly", name, null, e));
        }
    }

    /**
     * Starts a service assembly.
     *
     * @param name
     * @return
     */
    public String startServiceAssembly(String name) throws Exception {
        try {
            container.getDeploymentService().start(name);
            return success("startServiceAssembly", name);

        } catch (Exception e) {
            throw new RuntimeException(failure("startServiceAssembly", name, null, e));
        }
    }

    /**
     * Stops a particular service assembly.
     *
     * @param name
     * @return
     */
    public String stopServiceAssembly(String name) throws Exception {
        try {
            container.getDeploymentService().stop(name);
            return success("stopServiceAssembly", name);

        } catch (Exception e) {
            throw new RuntimeException(failure("stopServiceAssembly", name, null, e));
        }
    }

    /**
     * Shuts down a particular service assembly.
     *
     * @param name
     * @return
     */
    public String shutdownServiceAssembly(String name) throws Exception {
        try {
            container.getDeploymentService().shutDown(name);
            return success("shutdownServiceAssembly", name);

        } catch (Exception e) {
            throw new RuntimeException(failure("shutdownServiceAssembly", name, null, e));
        }
    }
    
   
    /**
     * load an archive from an external location and starts it
     * The archive can be a Component, Service Assembly or Shared Library.
     * @param location - can either be a url or filename (if relative - must be relative to the container)
     * @return status
     * @throws Exception 
     */
    public String installArchive(String location) throws Exception{
        try {
            container.updateExternalArchive(location);
            return success("installArchive", location);

        } catch (Exception e) {
            throw new RuntimeException(failure("shutdownServiceAssembly", location, null, e));
        }
    }
    
    /**
     * Prints information about all components (Service Engine or Binding Component) installed
     *
     * @param serviceEngines
     * @param bindingComponents
     * @param state
     * @param sharedLibraryName
     * @param serviceAssemblyName
     * @return
     */
    public String listComponents(boolean serviceEngines, boolean bindingComponents, String state, String sharedLibraryName, String serviceAssemblyName) throws Exception {
        return listAllComponents(serviceEngines, bindingComponents, state, sharedLibraryName, serviceAssemblyName, true);
    }
    
    /**
     * Prints information about all JBI components (Service Engine or Binding Component) installed
     *
     * @param serviceEngines
     * @param bindingComponents
     * @param state
     * @param sharedLibraryName
     * @param serviceAssemblyName
     * @return
     */
    public String listJBIComponents(boolean serviceEngines, boolean bindingComponents, String state, String sharedLibraryName, String serviceAssemblyName) throws Exception {
        return listAllComponents(serviceEngines, bindingComponents, state, sharedLibraryName, serviceAssemblyName, false);
    }

    /**
     * Prints information about all components (Service Engine or Binding Component) installed
     *
     * @param serviceEngines
     * @param bindingComponents
     * @param state
     * @param sharedLibraryName
     * @param serviceAssemblyName
     * @return
     */
    public String listAllComponents(boolean serviceEngines, boolean bindingComponents, String state, String sharedLibraryName, String serviceAssemblyName, boolean pojo) throws Exception {
        Collection connectors = container.getRegistry().getLocalComponentConnectors();
        List components = new ArrayList();
        for (Iterator iter = connectors.iterator(); iter.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) iter.next();

            // If we want SE, and it is not one, skip
            if (!serviceEngines && !lcc.isService()) {
                continue;
            }
            // If we want BC, and it is not one, skip
            if (!bindingComponents && !lcc.isBinding()) {
                continue;
            }
            // Check status
            if (state != null && state.length() > 0 && !state.equals(lcc.getComponentMBean().getCurrentState())) {
                System.out.println("state passed");
                continue;
            }

            // Check shared library
            if (sharedLibraryName != null && sharedLibraryName.length() > 0 && !container.getInstallationService().containsSharedLibrary(sharedLibraryName)) {
                continue;
            }

            // Check deployed service assembly
            if (serviceAssemblyName != null && serviceAssemblyName.length() > 0 && !container.getDeploymentService().isSaDeployed(serviceAssemblyName)) {
                continue;
            }
            
            if (lcc.isPojo() && !pojo){
                continue;
            }

            components.add(lcc);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>\n");
        buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) iter.next();
            buffer.append("\t<component-info");
            if (!lcc.isBinding() && lcc.isService()) {
                buffer.append(" type='service-engine'");
            } else if (lcc.isBinding() && !lcc.isService()) {
                buffer.append(" type='binding-component'");
            }
            buffer.append(" name='" + lcc.getComponentNameSpace().getName() + "'");
            buffer.append(" state='" + lcc.getComponentMBean().getCurrentState() + "'>");
            if (lcc.getPacket().getDescription() != null) {
                buffer.append("\t\t<description>");
                buffer.append(lcc.getPacket().getDescription());
                buffer.append("<description>\n");
            }
            buffer.append("\t</component-info>\n");
        }
        buffer.append("</component-info-list>");
        return buffer.toString();
    }
    
    /**
     * Prints information about all  Pojo components installed    
     * @return XML string
     */
    public String listPojoComponents() throws Exception {
        Collection connectors = container.getRegistry().getLocalComponentConnectors();
        List components = new ArrayList();
        for(Iterator iter=connectors.iterator();iter.hasNext();){
            LocalComponentConnector lcc=(LocalComponentConnector) iter.next();
            if(lcc.isPojo()){
                components.add(lcc);
            }
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>\n");
        buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) iter.next();
            buffer.append(" name='" + lcc.getComponentNameSpace().getName() + "'");
            buffer.append(" state='" + lcc.getComponentMBean().getCurrentState() + "'>");
            if (lcc.getPacket().getDescription() != null) {
                buffer.append("\t\t<description>");
                buffer.append(lcc.getPacket().getDescription());
                buffer.append("<description>\n");
            }
            buffer.append("\t</component-info>\n");
        }
        buffer.append("</component-info-list>");
        return buffer.toString();
    }

    /**
     * Prints information about shared libraries installed.
     *
     * @param componentName
     * @param sharedLibraryName
     * @return
     */
    public String listSharedLibraries(String componentName, String sharedLibraryName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Prints information about service assemblies deployed.
     *
     * @param state
     * @param componentName
     * @param serviceAssemblyName
     * @return
     */
    public String listServiceAssemblies(String state, String componentName, String serviceAssemblyName) throws Exception {
        String[] result = null;
        if (null != serviceAssemblyName && serviceAssemblyName.length() > 0) {
            result = new String[]{serviceAssemblyName};
        } else if (null != componentName && componentName.length() > 0) {
            result = container.getRegistry().getDeployedServiceAssembliesForComponent(componentName);
        } else {
            result = container.getRegistry().getDeployedServiceAssemblies();
        }

        List components = new ArrayList();
        for (int i = 0; i < result.length; i++) {
            // Check status
            if (state != null && state.length() > 0 && !state.equals(container.getRegistry().getServiceAssemblyState(result[i]))) {
                continue;
            }
            components.add(result[i]);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>\n");
        buffer.append("<service-assembly-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            String name = (String) iter.next();

            buffer.append("\t<service-assembly-info");
            buffer.append(" name='" + name + "'");
            buffer.append(" state='" + container.getRegistry().getServiceAssemblyState(name) + "'/>");
            buffer.append(" <description>" + container.getRegistry().getServiceAssemblyDesc(name) + "</description>");
            buffer.append("\t</service-assembly-info>\n");

            String[] serviceUnitList = container.getRegistry().getSADeployedServiceUnitList(name);
            for (int i = 0; i < serviceUnitList.length; i++) {
                buffer.append("\t<service-unit-info");
                buffer.append(" name='" + serviceUnitList[i] + "'");
                buffer.append(" state='" + container.getRegistry().getSADeployedServiceUnitDesc(name, serviceUnitList[i]) + "/>");
                buffer.append("\t</service-unit-info");
            }
        }
        buffer.append("</service-assembly-info-list>");

        return buffer.toString();
    }

    public String failure(String task, String componentName, String info, Exception e) {
        ManagementSupport.Message msg = new ManagementSupport.Message();
        msg.setComponent(componentName);
        msg.setTask(task);
        msg.setResult("FAILED");
        msg.setType("ERROR");
        msg.setException(e);
        msg.setMessage(info);
        return ManagementSupport.createComponentMessage(msg);
    }

    public String success(String task, String componentName) {
        ManagementSupport.Message msg = new ManagementSupport.Message();
        msg.setComponent(componentName);
        msg.setTask(task);
        msg.setResult("SUCCESS");
        // TODO: change the generated xml
        return ManagementSupport.createComponentMessage(msg);
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph = helper.addOperation(getObjectToManage(), "installComponent",1, "install a component");
        ph.setDescription(0, "file", "location of JBI Component to install");

        ph = helper.addOperation(getObjectToManage(), "uninstallComponent", 1, "uninstall a component");
        ph.setDescription(0, "name", "component name to uninstall");

        ph = helper.addOperation(getObjectToManage(), "installSharedLibrary", 1, "install a shared library");
        ph.setDescription(0, "file", "location of shared library to install");

        ph = helper.addOperation(getObjectToManage(), "uninstallSharedLibrary", 1, "uninstall a shared library");
        ph.setDescription(0, "name", "name of shared library to uninstall");
        
        ph = helper.addOperation(getObjectToManage(), "installArchive", 1, "install an archive (component/SA etc)");
        ph.setDescription(0, "location", "file name or url to the location");
        

        ph = helper.addOperation(getObjectToManage(), "startComponent", 1, "start a component");
        ph.setDescription(0, "name", "name of component to start");

        ph = helper.addOperation(getObjectToManage(), "stopComponent", 1, "stop a component");
        ph.setDescription(0, "name", "name of component to stop");

        ph = helper.addOperation(getObjectToManage(), "shutdownComponent", 1, "shutdown a component");
        ph.setDescription(0, "name", "name of component to shutdown");

        ph = helper.addOperation(getObjectToManage(), "deployServiceAssembly", 1, "deploy a service assembly");
        ph.setDescription(0, "file", "location of service assembly to deploy");

        ph = helper.addOperation(getObjectToManage(), "undeployServiceAssembly", 1, "undeploy a service assembly");
        ph.setDescription(0, "name", "name of service assembly to undeploy");

        ph = helper.addOperation(getObjectToManage(), "startServiceAssembly", 1, "start a service assembly");
        ph.setDescription(0, "name", "name of service assembly to start");

        ph = helper.addOperation(getObjectToManage(), "stopServiceAssembly", 1, "stop a service assembly");
        ph.setDescription(0, "name", "name of service assembly to stop");

        ph = helper.addOperation(getObjectToManage(), "shutdownServiceAssembly", "shutdown a service assembly");
        ph.setDescription(0, "name", "name of service assembly to shutdown");

        ph = helper.addOperation(getObjectToManage(), "listComponents", 5, "list all components installed");
        ph.setDescription(0, "serviceEngines", "if true will list service engines");
        ph.setDescription(1, "bindingComponents", "if true will list binding components");
        ph.setDescription(2, "state", "component state to list, if null will list all");
        ph.setDescription(3, "sharedLibraryName", "shared library name to list");
        ph.setDescription(4, "serviceAssemblyName", "service assembly name to list");
        
        ph = helper.addOperation(getObjectToManage(), "listJBIComponents", 5, "list JBI components installed");
        ph.setDescription(0, "serviceEngines", "if true will list service engines");
        ph.setDescription(1, "bindingComponents", "if true will list binding components");
        ph.setDescription(2, "state", "component state to list, if null will list all");
        ph.setDescription(3, "sharedLibraryName", "shared library name to list");
        ph.setDescription(4, "serviceAssemblyName", "service assembly name to list");
        
        ph = helper.addOperation(getObjectToManage(), "listPojoComponents", "list all POJO Components");

        ph = helper.addOperation(getObjectToManage(), "listSharedLibraries", 2, "list shared library");
        ph.setDescription(0, "componentName", "component name");
        ph.setDescription(1, "sharedLibraryName", "shared library name");

        ph = helper.addOperation(getObjectToManage(), "listServiceAssemblies", 3, "list service assemblies");
        ph.setDescription(0, "state", "service assembly state to list");
        ph.setDescription(1, "componentName", "component name");
        ph.setDescription(2, "serviceAssemblyName", "service assembly name");
        
        
        
        
                

        return OperationInfoHelper.join(super.getOperationInfos(), helper.getOperationInfos());
    }

}
