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
package org.eclipse.jst.jbi.internal.deployables;

import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacet;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.web.internal.deployables.ComponentDeployable;

public class JBIComponentDeployable extends ComponentDeployable {

	public JBIComponentDeployable(IProject project, IVirtualComponent component) {
		super(project);
	}

	public String getContextRoot() {
		Properties props = component.getMetaProperties();
		if (props.containsKey("context-root")) //$NON-NLS-1$
			return props.getProperty("context-root"); //$NON-NLS-1$
		return component.getName();
	}

	public String getURI(IModule module) {
		IVirtualComponent comp = ComponentCore.createComponent(module
				.getProject());
		String aURI = null;
		if (comp != null) {
			if (!comp.isBinary()
					&& isProjectOfType(module.getProject(), "jst.jbi")) {
				IVirtualReference ref = component.getReference(comp.getName());
				aURI = ref.getRuntimePath().append(
						comp.getName() + "-installer.jar").toString(); //$NON-NLS-1$
			}
		}

		if (aURI != null && aURI.length() > 1 && aURI.startsWith("/")) //$NON-NLS-1$
			aURI = aURI.substring(1);
		return aURI;
	}

	private boolean isProjectOfType(IProject project, String typeID) {
		IFacetedProject facetedProject = null;
		try {
			facetedProject = ProjectFacetsManager.create(project);
		} catch (CoreException e) {
			return false;
		}

		if (facetedProject != null
				&& ProjectFacetsManager.isProjectFacetDefined(typeID)) {
			IProjectFacet projectFacet = ProjectFacetsManager
					.getProjectFacet(typeID);
			return projectFacet != null
					&& facetedProject.hasProjectFacet(projectFacet);
		}
		return false;
	}

	public String getVersion() {
		IFacetedProject facetedProject = null;
		try {
			facetedProject = ProjectFacetsManager
					.create(component.getProject());
			if (facetedProject != null
					&& ProjectFacetsManager.isProjectFacetDefined("jst.jbi")) {
				IProjectFacet projectFacet = ProjectFacetsManager
						.getProjectFacet("jst.jbi");
				return facetedProject.getInstalledVersion(projectFacet)
						.getVersionString();
			}
		} catch (Exception e) {
			// Ignore
		}
		return "1.0"; //$NON-NLS-1$
	}
}
