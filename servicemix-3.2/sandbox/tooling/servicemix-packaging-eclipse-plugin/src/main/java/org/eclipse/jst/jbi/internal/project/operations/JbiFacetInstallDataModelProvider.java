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
package org.eclipse.jst.jbi.internal.project.operations;

import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
import org.eclipse.jst.j2ee.project.facet.J2EEModuleFacetInstallDataModelProvider;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class JbiFacetInstallDataModelProvider extends
		J2EEModuleFacetInstallDataModelProvider {

	private static final String JBI_V1_0 = "1.0";

	private static final String JBI_PROJECT_FACET = "jst.jbi.component";

	public static final IProjectFacetVersion JBI_10 = ProjectFacetsManager
			.getProjectFacet(JBI_PROJECT_FACET).getVersion(JBI_V1_0); //$NON-NLS-1$

	public Set getPropertyNames() {
		Set names = super.getPropertyNames();
		names.add(CONFIG_FOLDER);
		names.add(IJbiFacetInstallDataModelProperties.JAVA_SOURCE_FOLDER);
		return names;
	}

	public Object getDefaultProperty(String propertyName) {
		if (propertyName.equals(FACET_ID)) {
			return JBI_PROJECT_FACET;
		} else if (propertyName.equals(CONFIG_FOLDER)) {
			return "src/main/resources";
		} else if (propertyName
				.equals(IJbiFacetInstallDataModelProperties.JAVA_SOURCE_FOLDER)) {
			return "src/main/java";
		}
		return super.getDefaultProperty(propertyName);
	}

	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
		return J2EEVersionConstants.J2EE_1_4_ID;
	}

	public boolean isPropertyEnabled(String propertyName) {
		return super.isPropertyEnabled(propertyName);
	}

	public boolean propertySet(String propertyName, Object propertyValue) {
		boolean status = false;
		status = super.propertySet(propertyName, propertyValue);
		return status;
	}

	public IStatus validate(String propertyName) {
		return OK_STATUS;
	}

}
