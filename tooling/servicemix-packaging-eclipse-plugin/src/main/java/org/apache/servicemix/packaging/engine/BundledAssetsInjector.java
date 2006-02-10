package org.apache.servicemix.packaging.engine;

import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.servicemix.descriptors.bundled.assets.BundledAssets;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class BundledAssetsInjector implements PackagingInjector {

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
			JAXBContext context = JAXBContext.newInstance(BundledAssets.class
					.getPackage().getName());
			Marshaller m = context.createMarshaller();
			final StringWriter write = new StringWriter();
			m.marshal(storedAssets, write);
			outputStream.putNextEntry(new ZipEntry(
					"META-INF/bundled-assets.xml"));
			outputStream.write(write.toString().getBytes());
			outputStream.closeEntry();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
