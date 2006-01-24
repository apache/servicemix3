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
package org.apache.servicemix.packaging.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.servicemix.packaging.DeployerEditor;
import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.jface.action.Action;

/**
 * The Eclipse Action to deploy from the DeployerEditor
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeployServiceAction extends Action {

	public static final String COMPONENT_ID = "DEPLOY_SERVICE_ACTION";

	private EditPartViewer viewer;

	private DeployerEditor editor;

	public DeployServiceAction(EditPartViewer viewer, DeployerEditor editor) {
		super();
		this.viewer = viewer;
		this.editor = editor;
	}

	@Override
	public String getId() {
		return COMPONENT_ID;
	}

	@Override
	public String getText() {
		return "Deploy";
	}

	@Override
	public boolean isEnabled() {
		List selectedParts = viewer.getSelectedEditParts();
		for (Iterator partIterator = selectedParts.iterator(); partIterator
				.hasNext();) {
			EditPart editPart = (EditPart) partIterator.next();
			if (editPart.getModel() instanceof AbstractComponent) {
				AbstractComponent component = (AbstractComponent) editPart
						.getModel();
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		try {
			List selectedParts = viewer.getSelectedEditParts();

			// Gather up a list of the services
			List<AbstractComponent> components = new ArrayList<AbstractComponent>();
			for (Iterator partIterator = selectedParts.iterator(); partIterator
					.hasNext();) {
				EditPart editPart = (EditPart) partIterator.next();
				if (editPart.getModel() instanceof AbstractComponent) {
					AbstractComponent component = (AbstractComponent) editPart
							.getModel();
					components.add(component);
				}
			}

			// Deploy the services
			for (AbstractComponent component : components) {
				if (component instanceof BindingComponent) {					
					((BindingComponent) component).getComponentArtifact()
							.getDeploymentEngine().deployBindingComponent(
									editor.getProject(),
									(BindingComponent) component);
				} else if (component instanceof ServiceAssembly) {
					((ServiceAssembly) component).getComponentArtifact()
							.getDeploymentEngine().deployServiceAssembly(
									editor.getProject(),
									(ServiceAssembly) component);
				}
			}
		} catch (Exception e) {

		}
	}
}
