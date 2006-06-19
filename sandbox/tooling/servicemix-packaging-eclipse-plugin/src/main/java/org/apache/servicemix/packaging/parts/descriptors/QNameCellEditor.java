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

import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.widgets.Control;

/**
 * The property descriptor cell editor for an XML Qualified Name (QName)
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class QNameCellEditor extends DialogCellEditor {

	private DeploymentDiagram model;

	public QNameCellEditor(DeploymentDiagram model) {
		super();
		this.model = model;
	}

	@Override
	protected Object openDialogBox(Control arg0) {
		if (model == null) {
			QNameEditDialog editDialog = new QNameEditDialog(arg0.getShell(),
					getValue());
			if (editDialog.open() == IDialogConstants.OK_ID) {
				return editDialog.getQName();
			} else
				return null;
		} else {
			QNameFromDeploymentEditDialog editDialog = new QNameFromDeploymentEditDialog(
					arg0.getShell(), getValue(), model);
			if (editDialog.open() == IDialogConstants.OK_ID) {
				return editDialog.getQName();
			} else
				return null;
		}

	}
}
