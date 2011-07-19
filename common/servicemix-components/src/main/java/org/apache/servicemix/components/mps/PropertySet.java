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

import java.util.ArrayList;
import java.util.Iterator;

import javax.jbi.JBIException;
import javax.jbi.messaging.NormalizedMessage;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PropertySet {

	public final static String XML_ELEMENT_NAME = "property-set";
	
	public final static String XML_ATTR_NAME_NAME = "name";
	
	/**
	 * Our name
	 */
	private String propertySetName;

	/**
	 * Our list of property value resolvers
	 */
	private ArrayList pvrs = new ArrayList();
	
	/**
	 * Create a propertySet with a particular name
	 * Element self is //property-set
	 * 
	 * @param name
	 */
	public PropertySet(String name, Element self) throws ConfigNotSupportedException{
		this.propertySetName = name;
		NodeList properties = self.getElementsByTagName(PropertyValueResolver.XML_ELEMENT_NAME);
		for (int i=0; i < properties.getLength();i++) {
			Element property = (Element)properties.item(i);
			this.pvrs.add((new PropertyValueResolver(property.getAttribute(XML_ATTR_NAME_NAME),property)));
		}
	}
	
	/**
	 * Apply all the property values for this in/out pair
	 * 
	 * @param in
	 * @param out
	 */
	public void applyProperties(NormalizedMessage in, NormalizedMessage out) throws JBIException {
		
		// for every property, set the value on the message
		for (Iterator iter = pvrs.iterator(); iter.hasNext();) {
			((PropertyValueResolver)iter.next()).setProperty(in,out);
		}
	}

	public String getName() {
		return propertySetName;
	}

}
