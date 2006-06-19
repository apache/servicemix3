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
package org.apache.servicemix.packaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.apache.servicemix.packaging.parts.DeploymentEditPartFactory;
import org.apache.servicemix.packaging.parts.DeploymentTreeEditPartFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.TransferDropTargetListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * The DeployerEditor is the Eclipse Editor that can be used to layout
 * registered components and configure them before deployment.
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeployerEditor extends GraphicalEditorWithFlyoutPalette {

	public class DeployerOutlinePage extends ContentOutlinePage {

		public DeployerOutlinePage(EditPartViewer viewer) {
			super(viewer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
		 */
		public void createControl(Composite parent) {
			getViewer().createControl(parent);
			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new DeploymentTreeEditPartFactory());
			ContextMenuProvider cmProvider = new DeployerEditorContextMenuProvider(
					getViewer(), null, getActionRegistry());
			getViewer().setContextMenu(cmProvider);
			getSite().registerContextMenu(
					"com.unity.jbi.deployer.outline.contextmenu", cmProvider,
					getSite().getSelectionProvider());
			getSelectionSynchronizer().addViewer(getViewer());
			getViewer().setContents(getModel());
		}

		public void dispose() {
			// unhook outline viewer
			getSelectionSynchronizer().removeViewer(getViewer());
			// dispose
			super.dispose();
		}

		public Control getControl() {
			return getViewer().getControl();
		}

		public void init(IPageSite pageSite) {
			super.init(pageSite);
			ActionRegistry registry = getActionRegistry();
			IActionBars bars = pageSite.getActionBars();
			String id = ActionFactory.UNDO.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = ActionFactory.REDO.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = ActionFactory.DELETE.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
		}
	}

	private static PaletteRoot PALETTE_MODEL;

	private DeploymentEditPartFactory deploymentEditPartFactory;

	private DeploymentDiagram diagram;

	private IProject project;

	public DeployerEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();

		GraphicalViewer viewer = getGraphicalViewer();
		deploymentEditPartFactory = new DeploymentEditPartFactory();
		deploymentEditPartFactory.setProject(project);
		viewer.setEditPartFactory(deploymentEditPartFactory);
		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));

		// configure the context menu provider
		ContextMenuProvider cmProvider = new DeployerEditorContextMenuProvider(
				viewer, this, getActionRegistry());
		viewer.setContextMenu(cmProvider);
		getSite().registerContextMenu(cmProvider, viewer);
	}

	private void createOutputStream(OutputStream os) throws IOException,
			JAXBException {
		JAXBContext context = JAXBContext.newInstance(DeploymentDiagram.class
				.getPackage().getName());
		Marshaller m = context.createMarshaller();
		OutputStreamWriter writer = new OutputStreamWriter(os);
		m.marshal(diagram, writer);
	}

	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {
			protected void configurePaletteViewer(PaletteViewer viewer) {
				super.configurePaletteViewer(viewer);
				viewer
						.addDragSourceListener(new TemplateTransferDragSourceListener(
								viewer));
			}
		};
	}

	private TransferDropTargetListener createTransferDropTargetListener() {
		return new TemplateTransferDropTargetListener(getGraphicalViewer()) {
			protected CreationFactory getFactory(Object template) {
				return new SimpleFactory((Class) template);
			}
		};
	}

	public void doSave(IProgressMonitor monitor) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			createOutputStream(out);
			IFile file = ((IFileEditorInput) getEditorInput()).getFile();
			file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
					false, monitor);
			getCommandStack().markSaveLocation();
		} catch (CoreException ce) {
			ce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	public void doSaveAs() {
		// Show a SaveAs dialog
		Shell shell = getSite().getWorkbenchWindow().getShell();
		SaveAsDialog dialog = new SaveAsDialog(shell);
		dialog.setOriginalFile(((IFileEditorInput) getEditorInput()).getFile());
		dialog.open();

		IPath path = dialog.getResult();
		if (path != null) {
			final IFile file = ResourcesPlugin.getWorkspace().getRoot()
					.getFile(path);
			try {
				new ProgressMonitorDialog(shell).run(false, false,
						new WorkspaceModifyOperation() {
							public void execute(final IProgressMonitor monitor) {
								try {
									ByteArrayOutputStream out = new ByteArrayOutputStream();
									createOutputStream(out);
									file.create(new ByteArrayInputStream(out
											.toByteArray()), true, monitor);
								} catch (CoreException ce) {
									ce.printStackTrace();
								} catch (IOException ioe) {
									ioe.printStackTrace();
								} catch (JAXBException e) {
									e.printStackTrace();
								}
							}
						});
				setInput(new FileEditorInput(file));
				getCommandStack().markSaveLocation();
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			} catch (InvocationTargetException ite) {
				ite.printStackTrace();
			}
		}
	}

	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class)
			return new DeployerOutlinePage(new TreeViewer());
		return super.getAdapter(type);
	}

	DeploymentDiagram getModel() {
		return diagram;
	}

	protected FlyoutPreferences getPalettePreferences() {
		return DeployerEditorPaletteFactory.createPalettePreferences();
	}

	protected PaletteRoot getPaletteRoot() {
		return DeployerEditorPaletteFactory.createPalette(this);
	}

	public IProject getProject() {
		return project;
	}

	private void handleLoadException(Exception e) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		String messageText = (e.getMessage() == null ? "Null message" : e
				.getMessage());
		Status s = new Status(Status.ERROR, "not_used", 0, messageText, e);
		e.printStackTrace();
		ErrorDialog.openError(shell, "Unable to load deployment diagram", null,
				s);
		diagram = new DeploymentDiagram();
	}

	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		GraphicalViewer viewer = getGraphicalViewer();

		viewer.setContents(getModel());
		refreshConnections(getModel());
		viewer.addDropTargetListener(createTransferDropTargetListener());
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	private void refreshConnections(DeploymentDiagram model) {
		for (AbstractComponent component : model.getChildren()) {
			if (component instanceof BindingComponent) {
				((BindingComponent) component).refreshConnections();
			} else if (component instanceof ServiceAssembly) {
				for (ServiceUnit unit : ((ServiceAssembly) component)
						.getServiceUnit()) {
					unit.refreshConnections();
				}
			}
		}

	}

	protected void setInput(IEditorInput input) {
		super.setInput(input);
		try {
			IFile file = ((IFileEditorInput) input).getFile();
			JAXBContext context = JAXBContext
					.newInstance(DeploymentDiagram.class.getPackage().getName());
			Unmarshaller m = context.createUnmarshaller();
			this.project = file.getProject();
			diagram = (DeploymentDiagram) m.unmarshal(file.getContents());
		} catch (CoreException e) {
			handleLoadException(e);
		} catch (JAXBException e) {
			handleLoadException(e);
		} catch (RuntimeException e) {
			handleLoadException(e);
		}
	}
}