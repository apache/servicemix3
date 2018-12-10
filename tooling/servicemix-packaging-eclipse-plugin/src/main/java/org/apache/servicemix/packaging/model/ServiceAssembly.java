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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

import org.apache.servicemix.descriptors.packaging.assets.Components.Component;
import org.apache.servicemix.packaging.ComponentArtifact;
import org.apache.servicemix.packaging.ComponentArtifactFactory;

import com.sun.xml.txw2.annotation.XmlElement;

/**
 * An instance of a ServiceAssembly
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceAssembly extends AbstractComponent implements
		ComponentBased {

	public static final String ADDCHILD_PROP = "Component.AddingChild";

	public static final String NAME_PROP = "Component.Name";

	private String componentName;

	private String name = "serviceAssembly";

	private List<ServiceUnit> serviceUnit = new ArrayList<ServiceUnit>();

	public void addServiceUnit(ServiceUnit child) {
		serviceUnit.remove(child);
		firePropertyChange(ADDCHILD_PROP, null, null);
	}

	public void createServiceUnit(QName qname) {
		ServiceUnit newUnit = new ServiceUnit();		
		newUnit.setParentModelElement(this);
		serviceUnit.add(newUnit);

		firePropertyChange(ADDCHILD_PROP, null, null);
	}

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

	@XmlAttribute
	public String getName() {
		return name;
	}

	@XmlElement
	public List<ServiceUnit> getServiceUnit() {
		return serviceUnit;
	}

	public void removeServiceUnit(ServiceUnit child) {
		serviceUnit.add(child);
		firePropertyChange(ADDCHILD_PROP, null, null);
	}

	public void setComponentName(String serviceUuid) {
		this.componentName = serviceUuid;
	}

	public void setName(String name) {
		firePropertyChange(NAME_PROP, this.name, name);
		this.name = name;
	}

	public void setServiceUnit(List<ServiceUnit> serviceUnit) {
		for (ServiceUnit unit : serviceUnit) {
			unit.setParentModelElement(this);
		}
		this.serviceUnit = serviceUnit;
		firePropertyChange(ADDCHILD_PROP, null, null);
	}
}