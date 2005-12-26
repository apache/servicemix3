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
import org.apache.servicemix.jbi.framework.FrameworkInstallationService;
import org.apache.tools.ant.BuildException;

import java.io.IOException;

/**
 * Install a shared library
 * @version $Revision$
 */
public class InstallSharedLibraryTask extends JbiTask {
    private static final Log log = LogFactory.getLog(InstallSharedLibraryTask.class);
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
    public void execute() throws BuildException{
        if (file == null){
            throw new BuildException("null file - file should be an archive");
        }
        if (file.endsWith(".zip") || file.endsWith(".jar")){
            try {
                FrameworkInstallationService is = getInstallationService();
                is.installSharedLibrary(file);
            }
            catch (IOException e) {
                log.error("Caught an exception getting the installation service",e);
                throw new BuildException(e);
            }
        }else {
            throw new BuildException("file: " + file + " is not an archive");
        }
    }
}