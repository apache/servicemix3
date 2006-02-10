package org.apache.servicemix.packaging.engine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class BindingComponentPackagingEngine extends AbstractPackagingEngine {

	private BindingComponent bindingComponent;

	public boolean canDeploy(ModelElement modelElement) {
		if (modelElement instanceof BindingComponent) {
			bindingComponent = (BindingComponent) modelElement;
			setArtifact(bindingComponent.getComponentArtifact());
			return true;
		}
		return false;
	}

	public void deploy(IProgressMonitor monitor, IProject project) {

		ZipOutputStream out = null;
		try {
			String fileName = "/"
					+ bindingComponent.getServiceName().getLocalPart()
					+ "-bc.zip";
			out = new ZipOutputStream(new FileOutputStream(
					getInstallPath(bindingComponent) + fileName));

			for (PackagingInjector injector : getInjectors()) {
				if (injector.canInject(bindingComponent)) {
					injector.inject(monitor, project, out);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// Ignore?
				}
		}
	}

	public void undeploy(IProgressMonitor monitor, IProject project) {
		// TODO Needs implementing

	}

}
