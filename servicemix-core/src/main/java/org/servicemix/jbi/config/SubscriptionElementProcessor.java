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
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Element;

/**
 * Handles the 'subscription' element
 * 
 * @version $Revision$
 */
public class SubscriptionElementProcessor extends QNameElementProcessor implements ElementProcessor {
    private static final Log log = LogFactory.getLog(SubscriptionElementProcessor.class);

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        // lets add a new bean element
        Element root = (Element) element.getParentNode();
        Element registration = addBeanElement(root, "org.servicemix.jbi.container.SubscriptionSpec");

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

        DOMUtil.copyAttributes(element, registration);
        DOMUtil.moveContent(element, registration);
        root.removeChild(element);

        logXmlGenerated(log, "subscription generated", registration);
    }
}
