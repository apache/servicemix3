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

import javax.xml.namespace.QName;

/**
 * ResourceReference
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ResourceReference {

	private String name;

	private QName resource;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public QName getResource() {
		return resource;
	}

	public void setResource(QName resource) {
		this.resource = resource;
	}
}
