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
package org.apache.servicemix.packaging.parts.descriptors;

import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.parts.DeploymentDiagramEditPart;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * The GEF property descriptor for an XML Qualified Name (QName)
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class QNamePropertyDescriptor extends PropertyDescriptor {

	private DeploymentDiagram model;

	public QNamePropertyDescriptor(Object property, String propertyName,
			DeploymentDiagramEditPart diagramEditPart) {
		super(property, propertyName);
		if (diagramEditPart != null) {
			this.model = (DeploymentDiagram) diagramEditPart.getModel();
		}
	}

	@Override
	public CellEditor createPropertyEditor(Composite arg0) {
		QNameCellEditor editor = new QNameCellEditor(model);
		editor.create(arg0);
		return editor;
	}

}
