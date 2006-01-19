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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Components
 * 
 * @author <a href="mailto:costello.tony@gmail.com">Tony Costello </a>
 * 
 */
@XmlRootElement
public class Components {

	private List<Component> component = new ArrayList<Component>();

	public List<Component> getComponent() {
		return component;
	}

	public void setComponent(List<Component> service) {
		this.component = service;
	}
}
