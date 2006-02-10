package org.apache.servicemix.packaging.engine;

import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface PackagingInjector {

	public boolean canInject(ModelElement modelElement);

	public void inject(IProgressMonitor monitor, IProject project,
			ZipOutputStream outputStream);

}
