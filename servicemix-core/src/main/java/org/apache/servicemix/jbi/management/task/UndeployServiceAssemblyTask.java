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

/**
 * Uninstall an Assembly
 * 
 * @version $Revision$
 */
public class UndeployServiceAssemblyTask extends JbiTask {
    
    private String name; //assembly name to uninstall

    /**
     * @return Returns the assemblyName.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The assembly name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void doExecute(AdminCommandsServiceMBean acs) throws Exception {
        if (name == null) {
            throw new BuildException("null componentName");
        }
        acs.undeployServiceAssembly(name);
    }
    
}