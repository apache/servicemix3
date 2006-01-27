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
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.eclipse.core.resources.IProject;

/**
 * The DeploymentEngine is simply a container to pass through to either the BC
 * and SA deployers
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class DeploymentEngine {

	private ServiceAssemblyDeployer assemblyDeployer;

	private BindingComponentDeployer bindingComponentDeployer;

	public DeploymentEngine(ComponentArtifact artifact) {
		this.assemblyDeployer = new ServiceAssemblyDeployer(artifact);
		this.bindingComponentDeployer = new BindingComponentDeployer(artifact);
	}

	public void deployBindingComponent(IProject project,
			BindingComponent component) throws Exception {
		bindingComponentDeployer.deploy(project, component);
	}

	public void undeployBindingComponent(BindingComponent component) {
		bindingComponentDeployer.undeploy(component);
	}

	public void undeployServiceAssembly(ServiceAssembly assembly) {
		assemblyDeployer.undeploy(assembly);
	}

	public void deployServiceAssembly(IProject project, ServiceAssembly assembly) {
		try {
			assemblyDeployer.deploy(project, assembly, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
