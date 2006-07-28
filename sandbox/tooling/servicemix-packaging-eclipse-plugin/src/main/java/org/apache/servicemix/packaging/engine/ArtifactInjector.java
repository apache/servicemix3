/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.packaging.engine;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.descriptors.packaging.assets.Artifact;
import org.apache.servicemix.descriptors.packaging.assets.Assets;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ArtifactInjector implements PackagingInjector {

	private Assets storedAssets;

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
