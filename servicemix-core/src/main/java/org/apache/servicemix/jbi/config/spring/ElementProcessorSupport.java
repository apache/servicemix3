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
package org.apache.servicemix.jbi.config.spring;

import org.apache.commons.logging.Log;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;

/**
 * A useful base class for transforming the configuration elements into regular
 * Spring XML elements
 * 
 * @version $Revision$
 */
public class ElementProcessorSupport {
	public static final String NAMESPACE = "";

	/**
	 * Recursively process all the child elements with the given processor
	 */
	protected void processChildren(ElementProcessor processor, Element element, BeanDefinitionReader beanDefinitionReader) {
		Node current = element.getFirstChild();
		while (current != null) {
			Node node = current;
			current = current.getNextSibling();
			if (node instanceof Element) {
				Element child = (Element) node;
				processor.processElement(child, beanDefinitionReader);
				processChildren(processor, child, beanDefinitionReader);
			}
		}
	}

	// Helper methods to add new Spring XML elements

	/**
	 * Adds a new Spring element of the given name to the owner
	 */
	protected Element addElement(Node owner, String name) {
		Element property = owner.getOwnerDocument().createElementNS(NAMESPACE,
				name);
		owner.appendChild(property);
		return property;
	}

	/**
	 * Creates and adds a new bean node on the given owner element
	 */
	protected Element addBeanElement(Node owner, String className) {
		Element bean = addElement(owner, "bean");
		bean.setAttribute("class", className);
		return bean;
	}

	/**
	 * Creates and adds a new property node on the given bean element
	 */
	protected Element addPropertyElement(Node bean, String propertyName) {
		Element property = addElement(bean, "property");
		property.setAttribute("name", convertToCamelCase(propertyName));
		return property;
	}

	/**
	 * Adds a new property element to the given bean element with the name and
	 * value
	 */
	protected Element addPropertyElement(Node bean, String propertyName,
			String value) {
		Element property = addPropertyElement(bean, propertyName);
		if (value != null)
			property.setAttribute("value", value);
		return property;
	}

	/**
	 * Adds a new constructor argument element to the given bean
	 */
	protected void addConstructorValueNode(Node bean, String value) {
		Element constructorArg = addElement(bean, "constructor-arg");
		constructorArg.setAttribute("value", value);
		bean.appendChild(constructorArg);
	}

	// DOM utilities

	protected void logXmlGenerated(Log log, String message, Node node) {
		if (log.isDebugEnabled()) {
			try {
				String xml = DOMUtil.asXML(node);
				log.debug(message + ": " + xml);
			} catch (TransformerException e) {
				log.warn("Could not transform generated XML into text: " + e, e);
			}
		}
	}

	protected String getElementNameToPropertyName(Element element) {
		String name = element.getNodeName();
		// lets turn dashes into camel case
		return convertToCamelCase(name);
	}

	protected String convertToCamelCase(String name) {
		while (true) {
			int idx = name.indexOf('-');
			if (idx >= 0) {
				String prefix = name.substring(0, idx);
				String cap = name.substring(idx + 1, idx + 2);
				String rest = name.substring(idx + 2);
				name = prefix + cap.toUpperCase() + rest;
			} else {
				break;
			}
		}
		return name;
	}
}
