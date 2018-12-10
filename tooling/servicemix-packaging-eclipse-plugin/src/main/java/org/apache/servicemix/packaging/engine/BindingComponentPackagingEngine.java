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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ModelElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public class BindingComponentPackagingEngine extends AbstractPackagingEngine {

	private BindingComponent bindingComponent;

	public boolean canDeploy(ModelElement modelElement) {
		if (modelElement instanceof BindingComponent) {
			bindingComponent = (BindingComponent) modelElement;
			setArtifact(bindingComponent.getComponentArtifact());
			return true;
		}
		return false;
	}

	public void deploy(IProgressMonitor monitor, IProject project) {

		ZipOutputStream out = null;
		try {
			String fileName = "/"
					+ bindingComponent.getServiceName().getLocalPart()
					+ "-bc.zip";
			out = new ZipOutputStream(new FileOutputStream(
					getInstallPath(bindingComponent) + fileName));

			for (PackagingInjector injector : getInjectors()) {
				if (injector.canInject(bindingComponent)) {
					injector.inject(monitor, project, out);
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// Ignore?
				}
		}
	}

	public void undeploy(IProgressMonitor monitor, IProject project) {
		// TODO Needs implementing

	}

}
