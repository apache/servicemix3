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
package org.apache.servicemix.packaging.parts.descriptors;

import org.apache.servicemix.packaging.parts.DeploymentDiagramEditPart;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * The property descriptor for an embedded artifact
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class EmbeddedArtifactDescriptor extends PropertyDescriptor {

	private DeploymentDiagramEditPart model;

	private String extension;

	public EmbeddedArtifactDescriptor(Object property, String propertyName,
			DeploymentDiagramEditPart diagramEditPart, String extension) {
		super(property, propertyName);
		if (diagramEditPart != null) {
			this.model = diagramEditPart;
		}
		this.extension = extension;
	}

	@Override
	public CellEditor createPropertyEditor(Composite arg0) {
		EmbeddedArtifactCellEditor editor = new EmbeddedArtifactCellEditor(
				model, extension);
		editor.create(arg0);
		return editor;
	}

	@Override
	public ILabelProvider getLabelProvider() {
		return new IResourceLabelProvider();
	}

}
