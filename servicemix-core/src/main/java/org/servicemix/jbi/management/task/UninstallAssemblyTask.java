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

package org.servicemix.jbi.management.task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;

import javax.jbi.management.DeploymentServiceMBean;

import java.io.IOException;

/**
 * Uninstall an Assembly
 * 
 * @version $Revision$
 */
public class UninstallAssemblyTask extends JbiTask {
    private static final Log log = LogFactory.getLog(UninstallComponentTask.class);
    private String assemblyName; //assemblyName to uninstall

    /**
     * @return Returns the assemblyName.
     */
    public String getAssemblyName() {
        return assemblyName;
    }

    /**
     * @param assemblyName The assemblyName to set.
     */
    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        if (assemblyName == null) {
            throw new BuildException("null assemblyName");
        }
        try {
            DeploymentServiceMBean is = getDeploymentService();
            is.undeploy(assemblyName);
        }
        catch (IOException e) {
            log.error("Caught an exception uninstalling the assembly", e);
            throw new BuildException(e);
        }
        catch (Exception e) {
            log.error("Caught an exception uninstalling the assembly", e);
            throw new BuildException(e);
        }
    }
}