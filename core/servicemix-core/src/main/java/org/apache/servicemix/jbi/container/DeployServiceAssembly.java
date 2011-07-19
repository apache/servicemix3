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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deploys a service assembly using either a File URL or the maven groupId, artifactId and optional versions.
 *
 * @version $Revision$
 * @org.apache.xbean.XBean element="deployServiceAssembly" description="Deploys a service assembly"
 */
public class DeployServiceAssembly extends DeploySupport {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(InstallSharedLibrary.class);

    private String serviceAssemblyName;

    public DeployServiceAssembly() {
        setType(".zip");
    }

    public String getServiceAssemblyName() {
        if (serviceAssemblyName == null) {
            return getArtifactId();
        }
        return serviceAssemblyName;
    }

    public void setServiceAssemblyName(String serviceAssemblyName) {
        this.serviceAssemblyName = serviceAssemblyName;
    }

    protected void doDeploy() throws Exception {
        String name = getServiceAssemblyName();
        if (name == null) {
            throw new IllegalArgumentException("You must specify a serviceAssemblyName or an artifactId property");
        }

        String file = getFile();

        LOGGER.info("Deploying shared library: {}", file);
        getCommandsService().deployServiceAssembly(file, isDeferException());
        getCommandsService().startServiceAssembly(name);
    }

}