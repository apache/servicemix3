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
package org.apache.servicemix.packaging.parts.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicemix.packaging.parts.DeploymentDiagramEditPart;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.PlatformUI;

/**
 * The content provider for an embedded artifact lookup
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class EmbeddedArtifactContentProvider implements
		IStructuredContentProvider {

	private String extension;

	public EmbeddedArtifactContentProvider(String extension) {
		this.extension = extension;
	}

	public Object[] getElements(Object arg0) {		
		if (arg0 instanceof DeploymentDiagramEditPart) {
			IProject project = ((DeploymentDiagramEditPart) arg0).getProject();
			try {
				List matchingResources = new ArrayList();
				for (IResource resource : project.members()) {
					matchingResources = internalFindByExtension(resource,
							extension, matchingResources);
				}				
				return matchingResources.toArray();
			} catch (Exception e) {
				MessageDialog.openError(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						"Unable to list resources in " + project.getName(), e
								.getMessage());
			}

		}
		return new Object[] {};
	}

	/**
	 * Used by the <code>findResourceByExtension</code> method to iterate
	 * through the project resources looking for resources with a given
	 * extension
	 * 
	 * @param resource
	 *            The resource to check
	 * @param extension
	 *            The extension to look for
	 * @return An array of the resources that match this extension
	 * @throws CoreException
	 */
	private List internalFindByExtension(IResource resource, String extension,
			List currentList) throws CoreException {
		if (resource.isDerived())
			return currentList;
		if (resource instanceof IFolder) {
			for (IResource folderChild : ((IFolder) resource).members()) {
				currentList = internalFindByExtension(folderChild, extension,
						currentList);
			}
			return currentList;
		}
		if (extension.equals(resource.getFileExtension())) {
			currentList.add(resource);
			return currentList;
		} else
			return currentList;
	}

	public void dispose() {
		// Nothing to do
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// Nothing to do
	}

}
