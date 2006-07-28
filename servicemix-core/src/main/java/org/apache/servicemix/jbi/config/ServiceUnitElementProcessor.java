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
package org.apache.servicemix.jbi.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.config.spring.CompositeElementProcessor;
import org.apache.servicemix.jbi.config.spring.ElementProcessor;
import org.apache.servicemix.jbi.config.spring.QNameElementProcessor;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles the 'container' element
 *
 * @version $Revision$
 */
public class ServiceUnitElementProcessor extends QNameElementProcessor implements ElementProcessor {

    private static final transient Log log = LogFactory.getLog(ServiceUnitElementProcessor.class);

    private static final CompositeElementProcessor compositeProcessor = new CompositeElementProcessor("", null) {
        protected void loadLocalNameToProcessorMap() {
            registerProcessor("component", new ComponentElementProcessor());
            registerProcessor("components", new ComponentsElementProcessor());
            registerProcessor("qname", new QNameElementProcessor());
        }
    };

    public static CompositeElementProcessor getCompositeprocessor() {
        return compositeProcessor;
    }

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        // lets add a new bean element
        Document document = element.getOwnerDocument();

        Element root = (Element) element.getParentNode();

        Element bean = document.createElement("bean");
        root.appendChild(bean);
        DOMUtil.copyAttributes(element, bean);
        DOMUtil.moveContent(element, bean);
        root.removeChild(element);

        String id = bean.getAttribute("id");
        if (id == null || id.length() == 0) {
            bean.setAttribute("id", "jbi");
        }

        String className = bean.getAttribute("class");
        if (className == null || className.length() == 0) {
            bean.setAttribute("class", "org.apache.servicemix.jbi.container.SpringServiceUnitContainer");
        }

        processChildren(compositeProcessor, bean, beanDefinitionReader);

        logXmlGenerated(log, "container generated", bean);
    }

}
