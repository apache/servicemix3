package org.apache.servicemix.packaging;

import java.io.IOException;
import java.io.Writer;

import org.apache.servicemix.descriptors.bundled.assets.BundledAssets.Connection;
import org.apache.servicemix.descriptors.bundled.assets.BundledAssets.Parameter;
import org.apache.servicemix.packaging.model.ServiceUnit;

public class AssetPropertiesWriter {

	public void write(Writer writer, ServiceUnit unit) throws IOException {
		writer.append("serviceUnitName=");
		writer.append(unit.getServiceUnitName());
		writer.append("\n");
		writer.append("serviceNamespaceURI=");
		writer.append(unit.getServiceName().getNamespaceURI());
		writer.append("\n");
		writer.append("serviceLocalPart=");
		writer.append(unit.getServiceName().getLocalPart());
		writer.append("\n");
		for (Parameter value : unit.getStoredAssets().getParameter()) {
			writer.append(value.getName());
			writer.append("=");
			writer.append(value.getValue());
			writer.append("\n");
		}
		for (Connection reference : unit.getStoredAssets().getConnection()) {
			writer.append(reference.getName());
			writer.append("NamespaceURI=");
			writer.append(reference.getQname().getNamespaceURI());
			writer.append("\n");
			writer.append(reference.getName());
			writer.append("LocalPart=");
			writer.append(reference.getQname().getLocalPart());
			writer.append("\n");
		}
	}
}
