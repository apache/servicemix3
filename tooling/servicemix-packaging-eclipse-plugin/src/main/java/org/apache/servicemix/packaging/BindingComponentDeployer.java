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

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.descriptors.deployment.assets.Components.Component;
import org.apache.servicemix.packaging.model.BindingComponent;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Element;

/**
 * The Binding Component Deployer
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class BindingComponentDeployer extends AbstractDeployer {

	public BindingComponentDeployer(ComponentArtifact artifact) {
		setArtifact(artifact);
	}

	public void deploy(IProject project, BindingComponent component)
			throws InvocationTargetException {
		ZipOutputStream out = null;
		try {
			String fileName = "/" + component.getServiceName().getLocalPart()
					+ "-bc.zip";
			out = new ZipOutputStream(new FileOutputStream(
					getInstallPath(component) + fileName));

			Component definition = component.getComponentArtifact()
					.getComponentDefinitionByName(component.getComponentName());
			if ((definition.getAssets() != null)
					&& (definition.getAssets().getDeploymentAssistants() != null)) {
				List<Element> elements = definition.getAssets()
						.getDeploymentAssistants().getAnyOrAny();
				for (Element element : elements) {
					System.out.println("Element:" + element);
				}
			}
			injectComponentFiles(out, component.getComponentName());
			injectEmbeddedArtifacts(component.getStoredAssets(), out, project);
			injectBundledAssets(component.getStoredAssets(), out);
		} catch (Throwable e) {
			throw new InvocationTargetException(e);
		} finally {
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// Ignore?
				}
		}
	}

	public void undeploy(BindingComponent component) {
		// TODO Auto-generated method stub

	}

}
