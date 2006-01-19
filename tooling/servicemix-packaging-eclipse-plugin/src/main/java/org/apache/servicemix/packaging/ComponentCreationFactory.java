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
package org.apache.servicemix.packaging;

import org.apache.servicemix.packaging.model.BindingComponent;
import org.apache.servicemix.packaging.model.ComponentBased;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.parts.descriptors.ServiceNameHelper;
import org.eclipse.gef.requests.CreationFactory;

/**
 * The GEF creation factory for the components from the palette
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ComponentCreationFactory implements CreationFactory {

	private String serviceUuid;

	private String type;

	private DeployerEditor editor;

	private static final String TYPE_BC = "binding-component";

	private static final String TYPE_SE = "service-engine";

	public ComponentCreationFactory(DeployerEditor editor, String serviceUuid,
			String type) {
		this.editor = editor;
		this.serviceUuid = serviceUuid;
		this.type = type;
	}

	public Object getNewObject() {
		ComponentBased component = null;
		if (TYPE_BC.equals(type)) {
			component = new BindingComponent();
			((BindingComponent) component)
					.setServiceName(ServiceNameHelper
							.getUniqueServiceName((((DeployerEditor) editor)
									.getModel())));
		} else
			component = new ServiceAssembly();
		component.setComponentUuid(serviceUuid);
		return component;
	}

	public Object getObjectType() {
		return new BindingComponent();
	}

}
