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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.management.BaseSystemService;

public class AdminCommandsService extends BaseSystemService implements AdminCommandsServiceMBean {

    private JBIContainer container;
    
    /**
     * Initialize the Service
     * 
     * @param container
     * @throws JBIException 
     * @throws DeploymentException
     */
    public void init(JBIContainer container) throws JBIException {
        this.container = container;
        container.getManagementContext().registerSystemService(this, AdminCommandsServiceMBean.class);
    }
    
    public String installComponent(String installJarURL, Properties props) {
        // TODO Auto-generated method stub
        return null;
    }

    public String uninstallComponent(String componentName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String installSharedLibrary(String installJarURL) {
        return container.getInstallationService().installSharedLibrary(installJarURL);
    }

    /* (non-Javadoc)
     * @see org.servicemix.jbi.framework.AdminCommandsServiceMBean#uninstallSharedLibrary(java.lang.String)
     */
    public String uninstallSharedLibrary(String sharedLibraryName) {
        boolean success = container.getInstallationService().uninstallSharedLibrary(sharedLibraryName);
        if (success) {
            return success("uninstallSharedLibrary", sharedLibraryName);
        } else {
            return failure("uninstallSharedLibrary", sharedLibraryName, "Failed", null);
        }
    }

    /* (non-Javadoc)
     * @see org.servicemix.jbi.framework.AdminCommandsServiceMBean#startComponent(java.lang.String)
     */
    public String startComponent(String componentName) {
        try {
            ComponentNameSpace cns = new ComponentNameSpace(container.getName(), componentName, componentName);
            LocalComponentConnector lcc = container.getRegistry().getLocalComponentConnector(cns);
            if (lcc == null) {
                throw new JBIException("Component " + componentName + " not found");
            }
            lcc.getComponentMBean().start();
            return success("startComponent", componentName);
        } catch (JBIException e) {
            throw new RuntimeException(failure("startComponent", componentName, null, e));
        }
    }

    /* (non-Javadoc)
     * @see org.servicemix.jbi.framework.AdminCommandsServiceMBean#stopComponent(java.lang.String)
     */
    public String stopComponent(String componentName) {
        try {
            ComponentNameSpace cns = new ComponentNameSpace(container.getName(), componentName, componentName);
            LocalComponentConnector lcc = container.getRegistry().getLocalComponentConnector(cns);
            if (lcc == null) {
                throw new JBIException("Component " + componentName + " not found");
            }
            lcc.getComponentMBean().stop();
            return success("stopComponent", componentName);
        } catch (JBIException e) {
            throw new RuntimeException(failure("stopComponent", componentName, null, e));
        }
    }

    public String shutdownComponent(String componentName, boolean force) {
        try {
            ComponentNameSpace cns = new ComponentNameSpace(container.getName(), componentName, componentName);
            LocalComponentConnector lcc = container.getRegistry().getLocalComponentConnector(cns);
            if (lcc == null) {
                throw new JBIException("Component " + componentName + " not found");
            }
            lcc.getComponentMBean().shutDown();
            return success("shutdownComponent", componentName);
        } catch (JBIException e) {
            throw new RuntimeException(failure("shutdownComponent", componentName, null, e));
        }
     }

    public String deployServiceAssembly(String installJarURL) {
        // TODO Auto-generated method stub
        return null;
    }

    public String undeployServiceAssembly(String serviceAssemblyName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String startServiceAssembly(String serviceAssemblyName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String stopServiceAssembly(String serviceAssemblyName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String shutdownServiceAssembly(String serviceAssemblyName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String listComponents(boolean serviceEngines, boolean bindingComponents, String state, String sharedLibraryName, String serviceAssemblyName) {
        Collection connectors = container.getRegistry().getLocalComponentConnectors();
        List components = new ArrayList();
        for (Iterator iter = connectors.iterator(); iter.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) iter.next();
            // If we want SE, and it is not one, skip
            if (serviceEngines && !lcc.isService()) {
                continue;
            }
            // If we want BC, and it is not one, skip
            if (bindingComponents && !lcc.isBinding()) {
                continue;
            }
            // Check status
            if (state != null && !state.equals(lcc.getComponentMBean().getCurrentState())) {
                continue;
            }
            // TODO: Check shared library
            // TODO: Check deployed service assembly
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
            buffer.append(" state='" + lcc.getComponentMBean().getCurrentState() + "'");
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

    public String listSharedLibraries(String componentName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String listServiceAssemblies(String state, String componentName) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDescription() {
        return "Admin Commands Service";
    }

    protected String failure(String task, String componentName, String info, Exception e) {
        ManagementSupport.Message msg = new ManagementSupport.Message();
        msg.setComponent(componentName);
        msg.setTask(task);
        msg.setResult("FAILED");
        msg.setType("ERROR");
        msg.setException(e);
        msg.setMessage(info);
        return ManagementSupport.createComponentMessage(msg);
    }

    protected String success(String task, String componentName) {
        ManagementSupport.Message msg = new ManagementSupport.Message();
        msg.setComponent(componentName);
        msg.setTask(task);
        msg.setResult("SUCCESS");
        // TODO: change the generated xml
        return ManagementSupport.createComponentMessage(msg);
    }
    
}
