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
package org.servicemix.jbi.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.jbi.config.spring.ElementProcessor;
import org.servicemix.jbi.config.spring.QNameElementProcessor;
import org.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the 'component' element
 * 
 * @version $Revision$
 */
public class ComponentElementProcessor extends QNameElementProcessor implements ElementProcessor {

    private static final transient Log log = LogFactory.getLog(ComponentElementProcessor.class);

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        // lets add a new bean element
        Document document = element.getOwnerDocument();

        Element root = (Element) element.getParentNode();
        String id = element.getAttribute("id");
        if (id == null || id.length() == 0) {
            throw new BeanDefinitionStoreException("A <component> must have an id attribute");
        }

        Element registration = addBeanElement(root, "org.servicemix.jbi.container.ActivationSpec");
        addPropertyElement(registration, "id", id);
        Element componentProperty = addPropertyElement(registration, "component");
        Element bean = document.createElement("bean");
        componentProperty.appendChild(bean);

        // lets remove any attributes we need
        String service = element.getAttribute("service");
        if (service != null) {
            element.removeAttribute("service");
            addQNameProperty(registration, "service", service, element);
        }
        String interfaceName = element.getAttribute("interface");
        if (interfaceName != null) {
            element.removeAttribute("interface");
            addQNameProperty(registration, "interfaceName", interfaceName, element);
        }
        String operation = element.getAttribute("operation");
        if (operation != null) {
            element.removeAttribute("operation");
            addQNameProperty(registration, "operation", operation, element);
        }

        String endpoint = element.getAttribute("endpoint");
        if (endpoint != null) {
            element.removeAttribute("endpoint");
            if (endpoint.length() > 0) {
                addPropertyElement(registration, "endpoint", endpoint);
            }
        }
        String destinationEndpoint = element.getAttribute("destinationEndpoint");
        if (destinationEndpoint != null) {
            element.removeAttribute("destinationEndpoint");
            if (destinationEndpoint.length() > 0) {
                addPropertyElement(registration, "destinationEndpoint", destinationEndpoint);
            }
        }

        String destinationService = element.getAttribute("destinationService");
        if (destinationService != null) {
            element.removeAttribute("destinationService");
            addQNameProperty(registration, "destinationService", destinationService, element);
        }
        String destinationInterface = element.getAttribute("destinationInterface");
        if (destinationInterface != null) {
            element.removeAttribute("destinationInterface");
            addQNameProperty(registration, "destinationInterface", destinationInterface, element);
        }
        String destinationOperation = element.getAttribute("destinationOperation");
        if (destinationOperation != null) {
            element.removeAttribute("destinationOperation");
            addQNameProperty(registration, "destinationOperation", destinationOperation, element);
        }
        String failOnNoEndpoint = element.getAttribute("failIfNoDestinationEndpoint");
        if (failOnNoEndpoint != null) {
            element.removeAttribute("failIfNoDestinationEndpoint");
            if (failOnNoEndpoint.length() > 0) {
                addPropertyElement(registration, "failIfNoDestinationEndpoint", failOnNoEndpoint);
            }
        }
        String persistent = element.getAttribute("persistent");
        if (persistent != null) {
        	element.removeAttribute("persistent");
        	if (persistent.length() > 0) {
        		addPropertyElement(registration, "persistent", persistent);
        	}
        }

        // lets move any subscriptions into a new property
        Element list = root.getOwnerDocument().createElement("list");
        boolean hasSubscriptions = false;
        NodeList childNodes = element.getChildNodes();
        for (int index = 0; index < childNodes.getLength() - 1; ) {
            Node node = childNodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getLocalName().equals("subscription")) {
                element.removeChild(node);
                list.appendChild(node);
                hasSubscriptions = true;
            }
            else {
                index++;
            }

        }
        if (hasSubscriptions) {
            Element subscriptions = addPropertyElement(root, "subscriptions");
            subscriptions.appendChild(list);
            registration.appendChild(subscriptions);
        }

        DOMUtil.copyAttributes(element, bean);
        DOMUtil.moveContent(element, bean);
        root.removeChild(element);

        logXmlGenerated(log, "component generated", registration);
    }

}
