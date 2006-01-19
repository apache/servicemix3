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
package org.apache.servicemix.packaging.assets;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

/**
 * ComponentAssets
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
@XmlRootElement
public class ComponentAssets {

	private List<ParameterValue> parameterValue = new ArrayList<ParameterValue>();

	private List<ResourceReference> resourceReference = new ArrayList<ResourceReference>();

	private QName serviceName = new QName("http://openuri.org", "newService");

	private String name;

	public List<ParameterValue> getParameterValue() {
		return parameterValue;
	}

	public List<ResourceReference> getResourceReference() {
		return resourceReference;
	}

	public QName getServiceName() {
		return serviceName;
	}

	public void setParameterValue(List<ParameterValue> parameterValue) {
		this.parameterValue = parameterValue;
	}

	public void setResourceReference(List<ResourceReference> resourceReference) {
		this.resourceReference = resourceReference;
	}

	public void setServiceName(QName serviceName) {
		this.serviceName = serviceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
