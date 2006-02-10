package org.apache.servicemix.packaging.engine;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.ComponentArtifact;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ComponentFilesInjector implements PackagingInjector {

	private ComponentArtifact artifact;

	private String componentName;

	public boolean canInject(ModelElement modelElement) {
		if (modelElement instanceof BindingComponent) {
			componentName = ((BindingComponent) modelElement)
					.getComponentName();
			artifact = ((BindingComponent) modelElement).getComponentArtifact();
			return true;
		}
		return false;
	}

	public void inject(IProgressMonitor monitor, IProject project,
			ZipOutputStream outputStream) {
		try {
			for (String fileName : artifact.getResourceMap().keySet()) {
				if (!fileName.equals("META-INF/components.xml")) {
					if (fileName.equals(artifact.getComponentDefinitionByName(
							componentName).getAssets().getJbiDescriptor())) {
						outputStream.putNextEntry(new ZipEntry(
								"META-INF/jbi.xml"));
					} else
						outputStream.putNextEntry(new ZipEntry(fileName));
					outputStream.write(artifact.getResourceMap().get(fileName));
					outputStream.closeEntry();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
