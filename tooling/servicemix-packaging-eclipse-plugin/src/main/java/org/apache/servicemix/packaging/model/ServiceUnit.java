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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.servicemix.packaging.ComponentArtifact;

/**
 * An instance of a Service Unit
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceUnit extends AbstractConnectableService implements
		ComponentBased {

	private static final long serialVersionUID = -4682078748821423318L;

	public ServiceAssembly parentAssembly;

	private String serviceUnitName = "serviceUnit";

	public ComponentArtifact getComponentArtifact() {
		return ((ServiceAssembly) getParentModelElement())
				.getComponentArtifact();
	}

	@XmlTransient
	public String getComponentName() {
		return ((ServiceAssembly) getParentModelElement()).getComponentName();
	}

	@XmlAttribute
	public String getServiceUnitName() {
		return serviceUnitName;
	}

	public void setComponentName(String serviceUuid) {
		// ignore?
	}

	public void setServiceUnitName(String serviceUnitName) {
		this.serviceUnitName = serviceUnitName;
	}

}
