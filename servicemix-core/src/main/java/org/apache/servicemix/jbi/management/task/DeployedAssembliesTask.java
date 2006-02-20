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
import org.apache.tools.ant.BuildException;
import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;

import javax.jbi.management.DeploymentServiceMBean;

import java.io.IOException;

/**
 * List deployed Service Assemblies
 * 
 * @version $Revision$
 */
public class DeployedAssembliesTask extends JbiTask {
    private static final Log log = LogFactory.getLog(DeployedAssembliesTask.class);
    private String state;
    private String componentName;
    private String serviceAssemblyName;

    /**
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @param state Sets the state
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     *
     * @return the component name
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     *
     * @param componentName Sets the component name
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
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
     * @param serviceAssemblynname Sets the service assembly name
     */
    public void setServiceAssemblyName(String serviceAssemblynname) {
        this.serviceAssemblyName = serviceAssemblynname;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        try {
            AdminCommandsServiceMBean acs = getAdminCommandsService();
            String result = acs.listServiceAssemblies(this.getState(), this.getComponentName(), this.getServiceAssemblyName());
            System.out.println(result);
        }
        catch (IOException e) {
            log.error("Caught an exception getting deployed assemblies", e);
            throw new BuildException(e);
        }
        catch (Exception e) {
            log.error("Caught an exception getting deployed assemblies", e);
            throw new BuildException(e);
        }
    }
}