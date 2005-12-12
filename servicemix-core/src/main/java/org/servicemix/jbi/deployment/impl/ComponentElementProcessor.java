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

import org.servicemix.jbi.config.spring.BeanPropertyElementProcessor;
import org.servicemix.jbi.deployment.Component;
import org.servicemix.jbi.deployment.InstallationDescriptorExtension;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the 'component' element.
 *
 * @version $Revision$
 */
public class ComponentElementProcessor extends BeanPropertyElementProcessor {

    public static final String JBI_NAMESPACE = "";

    private JbiNamespaceProcessor jbiProcessor;

    public ComponentElementProcessor(JbiNamespaceProcessor childProcessor) {
        super(Component.class, null, childProcessor);
        this.jbiProcessor = childProcessor;
    }

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        // lets rename any shared-library elements
        Element property = addPropertyElement(element, "sharedLibraries");
        Element list = addElement(property, "list");

        DocumentFragment fragment = element.getOwnerDocument().createDocumentFragment();

        Node current = element.getFirstChild();
        while (current != null) {
            Node node = current;
            current = current.getNextSibling();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String uri = node.getNamespaceURI();
                if (!JbiNamespaceProcessor.JBI_NAMESPACE.equals(uri) && node != property) {
                    element.removeChild(node);
                    fragment.appendChild(node);
                }
                else if ("shared-library".equals(node.getNodeName())) {
                    Element child = (Element) node;
                    element.removeChild(child);
                    list.appendChild(child);
                    jbiProcessor.getSharedListProcessor().processElement(child, beanDefinitionReader);
                }
            }
        }

        Element descriptorProperty = addPropertyElement(element, "descriptorExtension");
        descriptorProperty.setAttribute("ref", "installationDescriptorExtension");

        // lets find all elements which are not in the JBI namespace
        Map propertiesMap = new HashMap();
        propertiesMap.put("descriptorExtension", fragment);
        RootBeanDefinition definition = new RootBeanDefinition(InstallationDescriptorExtension.class, new MutablePropertyValues(propertiesMap));
        beanDefinitionReader.getBeanFactory().registerBeanDefinition("installationDescriptorExtension", definition);

        super.processElement(element, beanDefinitionReader);
    }
}
