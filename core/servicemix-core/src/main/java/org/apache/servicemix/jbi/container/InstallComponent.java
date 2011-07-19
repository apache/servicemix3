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

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Installs a component using either a File URL or the maven groupId, artifactId and optional versions.
 *
 * @org.apache.xbean.XBean element="installComponent"
 * description="Installs a shared library"
 *
 * @version $Revision$
 */
public class InstallComponent extends DeploySupport {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(InstallSharedLibrary.class);

    private Properties properties = new Properties();
    private String componentName;

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getComponentName() {
        if (componentName == null) {
            return getArtifactId();
        }
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    protected void doDeploy() throws Exception {
        String name = getComponentName();
        if (name == null) {
            throw new IllegalArgumentException("You must specify a componentName or an artifactId property");
        }

        String file = getFile();

        LOGGER.info("Deploying component: {}", file);
        getCommandsService().installComponent(file, getProperties(), isDeferException());

        getCommandsService().startComponent(name);
    }

}