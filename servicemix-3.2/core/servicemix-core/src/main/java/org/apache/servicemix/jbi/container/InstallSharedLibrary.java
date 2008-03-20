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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Installs a shared library using either a File URL or the maven groupId, artifactId and optional versions.
 *
 * @org.apache.xbean.XBean element="installSharedLibrary"
 * description="Installs a shared library"
 *
 * @version $Revision: 1.1 $
 */
public class InstallSharedLibrary extends DeploySupport {
    private static final transient Log LOG = LogFactory.getLog(InstallSharedLibrary.class);

    protected void doDeploy() throws Exception {
        String file = getFile();

        LOG.info("Deploying shared library: " + file);
        getCommandsService().installSharedLibrary(file, isDeferException());
    }
}
