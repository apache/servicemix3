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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.servicemix.descriptors.deployment.assets.Components.Component;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.eclipse.core.resources.IProject;
import org.w3c.dom.Element;

/**
 * The Service Assembly Deployer
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class ServiceAssemblyDeployer extends AbstractDeployer {

	public ServiceAssemblyDeployer(ComponentArtifact artifact) {
		setArtifact(artifact);
	}

	public void deploy(IProject project, ServiceAssembly assembly,
			boolean deployComponent) throws InvocationTargetException {

		if (deployComponent)
			deployComponent(assembly);

		ZipOutputStream out = null;
		try {
			String fileName = "/" + assembly.getName() + "-sa.zip";
			out = new ZipOutputStream(new FileOutputStream(
					getDeploymentDir(assembly) + fileName));

			Component definition = assembly.getComponentArtifact()
					.getComponentDefinitionByName(assembly.getComponentName());
			if ((definition.getAssets() != null)
					&& (definition.getAssets().getDeploymentAssistants() != null)) {
				List<Element> elements = definition.getAssets()
						.getDeploymentAssistants().getAnyOrAny();
				for(Element element : elements) {
					System.out.println("Element:"+element);
				}
			}

			injectServiceAssemblyDescriptor(assembly, out);
			injectBundledAssets(assembly.getStoredAssets(), out);

			for (ServiceUnit unit : assembly.getServiceUnit()) {
				injectServiceUnitZip(project, unit, out);
			}
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

	private void deployComponent(ServiceAssembly c)
			throws InvocationTargetException {
		ZipOutputStream out = null;
		try {
			Component componentDefintion = c.getComponentArtifact()
					.getComponentDefinitionByName(c.getComponentName());
			File artifactFile = new File(getArtifactForComponent(
					componentDefintion).getArchivePath());

			out = new ZipOutputStream(new FileOutputStream(getInstallPath(c)
					+ "/" + artifactFile.getName()));
			injectComponentFiles(out, c.getComponentName());
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

	private void injectServiceUnitZip(IProject project, ServiceUnit unit,
			ZipOutputStream out) throws Exception {
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

		ZipOutputStream suZip = new ZipOutputStream(bytesOut);
		if (unit.getStoredAssets() != null) {
			injectEmbeddedArtifacts(unit.getStoredAssets(), suZip, project);
			injectBundledAssets(unit.getStoredAssets(), suZip);
			injectParametersAsProperties(unit, suZip);
		}
		suZip.close();

		out.putNextEntry(new ZipEntry(unit.getServiceUnitName() + ".zip"));
		out.write(bytesOut.toByteArray());
		out.closeEntry();

	}

	private void injectServiceAssemblyDescriptor(ServiceAssembly assembly,
			ZipOutputStream out) throws Exception {
		StringWriter stringWriter = new StringWriter();
		ServiceAssemblyDescriptorWriter writer = new ServiceAssemblyDescriptorWriter();
		writer.write(stringWriter, assembly);
		out.putNextEntry(new ZipEntry("META-INF/jbi.xml"));
		out.write(stringWriter.toString().getBytes());
		out.closeEntry();
	}

	private void injectParametersAsProperties(ServiceUnit unit,
			ZipOutputStream out) throws Exception {
		StringWriter stringWriter = new StringWriter();
		AssetPropertiesWriter writer = new AssetPropertiesWriter();
		writer.write(stringWriter, unit);
		out.putNextEntry(new ZipEntry("servicemix.properties"));
		out.write(stringWriter.toString().getBytes());
		out.closeEntry();
	}

	public void undeploy(ServiceAssembly assembly) {
		// TODO

	}

}
