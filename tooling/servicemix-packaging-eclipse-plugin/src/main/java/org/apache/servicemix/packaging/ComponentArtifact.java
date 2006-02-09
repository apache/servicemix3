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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.servicemix.descriptors.deployment.assets.Components;
import org.apache.servicemix.descriptors.deployment.assets.Components.Component;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A base container for the information and resources in a Component Artifact
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class ComponentArtifact {

	public static final String META_INF_COMPONENTS_XML = "META-INF/components.xml";

	private String archivePath;

	private Components components;

	private DeploymentEngine deploymentEngine;

	private JarResources jar;

	public ComponentArtifact(String archivePath) throws InvalidArchiveException {
		this.archivePath = archivePath;
		File file = new File(archivePath);
		if (!file.exists()) {
			throw new InvalidArchiveException(
					"The archive doesn't exist, or could not be found ["
							+ archivePath + "]");
		}

		getComponentDescription(file);

		// Create a deployment engine for this artifact
		deploymentEngine = new DeploymentEngine(this);

	}

	private Image createImage(InputStream inputStream) {
		if (inputStream != null)
			return new Image(Display.getCurrent(), inputStream);
		else
			return null;
	}

	public String getArchivePath() {
		return archivePath;
	}

	public Components getComponents() {
		return components;
	}

	public DeploymentEngine getDeploymentEngine() {
		return deploymentEngine;
	}

	public Image getFailedImage(String componentName) {
		Image image = createImage(getResource(getComponentDefinitionByName(
				componentName).getAssets().getFailedImage()));
		if (image != null)
			return image;
		else
			return createImage(getClass().getClassLoader().getResourceAsStream(
					"icons/failed.gif"));
	}

	public InputStream getResource(String resource) {
		byte[] resourceBytes = jar.getResource(resource);
		if (resourceBytes != null)
			return new ByteArrayInputStream(resourceBytes);
		else
			return null;
	}

	public Map<String, byte[]> getResourceMap() {
		Map resourceMap = jar.getResourceMap();
		return resourceMap;
	}

	public Component getComponentDefinitionByName(String componentName) {
		for (Component component : components.getComponent()) {
			if (component.getName().equals(componentName))
				return component;
		}
		return null;
	}

	public Image getComponentImage(String componentName) {
		return createImage(getResource(getComponentDefinitionByName(
				componentName).getAssets().getServiceImage()));
	}

	private void getComponentDescription(File file)
			throws InvalidArchiveException {
		try {
			jar = new JarResources(file.getAbsolutePath());
			byte[] serviceDescriptor = jar.getResource(META_INF_COMPONENTS_XML);
			if (serviceDescriptor == null)
				throw new InvalidArchiveException("Missing "
						+ META_INF_COMPONENTS_XML);

			JAXBContext context;
			try {
				context = JAXBContext.newInstance(Components.class.getPackage()
						.getName());
				Unmarshaller m = context.createUnmarshaller();
				components = (Components) m.unmarshal(new ByteArrayInputStream(
						serviceDescriptor));				
			} catch (JAXBException e) {
				throw new InvalidArchiveException(e);
			}
		} catch (IOException e) {
			throw new InvalidArchiveException(e);
		}

	}

	public Image getStartedImage(String componentName) {
		Image image = createImage(getResource(getComponentDefinitionByName(
				componentName).getAssets().getStartedImage()));
		if (image != null)
			return image;
		else
			return createImage(getClass().getClassLoader().getResourceAsStream(
					"icons/started.gif"));

	}

	public Image getStoppedImage(String componentName) {
		Image image = createImage(getResource(getComponentDefinitionByName(
				componentName).getAssets().getStoppedImage()));
		if (image != null)
			return image;
		else
			return createImage(getClass().getClassLoader().getResourceAsStream(
					"icons/stopped.gif"));
	}

	public void setComponents(Components services) {
		this.components = services;
	}

	public String toString() {
		return archivePath;
	}
}