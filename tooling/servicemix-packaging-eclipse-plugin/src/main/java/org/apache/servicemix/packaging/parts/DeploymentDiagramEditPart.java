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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.AbstractConnectableService;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.commands.ComponentCreateCommand;
import org.apache.servicemix.packaging.model.commands.ComponentSetConstraintCommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The GEF edit part for a deployment diagram
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeploymentDiagramEditPart extends AbstractGraphicalEditPart2
		implements PropertyChangeListener, IPropertySource {

	private static final String DEPLOY_PATH = "deployPath";

	private static final String INSTALL_PATH = "installPath";

	private static class ShapesXYLayoutEditPolicy extends XYLayoutEditPolicy {

		protected Command createAddCommand(EditPart child, Object constraint) {
			// not used
			return null;
		}

		protected Command createChangeConstraintCommand(
				ChangeBoundsRequest request, EditPart child, Object constraint) {
			if (child instanceof AbstractComponentEditPart
					&& constraint instanceof Rectangle) {
				return new ComponentSetConstraintCommand(
						(AbstractComponent) ((AbstractComponentEditPart) child)
								.getModel(), request, (Rectangle) constraint);
			}
			return super.createChangeConstraintCommand(request, child,
					constraint);
		}

		protected Command createChangeConstraintCommand(EditPart child,
				Object constraint) {
			// not used
			return null;
		}

		protected Command getCreateCommand(CreateRequest request) {
			Object childClass = request.getNewObjectType();
			if (childClass instanceof AbstractConnectableService) {
				return new ComponentCreateCommand((AbstractComponent) request
						.getNewObject(), (DeploymentDiagram) getHost()
						.getModel(), (Rectangle) getConstraintFor(request));
			}
			return null;
		}

		protected Command getDeleteDependantCommand(Request request) {
			// not used
			return null;
		}

	}

	private IProject project;

	public DeploymentDiagramEditPart(IProject owningProject) {
		this.project = owningProject;
	}

	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new RootComponentEditPolicy());
		installEditPolicy(EditPolicy.LAYOUT_ROLE,
				new ShapesXYLayoutEditPolicy());
	}

	protected IFigure createFigure() {
		Figure f = new FreeformLayer();
		f.setBorder(new MarginBorder(3));
		f.setLayoutManager(new FreeformLayout());

		ConnectionLayer connLayer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
		connLayer.setConnectionRouter(new ShortestPathConnectionRouter(f));

		return f;
	}

	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	private DeploymentDiagram getCastedModel() {
		return (DeploymentDiagram) getModel();
	}

	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	protected List getModelChildren() {
		return getCastedModel().getChildren();
	}

	public IProject getProject() {
		return project;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();
		descriptors
				.add(new TextPropertyDescriptor(INSTALL_PATH, "Install path"));
		descriptors.add(new TextPropertyDescriptor(DEPLOY_PATH, "Deploy path"));
		return getArray(descriptors);
	}

	public Object getPropertyValue(Object arg0) {
		if (DEPLOY_PATH.equals(arg0))
			return ((DeploymentDiagram) getModel()).getDeployPath();
		else if (INSTALL_PATH.equals(arg0))
			return ((DeploymentDiagram) getModel()).getInstallPath();
		return null;
	}

	public boolean isPropertySet(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		if (DeploymentDiagram.CHILD_ADDED_PROP.equals(prop)
				|| DeploymentDiagram.CHILD_REMOVED_PROP.equals(prop)) {
			refreshChildren();
		}
	}

	public void resetPropertyValue(Object arg0) {
		// TODO Auto-generated method stub

	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public void setPropertyValue(Object arg0, Object arg1) {
		if (DEPLOY_PATH.equals(arg0))
			((DeploymentDiagram) getModel()).setDeployPath((String) arg1);
		else if (INSTALL_PATH.equals(arg0))
			((DeploymentDiagram) getModel()).setInstallPath((String) arg1);
	}

}