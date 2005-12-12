/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.jbi.framework;

import java.net.URLClassLoader;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public class ClassLoaderTest extends TestCase {

	public static class TestClass {
	}
	
	public void testParentFirstClassLoader() throws Exception {
		URLClassLoader pcl = (URLClassLoader) getClass().getClassLoader();
		ClassLoader clsLoader = new ParentFirstClassLoader(pcl.getURLs(), pcl);
		Class  clazz = clsLoader.loadClass(TestClass.class.getName());
		assertSame(TestClass.class, clazz);
	}
	
	public void testSelfFirstClassLoader() throws Exception {
		URLClassLoader pcl = (URLClassLoader) getClass().getClassLoader();
		ClassLoader clsLoader = new SelfFirstClassLoader(pcl.getURLs(), pcl);
		Class  clazz = clsLoader.loadClass(TestClass.class.getName());
		assertNotSame(TestClass.class, clazz);
	}
	
}
