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

import java.util.LinkedList;

import org.apache.servicemix.descriptors.deployment.assets.Components.Component;
import org.apache.servicemix.packaging.figure.ServiceUnitFigure;
import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.apache.servicemix.packaging.parts.descriptors.AssetDescriptorFactory;
import org.apache.servicemix.packaging.parts.descriptors.QNamePropertyDescriptor;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The GEF edit part for a service unit
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceUnitEditPart extends AbstractComponentEditPart implements
		IPropertySource {

	private LinkedList<IPropertyDescriptor> descriptors;

	@Override
	protected void createEditPolicies() {
		NonResizableEditPolicy selectionPolicy = new NonResizableEditPolicy();
		selectionPolicy.setDragAllowed(false);
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, selectionPolicy);

		installEditPolicy(EditPolicy.COMPONENT_ROLE,
				new ServiceUnitEditPolicy());
	}

	@Override
	protected IFigure createFigureForModel() {
		ServiceUnitFigure newFigure = new ServiceUnitFigure(
				(ServiceUnit) getModel());
		return newFigure;
	}

	@Override
	protected void refreshVisuals() {
		((ServiceUnitFigure) getFigure()).refresh();
		super.refreshVisuals();
	}

	private Component getComponentDescriptor() {
		Component component = null;
		if (getModel() instanceof ComponentBased) {
			for (Component componentIter : ((ComponentBased) getModel())
					.getComponentArtifact().getComponents().getComponent()) {
				if (componentIter.getName().equals(
						((ComponentBased) getModel()).getComponentName())) {
					component = componentIter;
				}
			}
		}
		return component;

	}

	public Object getEditableValue() {
		return this;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (descriptors == null) {
			descriptors = new LinkedList<IPropertyDescriptor>();
			descriptors.add(new TextPropertyDescriptor(getModel(),
					"Service unit name"));
			descriptors.add(new QNamePropertyDescriptor(
					((ServiceUnit) getModel()).getServiceName(),
					"Service name", null));
			if (getComponentDescriptor().getAssets() != null)
				descriptors.addAll(AssetDescriptorFactory.getDescriptors(
						getComponentDescriptor().getAssets(),
						getDeploymentDiagram()));

		}
		return getArray(descriptors);
	}

	public Object getPropertyValue(Object arg0) {
		if (arg0 instanceof ServiceUnit) {
			return ((ServiceUnit) arg0).getServiceUnitName();
		} else
			return getPropertyFromAssets(arg0, ((ServiceUnit) getModel())
					.getStoredAssets());
	}

	public boolean isPropertySet(Object arg0) {
		return true;
	}

	public void resetPropertyValue(Object arg0) {

	}

	@Override
	protected ConnectionAnchor getConnectionAnchor() {
		if (anchor == null) {
			anchor = new ChopboxAnchor(((ServiceUnitFigure) getFigure())
					.getComponentImage());
		}
		return anchor;
	}

	public void setPropertyValue(Object arg0, Object arg1) {
		if (arg0 instanceof ServiceUnit) {
			((ServiceUnit) arg0).setServiceUnitName((String) arg1);
		} else
			setPropertyFromAssets(arg0, arg1, ((ServiceUnit) getModel())
					.getStoredAssets());
		((AbstractComponent) getModel()).updated();
	}
}