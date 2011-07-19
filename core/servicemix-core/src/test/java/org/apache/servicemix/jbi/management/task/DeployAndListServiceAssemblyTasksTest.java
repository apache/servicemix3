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
package org.apache.servicemix.jbi.management.task;

import java.io.File;
import java.net.URL;

import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.tools.ant.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InstallComponentTaskTest
 */
public class DeployAndListServiceAssemblyTasksTest extends JbiTaskSupport {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(DeployAndListServiceAssemblyTasksTest.class);

    private static final String XML_OUTPUT_PROPERTY = "test.xml.output";

    private DeployServiceAssemblyTask deployServiceAssembliesTask;

    private File rootDir = new File("target/testWDIR");

    private ListServiceAssembliesTask listServiceAssembliesTask;

    private Project project = new Project();

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        FileUtil.deleteFile(rootDir);
        this.container.setRootDir(rootDir.getPath());
        super.setUp();
        deployServiceAssembliesTask = new DeployServiceAssemblyTask() {
        };
        deployServiceAssembliesTask.setProject(new Project());
        deployServiceAssembliesTask.init();

        listServiceAssembliesTask = new ListServiceAssembliesTask();
        listServiceAssembliesTask.setProject(project);
        listServiceAssembliesTask.init();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        deployServiceAssembliesTask.close();
        super.tearDown();
    }

    public void testInstallationAndList() throws Exception {
        URL url = getClass().getClassLoader().getResource("org/apache/servicemix/jbi/installation/testassembly.jar");

        listServiceAssembliesTask.setServiceAssemblyName("sa");
        listServiceAssembliesTask.setXmlOutput(XML_OUTPUT_PROPERTY);
        listServiceAssembliesTask.execute();

        LOGGER.info(project.getProperty(XML_OUTPUT_PROPERTY));

        if (url != null) {
            String file = url.getFile();
            deployServiceAssembliesTask.setFile(file);
            deployServiceAssembliesTask.init();
            deployServiceAssembliesTask.execute();
            File testFile = new File(rootDir, "service-assemblies" + File.separator + "sa");
            assertTrue(testFile.exists());
        }

        listServiceAssembliesTask.setServiceAssemblyName("sa");
        listServiceAssembliesTask.setXmlOutput(XML_OUTPUT_PROPERTY);
        listServiceAssembliesTask.execute();

        LOGGER.info(project.getProperty(XML_OUTPUT_PROPERTY));

        FileUtil.deleteFile(rootDir);
    }

}
