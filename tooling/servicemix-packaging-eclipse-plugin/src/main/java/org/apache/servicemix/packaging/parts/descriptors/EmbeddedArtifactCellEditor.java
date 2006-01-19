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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;

/**
 * The cell editor for an embedded artifact
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class EmbeddedArtifactCellEditor extends DialogCellEditor {

	private DeploymentDiagramEditPart model;

	private String extension;

	public EmbeddedArtifactCellEditor(DeploymentDiagramEditPart model,
			String extension) {
		super();
		this.model = model;
		this.extension = extension;
	}

	@Override
	protected Object openDialogBox(Control arg0) {
		EmbeddedArtifactSelectionDialog editDialog = new EmbeddedArtifactSelectionDialog(
				arg0.getShell(), getValue(), model, extension);
		if (editDialog.open() == IDialogConstants.OK_ID) {
			return editDialog.getResource();
		}
		return null;
	}	
}
