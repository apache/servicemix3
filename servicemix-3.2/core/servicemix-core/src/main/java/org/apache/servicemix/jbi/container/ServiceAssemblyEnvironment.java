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

import org.apache.servicemix.jbi.util.FileUtil;

public class ServiceAssemblyEnvironment {

    private File rootDir;
    private File installDir;
    private File susDir;
    private File stateFile;
    
    public ServiceAssemblyEnvironment() {
    }

    /**
     * @return Returns the installRoot.
     */
    public File getInstallDir() {
        return installDir;
    }

    /**
     * @param installRoot The installRoot to set.
     */
    public void setInstallDir(File installRoot) {
        this.installDir = installRoot;
    }

    /**
     * @return Returns the susRoot.
     */
    public File getSusDir() {
        return susDir;
    }

    /**
     * @param susRoot The susRoot to set.
     */
    public void setSusDir(File susRoot) {
        this.susDir = susRoot;
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

    /**
     * @return Returns the rootDir.
     */
    public File getRootDir() {
        return rootDir;
    }

    /**
     * @param rootDir The rootDir to set.
     */
    public void setRootDir(File rootDir) {
        this.rootDir = rootDir;
    }

    public File getServiceUnitDirectory(String componentName, String suName) {
        File compDir = FileUtil.getDirectoryPath(susDir, componentName);
        return FileUtil.getDirectoryPath(compDir, suName);
    }

}
