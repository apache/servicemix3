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
package org.apache.servicemix.jbi.config.spring;

import org.apache.servicemix.jbi.util.DOMUtil;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Converts an XML element into a property declaration. The element name is used as the property
 * name unless it is overloaded.
 *
 * @version $Revision$
 */
public class ElementToPropertyProcessor extends ElementProcessorSupport implements ElementProcessor {
    private String propertyName;

    public ElementToPropertyProcessor() {
    }

    public ElementToPropertyProcessor(String propertyName) {
        this.propertyName = propertyName;
    }

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        Element bean = (Element) element.getParentNode();
        bean.removeChild(element);
        
        String name = propertyName;
        if (name == null) {
            name = getElementNameToPropertyName(element);
        }

        addPropertyElement(bean, name, DOMUtil.getElementText(element));

        processAttributes(element, bean);

    }

    /**
     * Lets add any other properties specified as attributes on this element
     */
    protected void processAttributes(Element element, Element bean) {
        NamedNodeMap attributes = element.getAttributes();
        for (int i = 0, size = attributes.getLength(); i < size; i++ ) {
            Attr node = (Attr) attributes.item(i);
            String value = node.getValue();
            if (value != null && value.length() > 0) {
            addPropertyElement(bean, node.getName(), value);
            }
        }
    }

}
