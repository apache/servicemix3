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
package org.apache.servicemix.components.util;

import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;

/**
 * component-supplied methods for managing Service Unit deployments
 * 
 * @version $Revision$
 */
public class ServiceUnitManagerSupport implements ServiceUnitManager {
    /**
     * Deploy a Service Unit to the Component
     * 
     * @param serviceUnitName
     * @param serviceUnitRootPath
     * @return a deployment status message
     * @throws DeploymentException
     */
    public String deploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        return serviceUnitRootPath;
    }

    /**
     * Initialize Deployment
     * 
     * @param serviceUnitName
     * @param serviceUnitRootPath
     * @throws DeploymentException
     */
    public void init(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
    }

    /**
     * shutdown
     * 
     * @param saerviceUnitName
     * @throws DeploymentException
     */
    public void shutDown(String saerviceUnitName) throws DeploymentException {
    }

    /**
     * start the deployment
     * 
     * @param serviceUnitName
     * @throws DeploymentException
     */
    public void start(String serviceUnitName) throws DeploymentException {
    }

    /**
     * stop the deployment
     * 
     * @param serviceUnitName
     * @throws DeploymentException
     */
    public void stop(String serviceUnitName) throws DeploymentException {
    }

    /**
     * Undeploy a Service Unit form the Component
     * 
     * @param serviceUnitName
     * @param serviceUnitRootPath
     * @return a status message
     * @throws DeploymentException
     */
    public String undeploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        return serviceUnitRootPath;
    }
}