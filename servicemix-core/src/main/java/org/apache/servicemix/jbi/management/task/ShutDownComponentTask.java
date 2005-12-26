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
import org.apache.servicemix.jbi.management.ManagementContextMBean;
import org.apache.tools.ant.BuildException;

import java.io.IOException;

/**
 * Shutdown a Component
 * 
 * @version $Revision$
 */
public class ShutDownComponentTask extends JbiTask {
    private static final Log log = LogFactory.getLog(ShutDownComponentTask.class);
    private String name;

    /**
     * @return Returns the componentName.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The component name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * execute the task
     * 
     * @throws BuildException
     */
    public void execute() throws BuildException {
        if (name == null) {
            throw new BuildException("null componentName");
        }
        try {
            ManagementContextMBean is = getManagementContext();
            is.shutDownComponent(name);
        }
        catch (IOException e) {
            log.error("Caught an exception shutting down component", e);
            throw new BuildException(e);
        }
        catch (Exception e) {
            log.error("Caught an exception shutting down component", e);
            throw new BuildException(e);
        }
    }
}