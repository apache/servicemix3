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

import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * The GEF figure for a Service Assembly Name
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceAssemblyNameFigure extends Figure {

	private Label name;

	private ServiceAssembly serviceAssembly;

	private Label componentName;

	public ServiceAssemblyNameFigure(ServiceAssembly service) {
		this.serviceAssembly = service;
		ToolbarLayout toolbarLayout = new ToolbarLayout();
		toolbarLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
		setLayoutManager(toolbarLayout);

		name = new Label();
		name.setLabelAlignment(PositionConstants.LEFT);
		name.setFont(new Font(null, "Arial", 12, SWT.NORMAL));
		name.setForegroundColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_BLACK));

		componentName = new Label();
		componentName.setLabelAlignment(PositionConstants.LEFT);
		componentName.setForegroundColor(Display.getCurrent().getSystemColor(
				SWT.COLOR_GRAY));

		add(name);
		add(componentName);
	}

	public void refresh() {
		name.setText(serviceAssembly.getName());
		StringBuffer description = new StringBuffer();
		if (serviceAssembly instanceof ComponentBased) {
			ComponentBased componentBase = (ComponentBased) serviceAssembly;
			description.append(componentBase.getComponentArtifact()
					.getComponentDefinitionByName(
							componentBase.getComponentName()).getDescription());
			description.append(" (");
			description.append(componentBase.getComponentArtifact()
					.getComponentDefinitionByName(
							componentBase.getComponentName()).getType());
			description.append(")");

			componentName.setText(description.toString());
		}
	}

	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		dim.width = name.getPreferredSize().width;
		dim.width = dim.width > componentName.getPreferredSize().width ? dim.width
				: componentName.getPreferredSize().width;
		dim.width += getInsets().getWidth();
		dim.height = name.getPreferredSize().height
				+ componentName.getPreferredSize().height;
		return dim;
	}
}
