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
package org.apache.servicemix.packaging.parts;

import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.Connection;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

/**
 * The Edit Part Factory for GEF to create the Edit Parts based on the model
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeploymentEditPartFactory implements EditPartFactory {

	private IProject project;

	public EditPart createEditPart(EditPart context, Object modelElement) {
		EditPart newEditPart = getInstance(modelElement);
		if (newEditPart != null)
			newEditPart.setModel(modelElement);
		return newEditPart;
	}

	private EditPart getInstance(Object modelElement) {
		if (modelElement instanceof DeploymentDiagram) {
			return new DeploymentDiagramEditPart(project);
		}
		if (modelElement instanceof BindingComponent) {
			return new BindingComponentEditPart();
		}
		if (modelElement instanceof ServiceAssembly) {
			return new ServiceAssemblyEditPart();
		}
		if (modelElement instanceof ServiceUnit) {
			return new ServiceUnitEditPart();
		}
		if (modelElement instanceof Connection) {
			return new ConnectionEditPart();
		}
		return null;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
