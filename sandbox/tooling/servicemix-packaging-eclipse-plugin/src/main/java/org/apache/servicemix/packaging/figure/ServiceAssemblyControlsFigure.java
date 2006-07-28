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

import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.internal.ui.palette.editparts.ColumnsLayout;

/**
 * The add/remove controls for service units in a service assembly
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceAssemblyControlsFigure extends Figure {

	private Button addButton;

	private ButtonModel buttonModel;

	private ServiceAssembly serviceAssembly;

	public ServiceAssemblyControlsFigure(ServiceAssembly serviceAssembly,
			ActionListener actionListener) {
		this.serviceAssembly = serviceAssembly;
		ColumnsLayout columnsLayout = new ColumnsLayout();
		columnsLayout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		setLayoutManager(columnsLayout);
		addButton = new Button("+");
		addButton.setToolTip(new Label("Create a new service unit"));
		add(addButton);
		buttonModel = new ButtonModel();
		buttonModel.addActionListener(actionListener);
		addButton.setModel(buttonModel);
	}

	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		dim.width = addButton.getPreferredSize().width;
		dim.width += getInsets().getWidth();
		dim.height = addButton.getPreferredSize().height;
		dim.height += getInsets().getHeight();
		return dim;
	}
}
