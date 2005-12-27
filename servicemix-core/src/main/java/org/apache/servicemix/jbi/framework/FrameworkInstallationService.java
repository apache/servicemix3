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
package org.apache.servicemix.jbi.framework;

import javax.jbi.management.DeploymentException;
import javax.jbi.management.InstallationServiceMBean;

/**
 * Installation Service - installs/uninstalls archives
 * 
 * @version $Revision$
 */
public interface FrameworkInstallationService extends InstallationServiceMBean {
    /**
     * Install an archive
     * 
     * @param location
     * @throws DeploymentException
     */
    public void install(String location) throws DeploymentException;
    
    /**
    * Install an archive
    * 
    * @param location
     * @param autostart 
    * @throws DeploymentException
    */
   public void install(String location,boolean autostart) throws DeploymentException;
}
