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
package org.apache.servicemix.packaging.parts;

import java.beans.PropertyChangeEvent;
import java.util.LinkedList;
import java.util.List;

import org.apache.servicemix.descriptors.packaging.assets.Components.Component;
import org.apache.servicemix.packaging.figure.ServiceAssemblyFigure;
import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.parts.descriptors.ServiceNameHelper;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * The GEF edit part of a service
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceAssemblyEditPart extends AbstractComponentEditPart
		implements IPropertySource, ActionListener {

	private LinkedList<IPropertyDescriptor> descriptors;

	@Override
	protected IFigure createFigureForModel() {
		ServiceAssemblyFigure newFigure = new ServiceAssemblyFigure(
				(ServiceAssembly) getModel(), this);
		return newFigure;
	}

	@Override
	public IFigure getContentPane() {
		return ((ServiceAssemblyFigure) getFigure()).getContentPaneFigure();
	}

	@Override
	protected List getModelChildren() {
		return ((ServiceAssembly) getModel()).getServiceUnit();
	}

	@Override
	public void refresh() {
		super.refresh();
		refreshVisuals();
		refreshChildren();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(ServiceAssembly.NAME_PROP)) {
			refreshVisuals();
		} else if (evt.getPropertyName().equals(ServiceAssembly.ADDCHILD_PROP)) {
			this.refresh();
		}
	}

	@Override
	protected void refreshVisuals() {
		super.refreshVisuals();
		((ServiceAssemblyFigure) getFigure()).refresh();
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
					"Service assembly name"));
			/*
			 * Ignore assets for assembly if
			 * (getComponentDescriptor().getAssets() != null) {
			 * descriptors.addAll(AssetDescriptorFactory.getDescriptors(
			 * getComponentDescriptor().getAssets(), getDeploymentDiagram())); }
			 */
		}

		return getArray(descriptors);
	}

	public Object getPropertyValue(Object arg0) {
		if (arg0 instanceof ServiceAssembly) {
			return ((ServiceAssembly) arg0).getName();
		} else
			return getPropertyFromAssets(arg0, ((ServiceAssembly) getModel())
					.getStoredAssets());
	}

	public boolean isPropertySet(Object arg0) {
		return true;
	}

	public void resetPropertyValue(Object arg0) {

	}

	public void setPropertyValue(Object arg0, Object arg1) {
		if (arg0 instanceof ServiceAssembly) {
			((ServiceAssembly) arg0).setName((String) arg1);
		} else
			setPropertyFromAssets(arg0, arg1, ((ServiceAssembly) getModel())
					.getStoredAssets());
		((AbstractComponent) getModel()).updated();
	}

	public void actionPerformed(ActionEvent arg0) {
		((ServiceAssembly) getModel())
				.createServiceUnit(ServiceNameHelper
						.getUniqueServiceName((DeploymentDiagram) getDeploymentDiagram()
								.getModel()));
	}
}