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

/**
 * EmbeddedArtifact
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class EmbeddedArtifact {

	private String category;

	private String description;

	private String extension;

	private String name;

	@XmlAttribute
	public String getCategory() {
		return category;
	}

	@XmlAttribute
	public String getDescription() {
		return description;
	}

	@XmlAttribute
	public String getExtension() {
		return extension;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public void setName(String name) {
		this.name = name;
	}

}
