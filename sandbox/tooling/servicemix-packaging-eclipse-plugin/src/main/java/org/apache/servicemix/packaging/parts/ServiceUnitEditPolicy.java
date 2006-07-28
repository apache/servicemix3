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
package org.apache.servicemix.packaging.parts;

import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;
import org.apache.servicemix.packaging.model.commands.ComponentDeleteCommand;
import org.apache.servicemix.packaging.model.commands.ServiceUnitDeleteCommand;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

/**
 * The GEF edit policy for a Service Unit
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceUnitEditPolicy extends ComponentEditPolicy {

	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		Object parent = getHost().getParent().getModel();
		Object child = getHost().getModel();
		if (parent instanceof ServiceAssembly && child instanceof ServiceUnit) {
			return new ServiceUnitDeleteCommand((ServiceAssembly) parent,
					(ServiceUnit) child);
		}
		return super.createDeleteCommand(deleteRequest);
	}
}
