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
package org.apache.servicemix.packaging.figure;

import org.apache.servicemix.descriptors.packaging.assets.Assets;
import org.apache.servicemix.descriptors.packaging.assets.Connection;
import org.apache.servicemix.packaging.model.AbstractConnectableService;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * The GEF figure for a Service Name
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceNameFigure extends Figure {

	private Label localName;

	private Label namespace;

	private AbstractConnectableService service;

	private Label componentName;

	public ServiceNameFigure(AbstractConnectableService service) {
		this.service = service;

		ToolbarLayout toolbarLayout = new ToolbarLayout();
		toolbarLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		setLayoutManager(toolbarLayout);

		localName = new Label();
		localName.setLabelAlignment(PositionConstants.LEFT);
		localName.setFont(new Font(null, "Arial", 12, SWT.NORMAL));
		localName.setForegroundColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_BLACK));

		namespace = new Label();
		namespace.setLabelAlignment(PositionConstants.LEFT);
		namespace.setForegroundColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));

		componentName = new Label();
		componentName.setLabelAlignment(PositionConstants.LEFT);
		componentName.setForegroundColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_GRAY));

		add(localName);
		add(namespace);
		add(componentName);
	}

	public void refresh() {
		StringBuffer description = new StringBuffer();
		if (service instanceof BindingComponent) {
			ComponentBased componentBase = (ComponentBased) service;
			description.append(componentBase.getComponentArtifact()
					.getComponentDefinitionByName(
							componentBase.getComponentName()).getName());
			description.append(" (");
			description.append(componentBase.getComponentArtifact()
					.getComponentDefinitionByName(
							componentBase.getComponentName()).getType());
			description.append(")");

			componentName.setText(description.toString());
		} else if (service instanceof ServiceUnit) {
			description.append(((ServiceUnit) service).getServiceUnitName());
			componentName.setText(((ServiceUnit) service).getServiceUnitName());
		}

		if (canHaveServiceName()) {
			if (service.getServiceName() != null) {
				localName.setText(service.getServiceName().getLocalPart());
				namespace.setText(service.getServiceName().getNamespaceURI());
				componentName.setText(description.toString());
			} else {
				localName.setText(description.toString());
				namespace.setText("Missing '"+getMissingConnectionName()+"' connection");
				componentName
						.setText("Check your properties and set up your connections");
			}
		} else {
			localName.setText(description.toString());
			namespace.setText("No connections available");
			componentName.setText("");
		}

	}

	private boolean canHaveServiceName() {
		if (service instanceof ServiceUnit) {
			ServiceUnit serviceUnit = (ServiceUnit) service;
			Assets assets = serviceUnit.getComponentArtifact()
					.getComponentDefinitionByName(
							serviceUnit.getComponentName()).getAssets();
			if ((assets != null) && (assets.getConnection() != null))
				for (Connection connection : assets.getConnection()) {
					if ("provides".equals(connection.getType())) {
						return true;
					}
				}
		} else if (service instanceof BindingComponent) {
			BindingComponent bindingComponent = (BindingComponent) service;
			Assets assets = bindingComponent.getComponentArtifact()
					.getComponentDefinitionByName(
							bindingComponent.getComponentName()).getAssets();
			if ((assets != null) && (assets.getConnection() != null))
				for (Connection connection : assets.getConnection()) {
					if ("provides".equals(connection.getType())) {
						return true;
					}
				}
		}

		return false;
	}

	private String getMissingConnectionName() {
		if (service instanceof ServiceUnit) {
			ServiceUnit serviceUnit = (ServiceUnit) service;
			Assets assets = serviceUnit.getComponentArtifact()
					.getComponentDefinitionByName(
							serviceUnit.getComponentName()).getAssets();
			if ((assets != null) && (assets.getConnection() != null))
				for (Connection connection : assets.getConnection()) {
					if ("provides".equals(connection.getType())) {
						return connection.getDescription();
					}
				}
		} else if (service instanceof BindingComponent) {
			BindingComponent bindingComponent = (BindingComponent) service;
			Assets assets = bindingComponent.getComponentArtifact()
					.getComponentDefinitionByName(
							bindingComponent.getComponentName()).getAssets();
			if ((assets != null) && (assets.getConnection() != null))
				for (Connection connection : assets.getConnection()) {
					if ("provides".equals(connection.getType())) {
						return connection.getDescription();
					}
				}
		}

		return null;
	}

	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		dim.width = localName.getPreferredSize().width > namespace
				.getPreferredSize().width ? localName.getPreferredSize().width
				: namespace.getPreferredSize().width;
		dim.width = dim.width > componentName.getPreferredSize().width ? dim.width
				: componentName.getPreferredSize().width;
		dim.width += getInsets().getWidth();
		dim.height = localName.getPreferredSize().height
				+ namespace.getPreferredSize().height
				+ componentName.getPreferredSize().height;
		return dim;
	}
}
