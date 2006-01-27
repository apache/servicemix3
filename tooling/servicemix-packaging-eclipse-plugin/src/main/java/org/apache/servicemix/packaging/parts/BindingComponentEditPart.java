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
import java.util.List;

import org.apache.servicemix.packaging.figure.BindingComponentFigure;
import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.parts.descriptors.AssetDescriptorFactory;
import org.apache.servicemix.packaging.parts.descriptors.QNamePropertyDescriptor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;

import org.apache.servicemix.packaging.descriptor.Component;

/**
 * The GEF Edit Part for a Binding Component
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class BindingComponentEditPart extends AbstractComponentEditPart
		implements IPropertySource {

	private LinkedList<IPropertyDescriptor> descriptors;

	@Override
	protected IFigure createFigureForModel() {
		BindingComponentFigure newFigure = new BindingComponentFigure(
				(BindingComponent) getModel());
		return newFigure;
	}	

	private Component getComponentDescriptor() {
		Component component = null;
		if (getModel() instanceof ComponentBased) {
			for (Component componentIter : ((ComponentBased) getModel())
					.getComponentArtifact().getComponents().getComponent()) {
				if (componentIter.getComponentUuid().equals(
						((ComponentBased) getModel()).getComponentUuid())) {
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
			descriptors.add(new QNamePropertyDescriptor(
					((BindingComponent) getModel()).getServiceName(),
					"Service name", null));
			if (getComponentDescriptor().getAssets() != null) {
				descriptors.addAll(AssetDescriptorFactory.getDescriptors(
						getComponentDescriptor().getAssets(),
						getDeploymentDiagram()));
			}
		}
		return getArray(descriptors);
	}

	public Object getPropertyValue(Object arg0) {
		return getPropertyFromAssets(arg0, ((BindingComponent) getModel())
				.getStoredAssets());
	}

	public boolean isPropertySet(Object arg0) {
		return true;
	}

	@Override
	protected void refreshVisuals() {
		((BindingComponentFigure) getFigure()).refresh();
		super.refreshVisuals();
	}

	public void resetPropertyValue(Object arg0) {

	}

	public void setPropertyValue(Object arg0, Object arg1) {		
		setPropertyFromAssets(arg0, arg1, ((BindingComponent) getModel())
				.getStoredAssets());
		((AbstractComponent) getModel()).updated();
	}

}