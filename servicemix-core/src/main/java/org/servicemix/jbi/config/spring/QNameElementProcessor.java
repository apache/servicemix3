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
package org.servicemix.jbi.config.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Handles the 'qname' element
 *
 * @version $Revision$
 */
public class QNameElementProcessor extends ElementProcessorSupport implements ElementProcessor {
    private static final transient Log log = LogFactory.getLog(QNameElementProcessor.class);

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        Element root = (Element) element.getParentNode();
        QName qname = DOMUtil.createQName(element, DOMUtil.getElementText(element));
        Element bean = addQNameBeanElement(root, qname);

        // now lets discard the old qname element to avoid breaking spring
        root.removeChild(element);

        logXmlGenerated(log, "qname generated", bean);
    }

    /**
     * Adds a new Spring bean element to create a QName instance of the given value.
     */
    protected Element addQNameBeanElement(Element owner, QName qname) {
        Element bean = addBeanElement(owner, "javax.xml.namespace.QName");

        String uri = qname.getNamespaceURI();
        boolean hasURI = uri != null && uri.length() > 0;
        if (hasURI) {
            addConstructorValueNode(bean, uri);
        }
        addConstructorValueNode(bean, qname.getLocalPart());
        if (hasURI) {
            String prefix = qname.getPrefix();
            if (prefix != null && prefix.length() > 0) {
                addConstructorValueNode(bean, prefix);
            }
        }
        return bean;
    }

    protected void addQNameProperty(Element registration, String propertyName, String qnameText, Element namespaceContext) {
        if (qnameText != null && qnameText.length() > 0) {
            Element property = addPropertyElement(registration, propertyName);
            QName qname = DOMUtil.createQName(namespaceContext, qnameText);
            addQNameBeanElement(property, qname);
        }
    }

}
