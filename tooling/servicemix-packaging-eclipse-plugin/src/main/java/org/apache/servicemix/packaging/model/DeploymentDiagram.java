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

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The base of the deployment diagram model
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
@XmlRootElement
public class DeploymentDiagram extends ModelElement {

	public static final String CHILD_ADDED_PROP = "DeploymentDiagram.ChildAdded";

	public static final String CHILD_REMOVED_PROP = "DeploymentDiagram.ChildRemoved";

	private static final long serialVersionUID = 1;

	private String path = "c:\temp";

	private List<AbstractComponent> services = new ArrayList<AbstractComponent>();

	public boolean addChild(AbstractComponent s) {
		s.setParentModelElement(this);
		if (s != null && services.add(s)) {
			firePropertyChange(CHILD_ADDED_PROP, null, s);
			return true;
		}
		return false;
	}

	@XmlTransient
	public List<AbstractComponent> getChildren() {
		return services;
	}

	public String getPath() {
		return path;
	}

	public List<AbstractComponent> getService() {
		return services;
	}

	public boolean removeChild(AbstractComponent component) {
		if (component != null && services.remove(component)) {
			firePropertyChange(CHILD_REMOVED_PROP, null, component);
			return true;
		}
		return false;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setService(List<AbstractComponent> services) {
		for (AbstractComponent component : services) {
			component.setParentModelElement(this);
		}
		this.services = services;
	}
}
