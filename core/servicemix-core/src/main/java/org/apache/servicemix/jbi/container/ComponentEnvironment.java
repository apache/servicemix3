/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.jbi.container;

import java.io.File;

import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;

/**
 * Holder for environment infomation
 * 
 * @version $Revision$
 */
public class ComponentEnvironment {
    
    private File installRoot;
    private File workspaceRoot;
    private File componentRoot;
    private File stateFile;
    private ComponentMBeanImpl localConnector;

    /**
     * @return Returns the installRoot.
     */
    public File getInstallRoot() {
        return installRoot;
    }

    /**
     * @param installRoot The installRoot to set.
     */
    public void setInstallRoot(File installRoot) {
        this.installRoot = installRoot;
    }

    /**
     * @return Returns the workspaceRoot.
     */
    public File getWorkspaceRoot() {
        return workspaceRoot;
    }

    /**
     * @param workspaceRoot The workspaceRoot to set.
     */
    public void setWorkspaceRoot(File workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    /**
     * @return Returns the localConnector.
     */
    public ComponentMBeanImpl getLocalConnector() {
        return localConnector;
    }

    /**
     * @param localConnector The localConnector to set.
     */
    public void setLocalConnector(ComponentMBeanImpl localConnector) {
        this.localConnector = localConnector;
    }

    /**
     * @return Returns the componentRoot.
     */
    public File getComponentRoot() {
        return componentRoot;
    }

    /**
     * @param componentRoot The componentRoot to set.
     */
    public void setComponentRoot(File componentRoot) {
        this.componentRoot = componentRoot;
    }

    /**
     * @return Returns the stateFile.
     */
    public File getStateFile() {
        return stateFile;
    }

    /**
     * @param stateFile The stateFile to set.
     */
    public void setStateFile(File stateFile) {
        this.stateFile = stateFile;
    }
    
}
