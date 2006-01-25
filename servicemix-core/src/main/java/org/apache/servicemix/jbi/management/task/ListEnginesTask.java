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
import org.apache.tools.ant.BuildException;

import javax.management.ObjectName;
import java.io.IOException;

/**
 * ListEnginesTask
 *
 * @version
 */
public class ListEnginesTask extends JbiTask {
    private static final Log log = LogFactory.getLog(ListEnginesTask.class);

    /**
     * execute the task
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {
        try {
            ManagementContextMBean is = getManagementContext();
            ObjectName[] objName = is.getEngineComponents();

            StringBuffer buffer = new StringBuffer();
            buffer.append("<?xml version='1.0'?>\n");
            buffer.append("<component-info-list xmlns='http://java.sun.com/xml/ns/jbi/component-info-list' version='1.0'>\n");

            if (objName != null) {
                for (int i = 0; i < objName.length; i++) {
                    buffer.append("\t<component-info");
                    buffer.append(" type='service-engine'>\n");
                    buffer.append(" <name>" + objName[i].getKeyProperty("name") + "</name>");
                    buffer.append("\t</component-info>\n");
                }
            }
            buffer.append("</component-info-list>");
            System.out.println(buffer.toString());

        } catch (IOException e) {
            log.error("Caught an exception getting deployed assemblies", e);
            throw new BuildException(e);

        } catch (Exception e) {
            log.error("Caught an exception getting deployed assemblies", e);
            throw new BuildException("exception " + e);
        }

    }

}
