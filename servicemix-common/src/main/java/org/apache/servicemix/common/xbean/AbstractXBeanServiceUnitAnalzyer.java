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
package org.apache.servicemix.common.xbean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.servicemix.common.Endpoint;
import org.apache.servicemix.common.packaging.Provides;
import org.apache.servicemix.common.packaging.ServiceUnitAnalyzer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public abstract class AbstractXBeanServiceUnitAnalzyer implements
		ServiceUnitAnalyzer {

	List consumes = new ArrayList();

	List provides = new ArrayList();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.packaging.ServiceUnitAnalyzer#getConsumes()
	 */
	public List getConsumes() {
		return consumes;
	}

	protected abstract List getConsumes(Endpoint endpoint);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.packaging.ServiceUnitAnalyzer#getProvides()
	 */
	public List getProvides() {
		return provides;
	}

	protected Provides getProvides(Endpoint endpoint) {
		Provides newProvide = new Provides();
		newProvide.setEndpointName(endpoint.getEndpoint());
		newProvide.setInterfaceName(endpoint.getInterfaceName());
		newProvide.setServiceName(endpoint.getService());
		return newProvide;
	}

	protected abstract String getXBeanFile();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.packaging.ServiceUnitAnalyzer#init(java.io.File)
	 */
	public void init(File explodedServiceUnitRoot) {
		XmlBeanFactory factory = new XmlBeanFactory(new FileSystemResource(
				new File(explodedServiceUnitRoot, getXBeanFile())));

		for (int i = 0; i < factory.getBeanDefinitionNames().length; i++) {
			Object bean = factory.getBean(factory.getBeanDefinitionNames()[i]);
			if (isValidEndpoint(bean)) {
				// The provides are generic while the consumes need to
				// be handled by the implementation
				Endpoint endpoint = (Endpoint) bean;
				provides.add(getProvides(endpoint));
				consumes.addAll(getConsumes(endpoint));
			}
		}
	}

	protected abstract boolean isValidEndpoint(Object bean);

}
