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

import javax.xml.namespace.QName;

import org.apache.servicemix.packaging.parts.DeploymentDiagramEditPart;
import org.eclipse.core.resources.IResource;
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
 * The selection dialog for an embedded artifact
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class EmbeddedArtifactSelectionDialog extends Dialog {

	private DeploymentDiagramEditPart deploymentDiagram;

	private String extension;

	private QName qname;

	private IResource resource;

	private TableViewer viewer;

	public EmbeddedArtifactSelectionDialog(Shell parentShell, Object object,
			DeploymentDiagramEditPart model, String extension) {
		super(parentShell);
		this.deploymentDiagram = model;
		this.extension = extension;
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
		getShell().setText("Embedded Artifact");
		Label descriptionLabel = new Label(parent, SWT.WRAP);
		descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		descriptionLabel
				.setText("An embedded artifact is a file resource you have within your project that you wish to make available to the JBI component or service unit.");

		createEmbeddedArtifactTable(parent);

		return parent;
	}

	private void createEmbeddedArtifactTable(Composite parent) {
		Table table = new Table(parent, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION);
		table.setLinesVisible(false);
		table.setHeaderVisible(false);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setText("Property Name");
		column.setWidth(600);

		viewer = new TableViewer(table);
		viewer
				.setContentProvider(new EmbeddedArtifactContentProvider(
						extension));
		viewer.setLabelProvider(new IResourceLabelProvider());
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

	@Override
	protected void okPressed() {
		if (viewer.getSelection() instanceof IStructuredSelection) {			
			Object firstElement = ((IStructuredSelection) viewer.getSelection())
					.getFirstElement();			
			if (firstElement instanceof IResource) {
				resource = (IResource) firstElement;
			}
		}
		super.okPressed();
	}

	public IResource getResource() {
		return resource;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}
}
