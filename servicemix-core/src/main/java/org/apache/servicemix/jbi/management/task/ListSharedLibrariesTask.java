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

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * ListSharedLibrariesTask
 *
 * @version $Revision: 
 */
public class ListSharedLibrariesTask extends JbiTask {
    
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
    public void doExecute(AdminCommandsServiceMBean acs) throws Exception {
        String result = acs.listSharedLibraries(getComponentName(), getSharedLibraryName());
        log(result, Project.MSG_WARN);
    }
    
}
