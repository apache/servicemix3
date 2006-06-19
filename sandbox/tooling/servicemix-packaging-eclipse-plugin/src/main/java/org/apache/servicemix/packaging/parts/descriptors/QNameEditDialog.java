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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog for editting an XML Qualified Name (QName)
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class QNameEditDialog extends Dialog {

	private QName qname = new QName("http://www.openuri.org","newService");

	private Text localPart;

	private Text namespaceUri;

	private TableViewer viewer;

	public QNameEditDialog(Shell parentShell, Object object) {
		super(parentShell);
		if (object instanceof QName)
			this.qname = (QName) object;
	}

	@Override
	protected void okPressed() {
		qname = new QName(namespaceUri.getText(), localPart.getText());
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

		Label localPartLabel = new Label(parent, SWT.WRAP);
		localPartLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		localPartLabel.setText("Local part");

		localPart = new Text(parent, SWT.BORDER);
		localPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label namespaceUriLabel = new Label(parent, SWT.WRAP);
		namespaceUriLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		namespaceUriLabel.setText("Namespace URI");
		namespaceUri = new Text(parent, SWT.BORDER);
		namespaceUri.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// initialize values
		localPart.setText(qname.getLocalPart());
		namespaceUri.setText(qname.getNamespaceURI());

		return parent;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 220);
	}

	public Object getQName() {
		return qname;
	}
}
