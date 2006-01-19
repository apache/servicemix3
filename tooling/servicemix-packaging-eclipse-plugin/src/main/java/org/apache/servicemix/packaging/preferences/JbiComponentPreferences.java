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
package org.apache.servicemix.packaging.preferences;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicemix.packaging.ComponentArtifact;
import org.apache.servicemix.packaging.ComponentArtifactFactory;
import org.apache.servicemix.packaging.DeployerPlugin;
import org.apache.servicemix.packaging.InvalidArchiveException;
import org.apache.servicemix.packaging.descriptor.Component;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Simple Eclipse preference page that can be used to register the JBI
 * components, the preferences should be one of the sources of components
 * for the ComponentArtifactFactory
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class JbiComponentPreferences extends PreferencePage implements
		IWorkbenchPreferencePage {

	private TableViewer componentBundlesViewer;

	@Override
	protected Control createContents(Composite arg0) {
		createTableViewer(arg0);
		componentBundlesViewer.setInput(gatherComponents());
		return null;
	}

	private Object gatherComponents() {
		List<Component> allComponents = new ArrayList<Component>();
		for (ComponentArtifact artifact : ComponentArtifactFactory
				.getComponentArtifacts()) {
			allComponents.addAll(artifact.getComponents().getComponent());
		}
		return allComponents;
	}

	private void createTableViewer(Composite owner) {
		Table propertiesTable = new Table(owner, SWT.NONE);
		propertiesTable.setLinesVisible(true);
		propertiesTable.setHeaderVisible(true);
		componentBundlesViewer = new TableViewer(propertiesTable);
		GridData gd = new GridData(GridData.FILL_BOTH);

		gd.heightHint = 20;
		gd.widthHint = 100;
		gd.verticalSpan = 3;
		propertiesTable.setLayoutData(gd);

		componentBundlesViewer
				.setContentProvider(new IStructuredContentProvider() {

					public void inputChanged(Viewer arg0, Object arg1,
							Object arg2) {
						// Nothing to do
					}

					public void dispose() {
						// Nothing to do
					}

					public Object[] getElements(Object arg0) {
						if (arg0 instanceof List) {
							return ((List) arg0).toArray();
						} else
							return new ComponentArtifact[] {};
					}

				});

		componentBundlesViewer.setLabelProvider(new ArtifactLabelProvider());

		Button addSubscription = new Button(owner, SWT.PUSH);
		addSubscription.setText("Add component");
		GridData addButtonGrid = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		addButtonGrid.widthHint = 160;
		addSubscription.setLayoutData(addButtonGrid);
		addSubscription.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				addComponentBundle();
			}
		});

		Button removeSubscription = new Button(owner, SWT.PUSH);
		removeSubscription.setText("Remove component");
		GridData removeButtonGrid = new GridData(
				GridData.VERTICAL_ALIGN_BEGINNING);
		removeButtonGrid.widthHint = 160;
		removeSubscription.setLayoutData(removeButtonGrid);
		removeSubscription.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event arg0) {
				removeComponentBundle();
			}
		});

		TableColumn column = new TableColumn(propertiesTable, SWT.LEFT, 0);
		column.setText("Name");
		column.setResizable(true);
		column.setWidth(100);
		column = new TableColumn(propertiesTable, SWT.LEFT, 1);
		column.setText("Location");
		column.setResizable(true);
		column.setWidth(200);

		CellEditor[] editors = new CellEditor[2];

		TextCellEditor textEditor2 = new TextCellEditor(propertiesTable);
		((Text) textEditor2.getControl()).setTextLimit(60);
		editors[0] = textEditor2;

		TextCellEditor textEditor = new TextCellEditor(propertiesTable);
		((Text) textEditor.getControl()).setTextLimit(60);
		editors[1] = textEditor;

		componentBundlesViewer.setCellEditors(editors);

		componentBundlesViewer.setColumnProperties(new String[] { "name",
				"value" });

	}

	protected void removeComponentBundle() {
		ISelection selection = componentBundlesViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection)
					.getFirstElement();
			List<String> services = (List<String>) componentBundlesViewer
					.getInput();
			services.remove(element);
			componentBundlesViewer.refresh();
		}
	}

	protected void addComponentBundle() {
		FileDialog fileDialog = new FileDialog(getShell());
		fileDialog.setFilterExtensions(new String[] { "*.zip", "*.jar" });
		fileDialog.setFilterNames(new String[] { "Zip Archives (*.zip)",
				"Java Archives (*.jar)" });
		try {
			if (fileDialog.open() != null)
				((List<ComponentArtifact>) componentBundlesViewer.getInput())
						.add(new ComponentArtifact(fileDialog.getFilterPath()
								+ "/" + fileDialog.getFileName()));
		} catch (InvalidArchiveException e) {
			Status s = new Status(Status.ERROR, "not_used", 0, e.getMessage(),
					e);
			ErrorDialog.openError(getShell(), "Invalid Component", null, s);
		}
		componentBundlesViewer.refresh();
	}

	public void init(IWorkbench arg0) {
		setPreferenceStore(DeployerPlugin.getDefault().getPreferenceStore());
	}

	@Override
	protected void performApply() {
		ComponentArtifactFactory
				.setComponentArtifacts((List<ComponentArtifact>) componentBundlesViewer
						.getInput());
		super.performApply();
	}

	@Override
	public boolean performOk() {
		ComponentArtifactFactory
				.setComponentArtifacts((List<ComponentArtifact>) componentBundlesViewer
						.getInput());
		return super.performOk();
	}

	public class ArtifactLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof Component) {
				Component service = (Component) obj;
				switch (index) {
				case 0:
					return service.getName();
				case 1:
					return getArtifactForService(service).getArchivePath();
				}
			}
			return obj.toString();
		}

		private ComponentArtifact getArtifactForService(
				Component serviceToLookup) {
			for (ComponentArtifact artifact : ComponentArtifactFactory
					.getComponentArtifacts()) {
				for (Component component : artifact.getComponents()
						.getComponent()) {
					if (component.getComponentUuid().equals(
							serviceToLookup.getComponentUuid())) {
						return artifact;
					}
				}
			}
			return null;
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
		}
	}

}
