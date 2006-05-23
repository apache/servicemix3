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
package org.apache.servicemix.jbi.loaders;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.management.DeploymentException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.schemas.deployment.ClassPath;
import org.apache.servicemix.schemas.deployment.Component;
import org.apache.servicemix.schemas.deployment.Descriptor;
import org.apache.servicemix.schemas.deployment.DescriptorFactory;

/**
 * Build custom class loader
 * 
 * @version $Revision$
 */
public class ClassLoaderService {
	private static final Log log = LogFactory.getLog(ClassLoaderService.class);

	private Map<String, ClassLoader> sharedLibraryMap = new ConcurrentHashMap<String, ClassLoader>();

	/**
	 * Buld a Custom ClassLoader
	 * 
	 * @param dir
	 * @param classPathNames
	 * @param parentFirst
	 * @return ClassLoader
	 * @throws MalformedURLException
	 * @throws MalformedURLException
	 * @throws DeploymentException
	 */
	public InstallationClassLoader buildClassLoader(File dir,
			List<String> classPathNames, boolean parentFirst)
			throws MalformedURLException, DeploymentException {
		return buildClassLoader(dir, classPathNames, parentFirst, null);
	}

	/**
	 * Buld a Custom ClassLoader
	 * 
	 * @param dir
	 * @param classPathNames
	 * @param parentFirst
	 * @param list
	 * @return ClassLoader
	 * @throws MalformedURLException
	 * @throws MalformedURLException
	 * @throws DeploymentException
	 */
	public InstallationClassLoader buildClassLoader(File dir,
			List<String> classPathNames, boolean parentFirst,
			List<Component.SharedLibrary> list) throws MalformedURLException,
			DeploymentException {
		InstallationClassLoader result = null;

		// Make the current ClassLoader the parent
		ClassLoader parent = getClass().getClassLoader();		
		
		URL[] urls = new URL[classPathNames.size()];
		for (int i = 0; i < classPathNames.size(); i++) {
			File file = new File(dir, classPathNames.get(i));
			if (!file.exists()) {
				throw new DeploymentException("Unable to add File " + file
						+ " to class path as it doesn't exist: "
						+ file.getAbsolutePath());
			}
			urls[i] = file.toURL();
		}
		if (parentFirst) {
			result = new ParentFirstClassLoader(urls, parent);
		} else {
			result = new SelfFirstClassLoader(urls, parent);
		}
		if (list != null) {
			for (Component.SharedLibrary lib : list) {
				String name = lib.getContent();
				ClassLoader cl = (ClassLoader) sharedLibraryMap.get(name);
				if (cl != null) {
					result.addSharedLibraryLoader(cl);
				} else {
					log.error("Could not find shared library: " + name);
				}
			}
		}
		return result;
	}

	/**
	 * Add a shared library
	 * 
	 * @param dir
	 * @param sl
	 * @throws MalformedURLException
	 */
    public void addSharedLibrary(File dir, Descriptor.SharedLibrary sl)
			throws MalformedURLException {
		if (sl != null) {
			boolean parentFirst = DescriptorFactory.isSLParentFirst(sl);
			String name = sl.getIdentification().getName();
			
			// Make the current ClassLoader the parent
			ClassLoader parent = getClass().getClassLoader();		
			
			ClassPath cp = sl.getSharedLibraryClassPath();
			List<String> classPathNames = cp.getPathElement();
			URL[] urls = new URL[classPathNames.size()];
			for (int i = 0; i < classPathNames.size(); i++) {
				File file = new File(dir, classPathNames.get(i));
				urls[i] = file.toURL();
			}
			if (parentFirst) {
				sharedLibraryMap.put(name, new ParentFirstClassLoader(urls,
						parent));
			} else {
				sharedLibraryMap.put(name, new SelfFirstClassLoader(urls,
						parent));
			}
		}
	}

	/**
	 * Remove a SharedLibrary
	 * 
	 * @param id
	 */
	public void removeSharedLibrary(String id) {
		sharedLibraryMap.remove(id);
	}
    
    /**
     * returns true if a shared library is already installed
     * @param name
     * @return true/false
     */
    public  boolean containsSharedLibrary(String name){
        return sharedLibraryMap.containsKey(name);
    }
}
