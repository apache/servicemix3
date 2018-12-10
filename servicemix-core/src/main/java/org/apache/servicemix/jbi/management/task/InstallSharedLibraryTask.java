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

import java.io.File;

import org.apache.servicemix.jbi.framework.AdminCommandsServiceMBean;
import org.apache.tools.ant.BuildException;

/**
 * Install a shared library
 * @version $Revision: 359151 $
 */
public class InstallSharedLibraryTask extends JbiTask {
    
    private String file; //shared library URI to install
    
    /**
     * @return Returns the file.
     */
    public String getFile() {
        return file;
    }
    /**
     * @param file The shared library URI to set.
     */
    public void setFile(String file) {
        this.file = file;
    }
    
    /**
     * execute the task
     * @throws BuildException
     */
    public void doExecute(AdminCommandsServiceMBean acs) throws Exception {
        if (file == null){
            throw new BuildException("null file - file should be an archive");
        }
        if (!file.endsWith(".zip") && !file.endsWith(".jar")) {
            throw new BuildException("file: " + file + " is not an archive");
        }
        File archive = new File(file);
        if (!archive.isFile()) {
            throw new BuildException("file: " + file + " not found");
        }
        acs.installSharedLibrary(archive.getAbsolutePath());
    }
    
}