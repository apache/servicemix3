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
package org.apache.servicemix.jbi.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.config.spring.ElementProcessor;
import org.apache.servicemix.jbi.config.spring.ElementProcessorSupport;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Element;

/**
 * Handles the 'components' element
 *
 * @version $Revision$
 */
public class ComponentsElementProcessor extends ElementProcessorSupport implements ElementProcessor {
    private static final transient Log log = LogFactory.getLog(ComponentsElementProcessor.class);


    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        Element root = (Element) element.getParentNode();
        Element property = addPropertyElement(root, "activationSpecs");
        Element list = root.getOwnerDocument().createElement("list");
        property.appendChild(list);
        DOMUtil.copyAttributes(element, list);
        DOMUtil.moveContent(element, list);
        root.removeChild(element);

        processChildren(ContainerElementProcessor.getCompositeprocessor(), list, beanDefinitionReader);

        logXmlGenerated(log, "Adding new components", property);
    }
}
