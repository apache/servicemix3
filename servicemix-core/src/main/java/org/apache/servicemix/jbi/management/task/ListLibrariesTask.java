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
import org.apache.servicemix.jbi.management.ManagementContextMBean;
import org.apache.servicemix.jbi.management.task.JbiTask;
import org.apache.servicemix.jbi.management.task.DeployedAssembliesTask;
import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * ListLibrariesTask
 *
 * @version $Revision: 
 */
public class ListLibrariesTask extends JbiTask {
    private static final Log log = LogFactory.getLog(DeployedAssembliesTask.class);
    private String componentName;
    private String sharedLibraryName;

    /**
     *
     * @return component name
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     *
     * @param componentName The component name to set
     */
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    /**
     *
     * @return shared library name
     */
    public String getSharedLibraryName() {
        return sharedLibraryName;
    }

    /**
     *
     * @param sharedLibraryName the shared library name to set
     */
    public void setSharedLibraryName(String sharedLibraryName) {
        this.sharedLibraryName = sharedLibraryName;
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
            String result = acs.listSharedLibraries(this.getComponentName(), this.getSharedLibraryName());
            System.out.println(result);
        } catch (IOException e) {
            log.error("Caught an exception getting admin commands service", e);
            throw new BuildException(e);
        }  catch (Exception e) {
            log.error("Error listing shared libraries", e);
            throw new BuildException(e);
        }
    }
}
