/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.jbi.deployment.impl;

import org.servicemix.jbi.config.spring.BeanElementProcessor;
import org.servicemix.jbi.config.spring.CompositeElementProcessor;
import org.servicemix.jbi.deployment.ClassPath;
import org.servicemix.jbi.deployment.Connection;
import org.servicemix.jbi.deployment.Connections;
import org.servicemix.jbi.deployment.Consumer;
import org.servicemix.jbi.deployment.Consumes;
import org.servicemix.jbi.deployment.Descriptor;
import org.servicemix.jbi.deployment.Identification;
import org.servicemix.jbi.deployment.Provider;
import org.servicemix.jbi.deployment.Provides;
import org.servicemix.jbi.deployment.ServiceAssembly;
import org.servicemix.jbi.deployment.ServiceUnit;
import org.servicemix.jbi.deployment.Services;
import org.servicemix.jbi.deployment.SharedLibrary;
import org.servicemix.jbi.deployment.SharedLibraryList;
import org.servicemix.jbi.deployment.Target;

/**
 * @version $Revision$
 */
public class JbiNamespaceProcessor extends CompositeElementProcessor {
	public static final String JBI_NAMESPACE = "http://java.sun.com/xml/ns/jbi";

	private BeanElementProcessor sharedListProcessor;

	public JbiNamespaceProcessor() {
		super(JBI_NAMESPACE);
	}

	protected void loadLocalNameToProcessorMap() {

		// TODO we can hopefully code generate this one day using annotations
		// etc?
		registerBeanPropertyProcessor("bootstrap-class-path", ClassPath.class);
		registerBeanPropertyProcessor("component-class-path", ClassPath.class);
		registerProcessor("component", new ComponentElementProcessor(this));
		registerBeanPropertyProcessor("connection", Connection.class);
		registerBeanPropertyProcessor("connections", Connections.class);
		registerBeanPropertyProcessor("consumer", Consumer.class);
		registerBeanPropertyProcessor("consumes", Consumes.class);
		registerBeanPropertyProcessor("identification", Identification.class);
		registerBeanProcessor("jbi", Descriptor.class);
		registerValueAlias("path-element");
		registerBeanPropertyProcessor("provider", Provider.class);
		registerBeanPropertyProcessor("provides", Provides.class);
		registerBeanPropertyProcessor("service-assembly", ServiceAssembly.class);
		registerBeanPropertyProcessor("services", Services.class);
		registerBeanProcessor("service-unit", ServiceUnit.class);
		registerBeanPropertyProcessor("target", Target.class);
		registerBeanPropertyProcessor("shared-library", SharedLibrary.class);
		registerBeanPropertyProcessor("shared-library-class-path", ClassPath.class);
		
		sharedListProcessor = registerBeanProcessor("shared-library-list",
				SharedLibraryList.class, "name");
	}

	public BeanElementProcessor getSharedListProcessor() {
		return sharedListProcessor;
	}
}
