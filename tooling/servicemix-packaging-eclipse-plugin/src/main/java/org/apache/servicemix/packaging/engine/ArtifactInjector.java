package org.apache.servicemix.packaging.engine;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.descriptors.bundled.assets.BundledAssets;
import org.apache.servicemix.descriptors.bundled.assets.BundledAssets.Artifact;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArtifactInjector implements PackagingInjector {

	private BundledAssets storedAssets;

	public boolean canInject(ModelElement modelElement) {
		if (modelElement instanceof BindingComponent) {
			storedAssets = ((BindingComponent) modelElement).getStoredAssets();
			return true;
		}
		if (modelElement instanceof ServiceUnit) {
			storedAssets = ((ServiceUnit) modelElement).getStoredAssets();
			return true;
		}
		return false;
	}

	public void inject(IProgressMonitor monitor, IProject project,
			ZipOutputStream outputStream) {
		try {
			for (Artifact reference : storedAssets.getArtifact()) {

				InputStream inputStream = project.getFile(reference.getPath())
						.getContents();
				byte[] theBytes;
				theBytes = new byte[inputStream.available()];
				inputStream.read(theBytes);
				outputStream.putNextEntry(new ZipEntry(reference.getName()));
				outputStream.write(theBytes);
				outputStream.closeEntry();
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to inject artifacts");
		}
	}

}
