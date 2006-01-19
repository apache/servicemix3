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
package org.apache.servicemix.packaging.descriptor;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Component
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class Component {

	private Assets assets;

	private String componentDescriptor = "META-INF/jbi.xml";

	private String componentUuid;

	private String description;

	private String failedImage = "images/failed.gif";

	private String name;

	private String serviceImage = "images/service.gif";

	private ServiceUnit serviceUnit;

	private String startedImage = "images/started.gif";

	private String stoppedImage = "images/stopped.gif";

	private String type = "binding-component";

	@XmlElement(nillable = true)
	public Assets getAssets() {
		return assets;
	}

	@XmlAttribute
	public String getComponentDescriptor() {
		return componentDescriptor;
	}

	@XmlElement(nillable = false)
	public String getComponentUuid() {
		return componentUuid;
	}

	@XmlElement(nillable = true)
	public String getDescription() {
		return description;
	}

	@XmlElement(nillable = true)
	public String getFailedImage() {
		return failedImage;
	}

	@XmlElement(nillable = true)
	public String getName() {
		return name;
	}

	@XmlElement(nillable = true)
	public String getServiceImage() {
		return serviceImage;
	}

	@XmlElement(nillable = true)
	public ServiceUnit getServiceUnit() {
		return serviceUnit;
	}

	@XmlElement(nillable = true)
	public String getStartedImage() {
		return startedImage;
	}

	@XmlElement(nillable = true)
	public String getStoppedImage() {
		return stoppedImage;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}

	public void setAssets(Assets assets) {
		this.assets = assets;
	}

	public void setComponentDescriptor(String componentDescriptor) {
		this.componentDescriptor = componentDescriptor;
	}

	public void setComponentUuid(String serviceUuid) {
		this.componentUuid = serviceUuid;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFailedImage(String failedImage) {
		this.failedImage = failedImage;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setServiceImage(String serviceImage) {
		this.serviceImage = serviceImage;
	}

	public void setServiceUnit(ServiceUnit serviceUnit) {
		this.serviceUnit = serviceUnit;
	}

	public void setStartedImage(String startedImage) {
		this.startedImage = startedImage;
	}

	public void setStoppedImage(String stoppedImage) {
		this.stoppedImage = stoppedImage;
	}

	public void setType(String type) {
		this.type = type;
	}
}
