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

import javax.xml.namespace.QName;

import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The Dialog for selecting a QName from a deployment diagram
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class QNameFromDeploymentEditDialog extends Dialog {

	private DeploymentDiagram deploymentDiagram;

	private TableViewer viewer;

	private QName qname;

	public QNameFromDeploymentEditDialog(Shell parentShell, Object object,
			DeploymentDiagram model) {
		super(parentShell);
		this.deploymentDiagram = model;
	}

	@Override
	protected void okPressed() {
		if (viewer.getSelection() instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) viewer.getSelection())
					.getFirstElement();
			if (firstElement instanceof QName) {
				QName newQname = (QName) firstElement;
				qname = newQname;
			}
		}
		super.okPressed();
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	protected Control createDialogArea(Composite parentComposite) {
		Composite parent = (Composite) super.createDialogArea(parentComposite);
		parent.setLayout(new GridLayout());
		getShell().setText("Service Name");
		Label descriptionLabel = new Label(parent, SWT.WRAP);
		descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionLabel
				.setText("A service name is an XML qualified name and requires that both the local name and namespace are defined.");

		createServiceNameTable(parent);

		return parent;
	}

	private void createServiceNameTable(Composite parent) {
		Table table = new Table(parent, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Property Name");
		column.setWidth(600);

		viewer = new TableViewer(table);
		viewer.setContentProvider(new ServiceNameContentProvider());
		viewer.setLabelProvider(new ServiceNameLabelProvider());
		viewer.setInput(deploymentDiagram);
		viewer.refresh();
	}

	@Override
	protected Point getInitialSize() {
		if (deploymentDiagram == null)
			return new Point(400, 220);
		else
			return new Point(400, 500);
	}

	public Object getQName() {
		return qname;
	}
}
