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
package org.apache.servicemix.jbi.installation;

import org.apache.servicemix.jbi.container.JBIContainer;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

/**
 * 
 * Tests the installation of a standard component, this is actual a dummy
 * component that doesn't do anything though we are validating the parsing of
 * the component descriptor and its deployment
 * 
 * @version $Revision$
 */
public class ComponentInstallationTest extends TestCase {
	protected JBIContainer container = new JBIContainer();

	private File tempRootDir;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		container.setCreateMBeanServer(true);
		container.setMonitorInstallationDirectory(true);
		tempRootDir = File.createTempFile("servicemix", "rootDir");
		tempRootDir.delete();
		File tempTemp = new File(tempRootDir.getAbsolutePath() + "/temp");
		if (!tempTemp.mkdirs())
			fail("Unable to create temporary working root directory ["
					+ tempTemp.getAbsolutePath() + "]");

		System.out.println("Using temporary root directory ["
				+ tempRootDir.getAbsolutePath() + "]");

		container.setRootDir(tempRootDir.getAbsolutePath());
		container.init();
		container.start();

	}

	public void testInstallation() throws Exception {
		try {
			// Get the component
			URL componentResource = getClass().getClassLoader().getResource(
					"component-dummy-1.0-jbi-installer.jar");
			assertNotNull(
					"The component JAR component-dummy-1.0-jbi-installer.jar is missing from the classpath",
					componentResource);
			container.installArchive(componentResource.toExternalForm());

			URL assemblyResource = getClass().getClassLoader().getResource(
					"dummy-assembly.jar");
			assertNotNull(
					"The assembly JAR dummy-assembly.jar is missing from the classpath",
					assemblyResource);
			container.installArchive(getClass().getClassLoader().getResource(
					"dummy-assembly.jar").toExternalForm());

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	} /*
		 * @see TestCase#tearDown()
		 */

	protected void tearDown() throws Exception {
		super.tearDown();
		container.shutDown();
		deleteDir(tempRootDir);
	}

	public static boolean deleteDir(File dir) {
		System.out.println("Deleting directory : "+dir.getAbsolutePath());
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}
}
