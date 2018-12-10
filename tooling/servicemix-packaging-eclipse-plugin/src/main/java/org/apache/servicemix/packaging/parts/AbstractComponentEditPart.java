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

import javax.xml.namespace.QName;

import org.apache.servicemix.descriptors.packaging.assets.Artifact;
import org.apache.servicemix.descriptors.packaging.assets.Assets;
import org.apache.servicemix.descriptors.packaging.assets.Connection;
import org.apache.servicemix.descriptors.packaging.assets.Parameter;
import org.apache.servicemix.packaging.ComponentArtifact;
import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.AbstractConnectableService;
import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.model.ComponentConnection;
import org.apache.servicemix.packaging.model.Connectable;
import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

/**
 * The abstract implementation for a JBI component edit part (GEF)
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public abstract class AbstractComponentEditPart extends
		AbstractGraphicalEditPart2 implements PropertyChangeListener,
		NodeEditPart {

	protected ConnectionAnchor anchor;

	public void activate() {
		if (!isActive()) {
			super.activate();
			((ModelElement) getModel()).addPropertyChangeListener(this);
		}
	}

	protected void setPropertyFromAssets(Object arg0, Object arg1, Assets assets) {

		if (arg0 instanceof Parameter) {
			Parameter parameter = (Parameter) arg0;
			String value = (String) arg1;

			for (Parameter parameterValue : assets.getParameter()) {
				if (parameter.getName().equals(parameterValue.getName())) {
					parameterValue.setValue(value);
					return;
				}
			}

			Parameter newParameterValue = new Parameter();
			newParameterValue.setName(parameter.getName());
			newParameterValue.setValue(value);
			assets.getParameter().add(newParameterValue);
		} else if (arg0 instanceof Connection) {
			Connection connection = (Connection) arg0;
			QName value = (QName) arg1;

			for (Connection resourceReference : assets.getConnection()) {
				if (connection.getName().equals(resourceReference.getName())) {
					resourceReference.setQname(value);
					return;
				}
			}

			Connection newReference = new Connection();
			newReference.setName(connection.getName());
			newReference.setQname(value);
			newReference.setType(connection.getType());
			assets.getConnection().add(newReference);
		} else if (arg0 instanceof Artifact) {
			Artifact artifact = (Artifact) arg0;
			IResource resource = (IResource) arg1;
			for (Artifact artifactReference : assets.getArtifact()) {
				if (artifact.getName().equals(artifactReference.getName())) {
					artifactReference.setPath(resource.getProjectRelativePath()
							.toOSString());
					return;
				}
			}
			Artifact newArtifactReference = new Artifact();
			newArtifactReference.setName(artifact.getName());
			newArtifactReference.setPath(resource.getProjectRelativePath()
					.toOSString());
			assets.getArtifact().add(newArtifactReference);
		}
	}

	protected Object getPropertyFromAssets(Object arg0, Assets assets) {
		Object result = null;
		if (arg0 instanceof Parameter) {
			Parameter parameter = (Parameter) arg0;
			for (Parameter parameterValue : assets.getParameter()) {
				if (parameter.getName().equals(parameterValue.getName())) {
					result = parameterValue.getValue();
				}
			}
			result = parameter.getValue();
		} else if (arg0 instanceof Connection) {
			Connection connection = (Connection) arg0;
			for (Connection resourceReference : assets.getConnection()) {
				if (connection.getName().equals(resourceReference.getName())) {
					result = resourceReference.getQname();
				}
			}
		} else if (arg0 instanceof Artifact) {
			Artifact artifact = (Artifact) arg0;
			for (Artifact artifactReference : assets.getArtifact()) {
				if (artifact.getName().equals(artifactReference.getName())) {
					return artifactReference.getPath();
				}
			}
		}
		return result;
	}

	protected DeploymentDiagramEditPart getDeploymentDiagram() {
		return (DeploymentDiagramEditPart) getRoot().getChildren().get(0);
	}

	protected void createEditPolicies() {
		// allow removal of the associated model element
		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new AbstractComponentEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
	}

	protected IFigure createFigure() {
		IFigure f = createFigureForModel();
		return f;
	}

	protected abstract IFigure createFigureForModel();

	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((ModelElement) getModel()).removePropertyChangeListener(this);
		}
	}

	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) {
			anchor = new ChopboxAnchor(getFigure());
		}
		return anchor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
	 */
	protected List getModelSourceConnections() {
		if (getModel() instanceof Connectable)
			return ((Connectable) getModel()).getSourceConnections();
		else
			return new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
	 */
	protected List getModelTargetConnections() {
		if (getModel() instanceof Connectable)
			return ((Connectable) getModel()).getTargetConnections();
		else
			return new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return getConnectionAnchor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String prop = evt.getPropertyName();
		try {
			if (AbstractConnectableService.LOCATION_PROP.equals(prop)
					|| AbstractConnectableService.SERVICENAME_PROP.equals(prop)) {
				refreshVisuals();
			} else if (AbstractConnectableService.SOURCE_CONNECTIONS_PROP
					.equals(prop)) {
				refreshSourceConnections();
			} else if (AbstractConnectableService.TARGET_CONNECTIONS_PROP
					.equals(prop)) {
				refreshTargetConnections();
			}
		} catch (Throwable t) {
			System.out
					.println("Exception while trying to handle property change : "
							+ t.getLocalizedMessage());
			t.printStackTrace();
		}
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		Rectangle bounds = new Rectangle(((AbstractComponent) getModel())
				.getLocation(), getFigure().getPreferredSize());
		((GraphicalEditPart) getParent()).setLayoutConstraint(this,
				getFigure(), bounds);
		refreshSourceConnections();
	}

	public ComponentArtifact getComponentArtifact() {
		if (getModel() instanceof ComponentBased) {
			return ((ComponentBased) getModel()).getComponentArtifact();
		} else
			return null;
	}
}
