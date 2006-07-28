/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.servicemix.descriptors.packaging.assets.Connection;
import org.apache.servicemix.packaging.parts.descriptors.ServiceNameHelper;

/**
 * A connection decorator handles the construction of connections between
 * connectables based on their stored assets and resource references
 * 
 * TODO Should ConnectionDecorator be an interface to allow different types?
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ConnectionDecorator implements PropertyChangeListener {

	private AbstractConnectableService component;

	public ConnectionDecorator(AbstractConnectableService component) {
		this.component = component;
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if ((!evt.getPropertyName().equals(
				AbstractConnectableService.SOURCE_CONNECTIONS_PROP))
				&& (!evt.getPropertyName().equals(
						AbstractConnectableService.TARGET_CONNECTIONS_PROP))) {
			retargetConnections();
		}

	}

	private void retargetConnections() {
		for (ComponentConnection connection : component.getSourceConnections()) {
			component.removeConnection(connection);
		}
		for (Connection reference : component.getStoredAssets().getConnection()) {
			if ("consumes".equals(reference.getType())) {
				Connectable target = ServiceNameHelper.getConnectableByQName(
						getDeploymentDiagram(component), reference.getQname());
				if ((target != null) && (!target.equals(component))) {
					ComponentConnection connection = new ComponentConnection(
							component, target);
					component.addConnection(connection);
				}
			}
		}

	}

	private DeploymentDiagram getDeploymentDiagram(
			AbstractConnectableService component2) {
		if (component2.getParentModelElement() instanceof DeploymentDiagram)
			return (DeploymentDiagram) component2.getParentModelElement();
		else if (component2.getParentModelElement() instanceof ServiceAssembly)
			return (DeploymentDiagram) ((ServiceAssembly) component2
					.getParentModelElement()).getParentModelElement();
		return null;
	}

}
