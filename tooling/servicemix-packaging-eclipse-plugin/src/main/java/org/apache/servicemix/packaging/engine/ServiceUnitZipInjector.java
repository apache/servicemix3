package org.apache.servicemix.packaging.engine;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ServiceUnitZipInjector implements PackagingInjector {

	private ServiceAssembly serviceAssembly;

	private List<PackagingInjector> serviceUnitInjectors = new ArrayList<PackagingInjector>();

	public boolean canInject(ModelElement modelElement) {
		if (modelElement instanceof ServiceAssembly) {
			serviceAssembly = (ServiceAssembly) modelElement;
			return true;
		}
		return false;
	}

	public void inject(IProgressMonitor monitor, IProject project,
			ZipOutputStream outputStream) {
		try {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

			for (ServiceUnit unit : serviceAssembly.getServiceUnit()) {
				boolean empty = true;
				ZipOutputStream suZip = new ZipOutputStream(bytesOut);

				for (PackagingInjector injector : serviceUnitInjectors) {
					if (injector.canInject(unit)) {
						injector.inject(monitor, project, suZip);
						empty = false;
					}
				}

				suZip.close();

				outputStream.putNextEntry(new ZipEntry(unit
						.getServiceUnitName()
						+ ".zip"));
				outputStream.write(bytesOut.toByteArray());
				outputStream.closeEntry();

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<PackagingInjector> getServiceUnitInjectors() {
		return serviceUnitInjectors;
	}

	public void setServiceUnitInjectors(
			List<PackagingInjector> serviceUnitInjectors) {
		this.serviceUnitInjectors = serviceUnitInjectors;
	}
}
