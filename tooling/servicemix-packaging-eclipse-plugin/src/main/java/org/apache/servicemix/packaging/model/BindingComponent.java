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
package org.apache.servicemix.packaging.model;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.servicemix.descriptors.packaging.assets.Components.Component;
import org.apache.servicemix.packaging.ComponentArtifact;
import org.apache.servicemix.packaging.ComponentArtifactFactory;

/**
 * An instance of a JBI Binding Component
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class BindingComponent extends AbstractConnectableService implements
		ComponentBased {

	private String componentName;

	@XmlTransient
	public ComponentArtifact getComponentArtifact() {
		for (ComponentArtifact artifact : ComponentArtifactFactory
				.getComponentArtifacts()) {
			for (Component component : artifact.getComponents().getComponent()) {
				if (component.getName().equals(getComponentName()))
					return artifact;
			}
		}
		return null;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

}