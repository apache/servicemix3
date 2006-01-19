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
 * Connection
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
public class Connection {

	private String category = null;

	private boolean defaultDestination = false;

	private String name;

	private String description;

	@XmlAttribute
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@XmlAttribute
	public String getCategory() {
		return category;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	@XmlAttribute
	public boolean isDefaultDestination() {
		return defaultDestination;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setDefaultDestination(boolean defaultDestination) {
		this.defaultDestination = defaultDestination;
	}

	public void setName(String name) {
		this.name = name;
	}

}
