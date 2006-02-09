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
package org.apache.servicemix.packaging;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The factory can be used to get a list of the component artifacts that have
 * been registered with the preferences in Eclipse
 * 
 * TODO Should be extended to enable gathering components from plugins and also
 * from the associated ServiceMix runtime
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class ComponentArtifactFactory {

	private final static String PREFERENCE_ID = "com.unity.jbi.deployer.preferences.serviceBundles";

	private static Map<String, Long> cachedArtifactsUpdateStamps = new HashMap<String, Long>();

	private static Map<String, ComponentArtifact> cachedArtifacts = new HashMap<String, ComponentArtifact>();

	public static List<ComponentArtifact> getComponentArtifacts() {
		String serviceBundlesString = DeployerPlugin.getDefault()
				.getPreferenceStore().getString(PREFERENCE_ID);
		List<ComponentArtifact> componentArtifacts = new LinkedList<ComponentArtifact>();
		if (serviceBundlesString != null
				&& !serviceBundlesString.trim().equals("")) {
			String[] serviceLocations;
			serviceLocations = serviceBundlesString.split(";");
			for (String archivePath : serviceLocations) {
				if (newOrUpdated(archivePath))
					componentArtifacts.add(addToCache(archivePath));
				else
					componentArtifacts.add(getFromCache(archivePath));

			}
		}
		return componentArtifacts;
	}

	private static ComponentArtifact getFromCache(String archivePath) {
		return cachedArtifacts.get(archivePath);
	}

	private static ComponentArtifact addToCache(String archivePath) {
		ComponentArtifact newArtifact;
		try {
			System.out.println("Adding to cache! " + archivePath);
			newArtifact = new ComponentArtifact(archivePath);
			cachedArtifactsUpdateStamps.put(archivePath,
					getModified(archivePath));
			cachedArtifacts.put(archivePath, newArtifact);
			return newArtifact;
		} catch (InvalidArchiveException e) {
			System.out.println("Archive on path " + archivePath
					+ " is not valid and will be dropped");
			return null;
		}
	}

	private static boolean newOrUpdated(String archivePath) {
		if (cachedArtifactsUpdateStamps.containsKey(archivePath)) {
			if (!getModified(archivePath).equals(cachedArtifactsUpdateStamps
					.get(archivePath)))
				return true;
		} else
			return true;
		return false;
	}

	private static Long getModified(String archivePath) {
		File archiveFile = new File(archivePath);
		return Long.valueOf(archiveFile.lastModified());
	}

	public static void setComponentArtifacts(
			List<ComponentArtifact> serviceArtifacts) {
		StringBuffer sb = null;
		for (ComponentArtifact service : serviceArtifacts) {
			if (sb == null)
				sb = new StringBuffer();
			else {
				sb.append(";");
			}
			sb.append(service);
		}
		if (sb != null)
			DeployerPlugin.getDefault().getPreferenceStore().setValue(
					PREFERENCE_ID, sb.toString());
		else
			DeployerPlugin.getDefault().getPreferenceStore().setValue(
					PREFERENCE_ID, "");
	}
}
