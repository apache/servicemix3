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
import java.util.List;

import javax.jbi.JBIException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.jbi.util.DOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class to hold the list of Property Values
 */
public class PropertyValueResolver {

	private final transient Logger logger = LoggerFactory.getLogger(getClass());

	public static final String XML_ELEMENT_NAME = "property";

	/**
	 * The name of the JBI property that this class will set.
	 */
	private String name;

	/**
	 * if the set contains a static-string propertyvalue at the top of the
	 * config, we don't need to do anything so the value is stored "here". Null
	 * if the list has to be evald
	 */
	private String staticValue = null;

	/**
	 * Our list of PropertySetTypes
	 */
	private List propertySetTypes = new ArrayList();

	/**
	 * Construct this PVR, the Element is the .. //property-set/property element
	 */
	public PropertyValueResolver(String propertyName, Element self)
			throws ConfigNotSupportedException {
		this.name = propertyName;
		addPropertySetTypes(self);
	}

	/**
	 * Set the property (this.name) on the out message based on any properties
	 * on the message
	 */
	public void setProperty(NormalizedMessage in, NormalizedMessage out)
			throws JBIException {

		if (this.staticValue != null) {
			out.setProperty(name, staticValue);
		} else {
			String value = resolveValue(in);
			if (value != null) {
				out.setProperty(name, value);
			} else {
				logger.warn("Property " + name
						+ " was not set as the value was unresolved");
			}
		}
	}

	/**
	 * Get the value out of the in, and put it in the out.
	 */
	private String resolveValue(NormalizedMessage message) throws JBIException {
		// go through the list
		// if a value is found on the one, return it, until the list is
		// exhausted
		String propValue = null;
		logger.debug("propvrsize=" + propertySetTypes.size());
		for (int i = 0; i < propertySetTypes.size(); i++) {
			PropertyValue pv = (PropertyValue) propertySetTypes.get(i);
			propValue = pv.getPropertyValue(message);
			logger.debug("value from" + pv.getClass() + " = " + propValue);
			if (propValue != null && !"".equals(propValue)) {
				break;
			}
			if (logger.isDebugEnabled()) {
				logger.debug(this.name + ": " + pv.getClass() + " was empty");
			}
		}
		return propValue;
	}

	/**
	 * Given the XML below, we will locate the different propertyValueTypes and
	 * set them on us.
	 * 
	 * <property name="some.property.name"> <existing-property/>
	 * <existing-property name="someproperty"/> <xpath-expression>
	 * <![CDATA[/someexpath/statement]]> </xpath-expression> <static-value><![CDATA[a
	 * value in the raw]]></static-value> </property>
	 */
	private void addPropertySetTypes(Element propertyElement)
			throws ConfigNotSupportedException {

		NodeList propertyValueNodes = propertyElement.getChildNodes();
		// iterate of all the propertyValue nodes ..
		// (same as equiv to select='//property[@name='x']/*'
		for (int i = 0; i < propertyValueNodes.getLength(); i++) {
			if (propertyValueNodes.item(i).getNodeType() != Element.ELEMENT_NODE) {
				continue;
			}
			Element pvElem = (Element) propertyValueNodes.item(i);
			PropertyValue pv;
			if (pvElem.getNodeName().equals(
					StaticStringPropertyValue.XML_ELEMENT_NAME)) {
				if (this.propertySetTypes.size() == 0) {
					this.staticValue = DOMUtil.getElementText(pvElem);
				}
				pv = new StaticStringPropertyValue(DOMUtil
						.getElementText(pvElem));
			} else if (pvElem.getNodeName().equals(
					XPathContentMessagePropertyValue.XML_ELEMENT_NAME)) {
				String xpath = DOMUtil.getElementText(pvElem);
				pv = new XPathContentMessagePropertyValue(xpath);
				if (logger.isDebugEnabled()) {
					logger.debug("Created an XPath VR :" + xpath);
				}
			} else if (pvElem.getNodeName().equals(
					ExistingPropertyCopier.XML_ELEMENT_NAME)) {
				// default to this parents name (so it acts like a property
				// copy)

				String propertyName = this.name;
				if (pvElem.getAttribute("name") != null
						&& !"".equals(pvElem.getAttribute("name"))) {
					// if there was a <existing-property name="somename"/>
					// then use that name as the source JBI property
					// in this mode it acts like a cp src dest and not a dupe.
					propertyName = pvElem.getAttribute("name");
				}
				pv = new ExistingPropertyCopier(propertyName);
			} else {
				throw new ConfigNotSupportedException("Property value type "
						+ pvElem.getNodeName()
						+ " is not supported for the MessagePropertySetter");
			}
			this.propertySetTypes.add(pv);

		}
	}

}
