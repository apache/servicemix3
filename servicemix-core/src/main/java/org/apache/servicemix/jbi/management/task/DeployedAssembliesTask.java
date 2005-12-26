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

package org.apache.servicemix.jbi.management.task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.BuildException;

import javax.jbi.management.DeploymentServiceMBean;

import java.io.IOException;

/**
 * List deployed Service Assemblies
 * 
 * @version $Revision$
 */
public class DeployedAssembliesTask extends JbiTask {
    private static final Log log = LogFactory.getLog(DeployedAssembliesTask.class);

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        try {
            DeploymentServiceMBean is = getDeploymentService();
            String[] sas = is.getDeployedServiceAssemblies();
            if (sas != null) {
                for (int i = 0;i < sas.length;i++) {
                    System.out.println(sas[i]);
                }
            }
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