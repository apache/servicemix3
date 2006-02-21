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

import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The content provider for a list of service names from a deployment diagram
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceNameContentProvider implements IStructuredContentProvider {

	public Object[] getElements(Object arg0) {
		if (arg0 instanceof DeploymentDiagram)
			return ServiceNameHelper.getServiceNames((DeploymentDiagram) arg0)
					.toArray();
		return new Object[] {};
	}

	public void dispose() {
		// Nothing to do
	}

	public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		// Nothing to do
	}

}
