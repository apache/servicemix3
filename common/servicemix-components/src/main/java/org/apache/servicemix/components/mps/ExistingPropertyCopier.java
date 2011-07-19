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
package org.apache.servicemix.components.mps;

import javax.jbi.JBIException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * Copy a property value from an existing property (somewhere)
 *
 */
public class ExistingPropertyCopier implements PropertyValue {

	public final static String XML_ELEMENT_NAME = "existing-property";
	
	private String name;

	public ExistingPropertyCopier(String name) {
		this.name = name;
	}
	public String getPropertyValue(NormalizedMessage msg) throws JBIException {
		if (msg.getProperty(name) != null) {
			return msg.getProperty(name).toString();
		} else {
			return null;
		}
		
	}

}
