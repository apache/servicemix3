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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.jbi.JBIException;
import javax.jbi.component.Bootstrap;
import javax.jbi.component.InstallationContext;
import javax.management.ObjectName;

import org.apache.servicemix.jbi.installation.AbstractManagementTest;
import org.apache.servicemix.jbi.installation.Bootstrap1;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.tools.ant.Project;

/**
 * 
 * InstallComponentTaskTest
 */
public class InstallComponentTaskTest extends JbiTaskSupport {

    private InstallComponentTask installComponentTask;

    private File rootDir = new File("target/testWDIR");

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        FileUtil.deleteFile(rootDir);
        this.container.setRootDir(rootDir.getPath());
        super.setUp();
        installComponentTask = new InstallComponentTask() {
        };
        installComponentTask.setProject(new Project());
        installComponentTask.init();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        installComponentTask.close();
        super.tearDown();
    }

    public void testInstallation() throws Exception {
        Bootstrap1.setDelegate(new Bootstrap() {
            public void cleanUp() throws JBIException {
            }

            public ObjectName getExtensionMBeanName() {
                return null;
            }

            public void init(InstallationContext installContext) throws JBIException {
            }

            public void onInstall() throws JBIException {
            }

            public void onUninstall() throws JBIException {
            }
        });
        String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
        installComponentTask.setFile(installJarUrl);
        installComponentTask.init();
        installComponentTask.execute();
        File testFile = new File(rootDir, "components" + File.separator + "component1");
        assertTrue(testFile.exists());
        FileUtil.deleteFile(rootDir);
    }

    protected File createInstallerArchive(String jbi) throws Exception {
        InputStream is = AbstractManagementTest.class.getResourceAsStream(jbi + "-jbi.xml");
        File jar = File.createTempFile("jbi", ".zip");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jar));
        jos.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        jos.write(buffer);
        jos.closeEntry();
        jos.close();
        is.close();
        return jar;
    }
}
