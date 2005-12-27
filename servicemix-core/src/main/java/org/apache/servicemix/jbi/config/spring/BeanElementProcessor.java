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
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Maps an element to a top level <bean> element.
 * 
 * @version $Revision$
 */
public class BeanElementProcessor extends ElementProcessorSupport implements ElementProcessor {

	private static final transient Log log = LogFactory.getLog(BeanElementProcessor.class);

	private String className;

	private String textProperty;

	private ElementProcessor childProcessor;

	public BeanElementProcessor(String className) {
		this.className = className;
	}

	public BeanElementProcessor(String className,
			ElementProcessor childProcessor) {
		this.className = className;
		this.childProcessor = childProcessor;
	}

	public BeanElementProcessor(String className, String textProperty) {
		this.className = className;
		this.textProperty = textProperty;
	}

	public BeanElementProcessor(String className, String textProperty,
			ElementProcessor childProcessor) {
		this.className = className;
		this.textProperty = textProperty;
		this.childProcessor = childProcessor;
	}

	public BeanElementProcessor(Class type) {
		this(type.getName());
	}

	public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
		logXmlGenerated(log, "processing Element", element);
		Element root = (Element) element.getParentNode();
		String tmp = element.getAttribute("class");
		if (tmp != null && tmp.length() > 0) {
			element.removeAttribute("class");
		} else {
			tmp = className;
		}
		Element bean = createBeanElement(root, element, tmp);

		turnAttributesIntoProperties(element, bean);
		DOMUtil.moveContent(element, bean);
		if (textProperty != null) {
			turnTextIntoProperty(bean);
		}
		root.removeChild(element);

		processBean(bean, beanDefinitionReader);

		logXmlGenerated(log, "component generated", bean);
	}

	protected void turnTextIntoProperty(Element bean) {
		String text = DOMUtil.getElementText(bean).trim();
		addPropertyElement(bean, textProperty, text);

		// lets remove all the text nodes
		Node node = bean.getFirstChild();
		while (node != null) {
			if (node.getNodeType() == Node.TEXT_NODE) {
				Node textNode = node;
				node = node.getNextSibling();
				bean.removeChild(textNode);
			} else {
				node = node.getNextSibling();
			}
		}
	}

	protected Element createBeanElement(Element root, Element element,
			String className) {
		Element bean = addBeanElement(root, className);
		return bean;
	}

	/**
	 * Provides a post processing hook
	 */
	protected void processBean(Element bean, BeanDefinitionReader beanDefinitionReader) {
		if (childProcessor != null) {
			processChildren(childProcessor, bean, beanDefinitionReader);
		}
	}

	protected void turnAttributesIntoProperties(Element from, Element to) {
		NamedNodeMap attributes = from.getAttributes();
		for (int i = 0, size = attributes.getLength(); i < size; i++) {
			Attr node = (Attr) attributes.item(i);
			// If the attribute is prefixed xmlns: then we we want to ignore
			// it
			if (node.getNodeName().startsWith("xmlns:")) {
				to.setAttribute(node.getNodeName(), node.getNodeValue());
			} else {
				// If the attribute is service-name or interface-name then we
				// need
				// to turn the property into a QName
				if (node.getNodeName().equals("service-name")
						|| node.getNodeName().equals("interface-name")) {
					Element propertyElement = addPropertyElement(to, node
							.getName(), null);
					QNameElementProcessor qnameElementProcessor = new QNameElementProcessor();
					qnameElementProcessor.addQNameBeanElement(propertyElement,
							DOMUtil.createQName(from, node.getNodeValue()));
				} else
					addPropertyElement(to, node.getName(), node.getValue());
			}
		}
	}
}
