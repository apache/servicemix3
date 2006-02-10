package org.apache.servicemix.packaging.engine;

import java.util.List;

import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface PackagingEngine {

	public boolean canDeploy(ModelElement modelElement);

	public void deploy(IProgressMonitor monitor, IProject project);

	public void undeploy(IProgressMonitor monitor, IProject project);

	public void setInjectors(List<PackagingInjector> injectors);	

}
