package org.apache.servicemix.packaging.engine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ServiceAssemblyPackagingEngine extends AbstractPackagingEngine {

	private ServiceAssembly serviceAssembly;

	public boolean canDeploy(ModelElement modelElement) {
		if (modelElement instanceof ServiceAssembly) {
			serviceAssembly = (ServiceAssembly) modelElement;
			setArtifact(serviceAssembly.getComponentArtifact());
			return true;
		}
		return false;
	}

	public void deploy(IProgressMonitor monitor, IProject project) {
		ZipOutputStream out = null;
		try {
			String fileName = "/" + serviceAssembly.getName() + "-sa.zip";
			out = new ZipOutputStream(new FileOutputStream(
					getDeploymentDir(serviceAssembly) + fileName));

			for (PackagingInjector injector : getInjectors()) {
				if (injector.canInject(serviceAssembly)) {
					injector.inject(monitor, project, out);
				}
			}
			
			out.close();
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
