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
package org.apache.servicemix.packaging.parts.descriptors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.servicemix.descriptors.packaging.assets.Artifact;
import org.apache.servicemix.descriptors.packaging.assets.Assets;
import org.apache.servicemix.descriptors.packaging.assets.Connection;
import org.apache.servicemix.descriptors.packaging.assets.Parameter;
import org.apache.servicemix.packaging.parts.DeploymentDiagramEditPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A simple factory that can take the description of assets for a component and
 * generate the PropertyDescriptors for the GEF property pages
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class AssetDescriptorFactory {

	private static final String PARA_TYPE_TEXT = "text";

	public static Collection<? extends IPropertyDescriptor> getDescriptors(
			Assets assets, DeploymentDiagramEditPart diagram) {
		List<IPropertyDescriptor> descriptors = new ArrayList<IPropertyDescriptor>();

		// Generate descriptors for parameters
		for (Parameter parameter : assets.getParameter()) {
			if (parameter.getType().equals(PARA_TYPE_TEXT)) {
				TextPropertyDescriptor textProperty = new TextPropertyDescriptor(
						parameter, parameter.getDescription());
				textProperty.setCategory(parameter.getCategory());
				descriptors.add(textProperty);
			}
		}

		// Generate descriptors for connections
		for (Connection connection : assets.getConnection()) {
			if ("provides".equals(connection.getType())) {
				QNamePropertyDescriptor qnameProperty = new QNamePropertyDescriptor(
						connection, connection.getDescription(), null);
				qnameProperty.setCategory(connection.getCategory());
				descriptors.add(qnameProperty);
			} else {
				QNamePropertyDescriptor qnameProperty = new QNamePropertyDescriptor(
						connection, connection.getDescription(), diagram);
				qnameProperty.setCategory(connection.getCategory());
				descriptors.add(qnameProperty);
			}

		}

		// Generate descriptiors for artifacts
		for (Artifact embeddedArtifact : assets.getArtifact()) {
			EmbeddedArtifactDescriptor embeddedArtifactDescriptor = new EmbeddedArtifactDescriptor(
					embeddedArtifact, embeddedArtifact.getDescription(),
					diagram, embeddedArtifact.getExtension());
			embeddedArtifact.setCategory(embeddedArtifact.getCategory());
			descriptors.add(embeddedArtifactDescriptor);
		}

		return descriptors;
	}
}
