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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.BuildException;

/**
 * Install a Component
 * 
 * @version $Revision$
 */
public class InstallComponentTask extends JbiTask {
    private static final Log log = LogFactory.getLog(InstallComponentTask.class);
    private String file; //file to install
    private String paramsFile;
    private List nestedParams;
    
    /**
     * @return Returns the file.
     */
    public String getFile() {
        return file;
    }
    /**
     * @param file The file to set.
     */
    public void setFile(String file) {
        this.file = file;
    }
    
    public String getParams() {
        return paramsFile;
    }
    public void setParams(String paramsFile) {
        this.paramsFile = paramsFile;
    }
    
    public Param addParam() {
        Param p = new Param();
        if (nestedParams == null) {
            nestedParams = new ArrayList();
        }
        nestedParams.add(p);
        return p;
    }
    
    /**
     * execute the task
     * @throws BuildException
     */
    public void execute() throws BuildException{
        if (file == null){
            throw new BuildException("null file - file should be an archive");
        }
        if (!file.endsWith(".zip") && !file.endsWith(".jar")) {
            throw new BuildException("file: " + file + " is not an archive");
        }
        Properties props;
        try {
            props = getProperties();
        } catch (IOException e) {
            log.error("Error retrieving installation properties", e);
            throw new BuildException(e);
        }
        AdminCommandsServiceMBean acs;
        try {
            acs = getAdminCommandsService();
        } catch (IOException e) {
            log.error("Caught an exception getting the installation service", e);
            throw new BuildException(e);
        }
        try {
            acs.installComponent(file, props);
        } catch (Exception e) {
            log.error("Error installing component", e);
            throw new BuildException(e);
        }
    }
    
    private Properties getProperties() throws IOException {
        Properties props = new Properties();
        if (paramsFile != null) {
            props.load(new FileInputStream(paramsFile));
        }
        if (nestedParams != null) {
            for (Iterator iter = nestedParams.iterator(); iter.hasNext();) {
                Param p = (Param) iter.next();
                props.setProperty(p.getValue(), p.getName());
            }
        }
        return props;
    }
    
    private static class Param {
        private String name;
        private String value;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
}