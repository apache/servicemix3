/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.servicemix.packaging.assets.ArtifactReference;
import org.apache.servicemix.packaging.assets.StoredAssets;
import org.eclipse.core.resources.IProject;

/**
 * The AbstractDeployer offers some shared functionality between the BC and SA
 * deployers
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public abstract class AbstractDeployer {

	private ComponentArtifact artifact;

	public ComponentArtifact getArtifact() {
		return artifact;
	}

	public String getDeploymentDir() {
		return "d:/";
	}

	protected void injectComponentFiles(ZipOutputStream out,
			String componentUuid) throws IOException {
		for (String fileName : getArtifact().getResourceMap().keySet()) {
			if (!fileName.equals("META-INF/components.xml")) {
				if (fileName.equals(getArtifact().getComponentDefinitionByUuid(
						componentUuid).getComponentDescriptor())) {
					out.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
				} else
					out.putNextEntry(new ZipEntry(fileName));
				out.write(artifact.getResourceMap().get(fileName));
				out.closeEntry();
			}
		}
	}

	protected void injectEmbeddedArtifacts(StoredAssets storedAssets,
			ZipOutputStream suZip, IProject project) throws Exception {
		for (ArtifactReference reference : storedAssets.getArtifactReference()) {
			InputStream inputStream = project.getFile(reference.getPath())
					.getContents();
			byte[] theBytes = new byte[inputStream.available()];
			inputStream.read(theBytes);
			suZip.putNextEntry(new ZipEntry(reference.getName()));
			suZip.write(theBytes);
			suZip.closeEntry();
		}

	}

	protected void injectStoredAssets(StoredAssets assets, ZipOutputStream out)
			throws Exception {
		JAXBContext context = JAXBContext.newInstance(StoredAssets.class
				.getPackage().getName());
		Marshaller m = context.createMarshaller();
		final StringWriter write = new StringWriter();
		m.marshal(assets, write);
		out.putNextEntry(new ZipEntry("META-INF/stored-assets.xml"));
		out.write(write.toString().getBytes());
		out.closeEntry();
	}

	public void setArtifact(ComponentArtifact artifact) {
		this.artifact = artifact;
	}
}
