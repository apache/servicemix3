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
package org.apache.servicemix.jbi.installation;

import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.util.FileUtil;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

/**
 * 
 * Tests the installation of a standard component, this is actual a dummy
 * component that doesn't do anything though we are validating the parsing of
 * the component descriptor and its deployment
 * 
 * @version $Revision: 411178 $
 */
public class ComponentAssemblyInstallationTest extends TestCase {
	protected JBIContainer container = new JBIContainer();

	private File tempRootDir;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		container.setCreateMBeanServer(false);
		container.setMonitorInstallationDirectory(false);
		tempRootDir = File.createTempFile("servicemix", "rootDir");
		tempRootDir.delete();
		File tempTemp = new File(tempRootDir.getAbsolutePath() + "/temp");
		if (!tempTemp.mkdirs())
			fail("Unable to create temporary working root directory ["
					+ tempTemp.getAbsolutePath() + "]");

		System.out.println("Using temporary root directory ["
				+ tempRootDir.getAbsolutePath() + "]");

		container.setRootDir(tempRootDir.getAbsolutePath());
        container.setMonitorDeploymentDirectory(false);
        container.setMonitorInstallationDirectory(false);
		container.init();
		container.start();

	}

	public void testInvalidComponentInstallation() throws Exception {
		try {			
			// Get the component
			URL componentResource = getClass().getClassLoader().getResource("logger-component-1.0-jbi-installer.zip");
			assertNotNull("The component JAR logger-component-1.0-jbi-installer is missing from the classpath", componentResource);
			container.installArchive(componentResource.toExternalForm());            
            fail("Missing bootstrap should have thrown exception?");
		} catch (Exception e) {
			
		}
	}

    public void testResourceInstallation() throws Exception {
		try {
			URL assemblyResource = getClass().getClassLoader().getResource("sample-jbi.zip");
			assertNotNull("The assembly JAR sample-jbi.jar is missing from the classpath",assemblyResource);
			String url = assemblyResource.toExternalForm();
            container.installArchive(url);
			Thread.sleep(10000);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

    /*
		 * @see TestCase#tearDown()
		 */

	protected void tearDown() throws Exception {
		super.tearDown();
		container.shutDown();
		FileUtil.deleteFile(tempRootDir);
	}

}
