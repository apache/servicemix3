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
package org.apache.servicemix.jbi.management.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.management.ManagementContextMBean;
import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.BuildException;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * ListComponentTask
 *
 * @version $Revision: 
 */
public class ListComponentTask extends JbiTask {
    private static final Log log = LogFactory.getLog(DeployedAssembliesTask.class);
    private String sharedLibraryName;
    private String serviceAssemblyName;
    private String bindingComponentName;
    private String state;

    /**
     *
     * @return shared library name
     */
    public String getSharedLibraryName() {
        return sharedLibraryName;
    }

    /**
     *
     * @param sharedLibraryName
     */
    public void setSharedLibraryName(String sharedLibraryName) {
        this.sharedLibraryName = sharedLibraryName;
    }

    /**
     *
     * @return service assembly name
     */
    public String getServiceAssemblyName() {
        return serviceAssemblyName;
    }

    /**
     *
     * @param serviceAssemblyName
     */
    public void setServiceAssemblyName(String serviceAssemblyName) {
        this.serviceAssemblyName = serviceAssemblyName;
    }

    /**
     *
     * @return binding component name
     */
    public String getBindingComponentName() {
        return bindingComponentName;
    }

    /**
     *
     * @param bindingComponentName
     */
    public void setBindingComponentName(String bindingComponentName) {
        this.bindingComponentName = bindingComponentName;
    }

    /**
     *
     * @return component state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state Sets the component state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        try {
             AdminCommandsServiceMBean acs;
             acs = getAdminCommandsService();
             String result = acs.listComponents(false, true, this.getState(), this.getSharedLibraryName(), this.getServiceAssemblyName());
             System.out.println(result);
        } catch (IOException e) {
            log.error("Caught an exception getting the admin commands service", e);
            throw new BuildException("exception " + e);
        } catch (Exception e) {
            log.error("Error listing component", e);
            throw new BuildException("exception " + e);
        }

    }
}
