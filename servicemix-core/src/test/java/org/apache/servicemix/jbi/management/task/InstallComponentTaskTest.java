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
package org.apache.servicemix.jbi.management.task;

import org.apache.servicemix.jbi.management.task.InstallComponentTask;
import org.apache.servicemix.jbi.util.FileUtil;

import java.io.File;
import java.net.URL;

/**
 *
 * InstallComponentTaskTest
 */
public class InstallComponentTaskTest extends JbiTaskSupport {
    
    
    private InstallComponentTask installComponentTask;
    private File rootDir = new File("testWDIR");
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        this.container.setRootDir(rootDir.getPath());
        super.setUp();        
        installComponentTask = new InstallComponentTask(){};
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
        URL url = getClass().getClassLoader().getResource("org/servicemix/jbi/installation/testarchive.jar");
        if (url != null) {
            String file = url.getFile();
            installComponentTask.setFile(file);
            installComponentTask.init();
            installComponentTask.execute();
            File testFile = new File(rootDir, container.getName() + File.separator + "components" + File.separator
                    + "ComponentTest");
            assertTrue(testFile.exists());
            FileUtil.deleteFile(rootDir);
        }
    }
}
