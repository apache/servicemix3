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

import java.io.StringWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class ManifestInjector implements PackagingInjector {

	public boolean canInject(ModelElement modelElement) {
		return true;
	}

	public void inject(IProgressMonitor monitor, IProject project,
			ZipOutputStream outputStream) {
		try {
			StringWriter stringWriter = new StringWriter();
			stringWriter.write("Created-By: ServiceMix JBI Packager");
			stringWriter.write("Built-By:" + System.getProperty("user.name"));
			stringWriter.write("Built-By:" + System.getProperty("user.name"));
			outputStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
			outputStream.write(stringWriter.toString().getBytes());
			outputStream.closeEntry();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
