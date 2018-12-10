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
package javax.jbi.component;

import javax.jbi.management.DeploymentException;

public interface ServiceUnitManager
{
    String deploy(String serviceUnitName, String serviceUnitRootPath)
        throws DeploymentException;

    void init(String serviceUnitName, String serviceUnitRootPath)
        throws DeploymentException;

    void start(String serviceUnitName)
        throws DeploymentException;

    void stop(String serviceUnitName)
        throws DeploymentException;

    void shutDown(String serviceUnitName)
        throws DeploymentException;

    String undeploy(String serviceUnitName, String serviceUnitRootPath)
        throws DeploymentException;
}
