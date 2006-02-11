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
package org.apache.servicemix.packaging.engine;

import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.ModelElement;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ServiceAssemblyDescriptorInjector implements PackagingInjector {

	private ServiceAssembly serviceAssembly;

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
			StringWriter stringWriter = new StringWriter();
			ServiceAssemblyDescriptorWriter writer = new ServiceAssemblyDescriptorWriter();
			writer.write(stringWriter, serviceAssembly);
			outputStream.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
			outputStream.write(stringWriter.toString().getBytes());
			outputStream.closeEntry();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
