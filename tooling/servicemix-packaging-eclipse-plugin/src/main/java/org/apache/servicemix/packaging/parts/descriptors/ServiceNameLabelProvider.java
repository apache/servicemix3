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

import javax.xml.namespace.QName;

import org.apache.servicemix.packaging.model.AbstractConnectableService;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label Provider for Service Names
 * 
 * @author <a href="mailto:philip.dodds@gmail.com">Philip Dodds </a>
 * 
 */
public class ServiceNameLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getColumnText(Object arg0, int arg1) {
		if (arg0 instanceof AbstractConnectableService)
			return ((AbstractConnectableService) arg0).getServiceName()
					.toString();
		if (arg0 instanceof QName)
			return ((QName) arg0).toString();
		return null;
	}

}
