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
package org.apache.servicemix.packaging.parts.descriptors;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.servicemix.packaging.model.AbstractComponent;
import org.apache.servicemix.packaging.model.AbstractConnectableService;
import org.apache.servicemix.packaging.model.DeploymentDiagram;
import org.apache.servicemix.packaging.model.ServiceAssembly;
import org.apache.servicemix.packaging.model.ServiceUnit;

/**
 * A helper class for handling Service Names in deployment diagrams
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceNameHelper {

	public static List<QName> getServiceNames(DeploymentDiagram diagram) {
		List<QName> serviceNames = new LinkedList<QName>();

		if (diagram != null) {
			for (AbstractComponent child : diagram.getChildren()) {
				if (child instanceof ServiceAssembly) {
					for (ServiceUnit unit : ((ServiceAssembly) child)
							.getServiceUnit()) {
						if (unit.getServiceName() != null)
							serviceNames.add(unit.getServiceName());
					}
				} else if (child instanceof AbstractConnectableService) {
					if (((AbstractConnectableService) child).getServiceName() != null)
						serviceNames.add(((AbstractConnectableService) child)
								.getServiceName());
				}
			}
		}
		return serviceNames;
	}

	public static QName getUniqueServiceName(DeploymentDiagram diagram) {
		List<QName> serviceNames = getServiceNames(diagram);
		QName newName = null;
		int count = 1;
		while (newName == null) {
			QName testName = new QName("http://openuri.org", "newService"
					+ count);
			if (!serviceNames.contains(testName)) {
				newName = testName;
			}
			count++;
		}
		return newName;
	}

	public static AbstractConnectableService getConnectableByQName(
			DeploymentDiagram diagram, QName qname) {
		if (diagram != null) {
			for (AbstractComponent child : diagram.getChildren()) {
				if (child instanceof ServiceAssembly) {
					for (ServiceUnit unit : ((ServiceAssembly) child)
							.getServiceUnit()) {
						if (qname.equals(unit.getServiceName()))
							return unit;
					}
				} else if (child instanceof AbstractConnectableService) {
					if (qname.equals(((AbstractConnectableService) child)
							.getServiceName())) {
						return (AbstractConnectableService) child;
					}
				}
			}
		}
		return null;
	}
}
