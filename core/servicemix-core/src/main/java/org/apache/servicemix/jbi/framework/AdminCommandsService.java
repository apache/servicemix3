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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.management.JMException;
import javax.management.MBeanOperationInfo;

import org.apache.servicemix.jbi.management.BaseSystemService;
import org.apache.servicemix.jbi.management.OperationInfoHelper;
import org.apache.servicemix.jbi.management.ParameterHelper;

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
     *            jbi component archive to install
     * @param props
     *            installation properties
     * @return
     */
    public String installComponent(String file, Properties props, boolean deferException) throws Exception {
        try {
            if (deferException) {
                container.updateExternalArchive(file);
            } else {
                container.getInstallationService().install(file, props, false);
            }
            return ManagementSupport.createSuccessMessage("installComponent", file);
        } catch (Exception e) {
            throw ManagementSupport.failure("installComponent", file, e);
        }
    }

    /**
     * Uninstalls a previously install JBI Component (a Service Engine or
     * Binding Component)
     * 
     * @param name
     * @return
     */
    public String uninstallComponent(String name) throws Exception {
        ComponentMBeanImpl comp = container.getComponent(name);
        if (comp == null) {
            throw ManagementSupport.failure("uninstallComponent", "Component '" + name + "' is not installed.");
        }
        if (!comp.isShutDown()) {
            throw ManagementSupport.failure("uninstallComponent", "Component '" + name + "' is not shut down.");
        }
        // taken from org.apache.servicemix.jbi.framework.AutoDeploymentService#removeArchive
        // ensure installer is loaded
        container.getInstallationService().loadInstaller(name);
        // uninstall and delete component
        boolean success = container.getInstallationService().unloadInstaller(name, true);
        if (success) {
            return ManagementSupport.createSuccessMessage("uninstallComponent", name);
        } else {
            throw ManagementSupport.failure("uninstallComponent", name);
        }
    }

    /**
     * Installs a Shared Library.
     * 
     * @param file
     * @return
     */
    public String installSharedLibrary(String file, boolean deferException) throws Exception {
        if (deferException) {
            container.updateExternalArchive(file);
            return ManagementSupport.createSuccessMessage("installSharedLibrary", file);
        } else {
            return container.getInstallationService().installSharedLibrary(file);
        }
    }

    /**
     * Uninstalls a previously installed Shared Library.
     * 
     * @param name
     * @return
     */
    public String uninstallSharedLibrary(String name) throws Exception {
        // Check that the library is installed
        SharedLibrary sl = container.getRegistry().getSharedLibrary(name);
        if (sl == null) {
            throw ManagementSupport.failure("uninstallSharedLibrary", "Shared library '" + name + "' is not installed.");
        }
        // Check that it is not used by a running component
        Collection components = container.getRegistry().getComponents();
        for (Iterator iter = components.iterator(); iter.hasNext();) {
            ComponentMBeanImpl comp = (ComponentMBeanImpl) iter.next();
            if (!comp.isShutDown()) {
                String[] sls = comp.getSharedLibraries();
                if (sls != null) {
                    for (int i = 0; i < sls.length; i++) {
                        if (name.equals(sls[i])) {
                            throw ManagementSupport.failure("uninstallSharedLibrary", "Shared library '" + name
                                            + "' is used by component '" + comp.getName() + "'.");
                        }
                    }
                }
            }
        }
        boolean success = container.getInstallationService().uninstallSharedLibrary(name);
        if (success) {
            return ManagementSupport.createSuccessMessage("uninstallSharedLibrary", name);
        } else {
            throw ManagementSupport.failure("uninstallSharedLibrary", name);
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
            ComponentMBeanImpl lcc = container.getComponent(name);
            if (lcc == null) {
                throw new JBIException("Component " + name + " not found");
            }
            lcc.start();
            return ManagementSupport.createSuccessMessage("startComponent", name);
        } catch (JBIException e) {
            throw ManagementSupport.failure("startComponent", name, e);
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
            ComponentMBeanImpl lcc = container.getComponent(name);
            if (lcc == null) {
                throw new JBIException("Component " + name + " not found");
            }
            lcc.stop();
            return ManagementSupport.createSuccessMessage("stopComponent", name);
        } catch (JBIException e) {
            throw ManagementSupport.failure("stopComponent", name, e);
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
            ComponentMBeanImpl lcc = container.getComponent(name);
            if (lcc == null) {
                throw new JBIException("Component " + name + " not found");
            }
            lcc.shutDown();
            return ManagementSupport.createSuccessMessage("shutdownComponent", name);
        } catch (JBIException e) {
            throw ManagementSupport.failure("shutdownComponent", name, e);
        }
    }

    /**
     * Deploys a Service Assembly.
     * 
     * @param file
     * @return
     */
    public String deployServiceAssembly(String file, boolean deferException) throws Exception {
        if (deferException) {
            container.updateExternalArchive(file);
            return ManagementSupport.createSuccessMessage("deployServiceAssembly", file);
        } else {
            return container.getDeploymentService().deploy(file);
        }
    }

    /**
     * Undeploys a previously deployed service assembly.
     * 
     * @param name
     * @return
     */
    public String undeployServiceAssembly(String name) throws Exception {
        return container.getDeploymentService().undeploy(name);
    }

    /**
     * Starts a service assembly.
     * 
     * @param name
     * @return
     */
    public String startServiceAssembly(String name) throws Exception {
        return container.getDeploymentService().start(name);
    }

    /**
     * Stops a particular service assembly.
     * 
     * @param name
     * @return
     */
    public String stopServiceAssembly(String name) throws Exception {
        return container.getDeploymentService().stop(name);
    }

    /**
     * Shuts down a particular service assembly.
     * 
     * @param name
     * @return
     */
    public String shutdownServiceAssembly(String name) throws Exception {
        return container.getDeploymentService().shutDown(name);
    }

    /**
     * load an archive from an external location and starts it The archive can
     * be a Component, Service Assembly or Shared Library.
     * 
     * @param location -
     *            can either be a url or filename (if relative - must be
     *            relative to the container)
     * @return status
     * @throws Exception
     */
    public String installArchive(String location) throws Exception {
        try {
            container.updateExternalArchive(location);
            return ManagementSupport.createSuccessMessage("installComponent", location);
        } catch (Exception e) {
            throw ManagementSupport.failure("installComponent", location, e);
        }
    }

    /**
     * Prints information about all components (Service Engine or Binding
     * Component) installed
     * 
     * @param excludeSEs
     * @param excludeBCs
     * @param excludePojos
     * @param requiredState
     * @param sharedLibraryName
     * @param serviceAssemblyName
     * @return list of components in an XML blob
     */
    public String listComponents(boolean excludeSEs, boolean excludeBCs, boolean excludePojos, String requiredState,
                    String sharedLibraryName, String serviceAssemblyName) throws Exception {
        // validate requiredState
        if (requiredState != null && requiredState.length() > 0
                        && !LifeCycleMBean.UNKNOWN.equalsIgnoreCase(requiredState)
                        && !LifeCycleMBean.SHUTDOWN.equalsIgnoreCase(requiredState)
                        && !LifeCycleMBean.STOPPED.equalsIgnoreCase(requiredState)
                        && !LifeCycleMBean.STARTED.equalsIgnoreCase(requiredState)) {
            throw ManagementSupport.failure("listComponents", "Required state '" + requiredState + "' is not a valid state.");
        }
        // Get components
        Collection connectors = container.getRegistry().getComponents();
        List<ComponentMBeanImpl> components = new ArrayList<ComponentMBeanImpl>();
        for (Iterator iter = connectors.iterator(); iter.hasNext();) {
            ComponentMBeanImpl component = (ComponentMBeanImpl) iter.next();
            // Skip SEs if needed
            if (excludeSEs && component.isService()) {
                continue;
            }
            // Skip BCs if needed
            if (excludeBCs && component.isBinding()) {
                continue;
            }
            // Skip Pojos if needed
            if (excludePojos && component.isPojo()) {
                continue;
            }
            // Check status
            if (requiredState != null && requiredState.length() > 0 && !requiredState.equalsIgnoreCase(component.getCurrentState())) {
                continue;
            }
            // Check shared library
            // TODO: check component dependency on SL
            if (sharedLibraryName != null && sharedLibraryName.length() > 0
                            && !container.getInstallationService().containsSharedLibrary(sharedLibraryName)) {
                continue;
            }
            // Check deployed service assembly
            // TODO: check SA dependency on component
            if (serviceAssemblyName != null && serviceAssemblyName.length() > 0) {
                String[] saNames = container.getRegistry().getDeployedServiceAssembliesForComponent(component.getName());
                boolean found = false;
                for (int i = 0; i < saNames.length; i++) {
                    if (serviceAssemblyName.equals(saNames[i])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            }
            components.add(component);
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>\n");
        buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
        for (Iterator<ComponentMBeanImpl> iter = components.iterator(); iter.hasNext();) {
            ComponentMBeanImpl component = iter.next();
            buffer.append("  <component-info");
            if (!component.isBinding() && component.isService()) {
                buffer.append(" type='service-engine'");
            } else if (component.isBinding() && !component.isService()) {
                buffer.append(" type='binding-component'");
            }
            buffer.append(" name='" + component.getName() + "'");
            buffer.append(" state='" + component.getCurrentState() + "'>\n");
            if (component.getDescription() != null) {
                buffer.append("    <description>");
                buffer.append(component.getDescription());
                buffer.append("</description>\n");
            }
            buffer.append("  </component-info>\n");
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
        Collection<SharedLibrary> libs;
        if (sharedLibraryName != null) {
            SharedLibrary sl = container.getRegistry().getSharedLibrary(sharedLibraryName);
            if (sl == null) {
                libs = Collections.EMPTY_LIST;
            } else {
                libs = Collections.singletonList(sl);
            }
        } else if (componentName != null) {
            // TODO: handle componentName
            libs = container.getRegistry().getSharedLibraries();
        } else {
            libs = container.getRegistry().getSharedLibraries();
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>\n");
        buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");
        for (Iterator<SharedLibrary> iter = libs.iterator(); iter.hasNext();) {
            SharedLibrary sl = iter.next();
            buffer.append("  <component-info type='shared-library' name='").append(sl.getName()).append("' state='Started'>");
            if (sl.getDescription() != null) {
                buffer.append("    <description>");
                buffer.append(sl.getDescription());
                buffer.append("</description>\n");
            }
            buffer.append("  </component-info>\n");
        }
        buffer.append("</component-info-list>");
        return buffer.toString();
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
            result = new String[] {serviceAssemblyName };
        } else if (null != componentName && componentName.length() > 0) {
            result = container.getRegistry().getDeployedServiceAssembliesForComponent(componentName);
        } else {
            result = container.getRegistry().getDeployedServiceAssemblies();
        }

        List<ServiceAssemblyLifeCycle> assemblies = new ArrayList<ServiceAssemblyLifeCycle>();
        for (int i = 0; i < result.length; i++) {
            ServiceAssemblyLifeCycle sa = container.getRegistry().getServiceAssembly(result[i]);
            if (sa != null) {
                // Check status
                if (state != null && state.length() > 0 && !state.equals(sa.getCurrentState())) {
                    continue;
                }
                assemblies.add(sa);
            }
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version='1.0'?>\n");
        buffer.append("<service-assembly-info-list xmlns='http://java.sun.com/xml/ns/jbi/service-assembly-info-list' version='1.0'>\n");
        for (Iterator<ServiceAssemblyLifeCycle> iter = assemblies.iterator(); iter.hasNext();) {
            ServiceAssemblyLifeCycle sa = iter.next();
            buffer.append("  <service-assembly-info");
            buffer.append(" name='" + sa.getName() + "'");
            buffer.append(" state='" + sa.getCurrentState() + "'>\n");
            buffer.append("    <description>" + sa.getDescription() + "</description>\n");

            ServiceUnitLifeCycle[] serviceUnitList = sa.getDeployedSUs();
            for (int i = 0; i < serviceUnitList.length; i++) {
                buffer.append("    <service-unit-info");
                buffer.append(" name='" + serviceUnitList[i].getName() + "'");
                buffer.append(" state='" + serviceUnitList[i].getCurrentState() + "'");
                buffer.append(" deployed-on='" + serviceUnitList[i].getComponentName() + "'>\n");
                buffer.append("      <description>" + serviceUnitList[i].getDescription() + "</description>\n");
                buffer.append("    </service-unit-info>\n");
            }

            buffer.append("  </service-assembly-info>\n");
        }
        buffer.append("</service-assembly-info-list>");

        return buffer.toString();
    }

    public MBeanOperationInfo[] getOperationInfos() throws JMException {
        OperationInfoHelper helper = new OperationInfoHelper();
        ParameterHelper ph = helper.addOperation(getObjectToManage(), "installComponent", 3, "install a component");
        ph.setDescription(0, "file", "location of JBI Component to install");
        ph.setDescription(1, "properties", "component installation properties");
        ph.setDescription(1, "deferExceptions", "true if exceptions due to missing dependencies should be differed");

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
        ph.setDescription(0, "excludeSEs", "if true will exclude service engines");
        ph.setDescription(1, "excludeBCs", "if true will exclude binding components");
        ph.setDescription(1, "excludePojos", "if true will exclude pojos components");
        ph.setDescription(2, "requiredState", "component state to list, if null will list all");
        ph.setDescription(3, "sharedLibraryName", "shared library name to list");
        ph.setDescription(4, "serviceAssemblyName", "service assembly name to list");

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
