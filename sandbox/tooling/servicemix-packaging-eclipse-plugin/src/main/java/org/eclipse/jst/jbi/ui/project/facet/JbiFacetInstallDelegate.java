package org.eclipse.jst.jbi.ui.project.facet;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jst.common.project.facet.WtpUtils;
import org.eclipse.jst.common.project.facet.core.ClasspathHelper;
import org.eclipse.jst.common.project.facet.core.IClasspathProvider;
import org.eclipse.jst.j2ee.project.facet.J2EEFacetInstallDelegate;
import org.eclipse.jst.jbi.internal.project.operations.IJbiFacetInstallDataModelProperties;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.datamodel.FacetDataModelProvider;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
import org.eclipse.wst.common.project.facet.core.IDelegate;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;

public class JbiFacetInstallDelegate extends J2EEFacetInstallDelegate implements
		IDelegate {

	public void execute(IProject project, IProjectFacetVersion fv,
			Object config, IProgressMonitor monitor) throws CoreException {
		if (monitor != null) {
			monitor.beginTask("Installing JBI facet", 1);
		}

		try {
			IDataModel model = (IDataModel) config;

			final IJavaProject jproj = JavaCore.create(project);

			// Add WTP natures.
			WtpUtils.addNatures(project);

			// Setup the flexible project structure.

			final IVirtualComponent c = ComponentCore.createComponent(project);
			c.create(0, null);
			c.setMetaProperty("java-output-path", "/build/classes/");

			final IVirtualFolder jbiRoot = c.getRootFolder();

			// Create the directory structure.
			final IWorkspace ws = ResourcesPlugin.getWorkspace();
			final IPath pjpath = project.getFullPath();

			IFolder jbiFolder = null;
			String srcFolder = null;

			srcFolder = model
					.getStringProperty(IJbiFacetInstallDataModelProperties.JAVA_SOURCE_FOLDER);
			jbiRoot.createLink(new Path("/" + srcFolder), 0, null);

			String resourcesFolder = model
					.getStringProperty(IJbiFacetInstallDataModelProperties.CONFIG_FOLDER);
			jbiRoot.createLink(new Path("/" + resourcesFolder), 0, null);

			IPath jbiFolderpath = pjpath.append(resourcesFolder);
			jbiFolder = ws.getRoot().getFolder(jbiFolderpath);

			// Add a manifest
			try {
				createManifest(project, jbiFolder, monitor);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Setup the classpath.			

			// TODO Need to ensure that both the resources and src are on the
			// classpath (as per normal M2 archetype)
			// ClasspathHelper.addClasspathEntries(project, fv, cpEntries);			

			if (!ClasspathHelper.addClasspathEntries(project, fv)) {
				System.out.println("Nothing added to runtime on project "
						+ project + " for facet version " + fv);
			}

			try {
				((IDataModelOperation) model
						.getProperty(FacetDataModelProvider.NOTIFICATION_OPERATION))
						.execute(monitor, null);
			} catch (org.eclipse.core.commands.ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (monitor != null) {
				monitor.worked(1);
			}
		}

		finally {
			if (monitor != null) {
				monitor.done();
			}
		}

	}
}
