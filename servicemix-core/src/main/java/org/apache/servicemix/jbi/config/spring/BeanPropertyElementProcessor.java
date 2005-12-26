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

import org.w3c.dom.Element;

/**
 * Maps an element to a property containing a bean definition.
 *
 * @version $Revision$
 */
public class BeanPropertyElementProcessor extends BeanElementProcessor {
    private String propertyName;

    public BeanPropertyElementProcessor(String className, String propertyName) {
        super(className);
        this.propertyName = propertyName;
    }

    public BeanPropertyElementProcessor(Class type, String propertyName) {
        this(type.getName(), propertyName);
    }

    public BeanPropertyElementProcessor(Class type, String propertyName, ElementProcessor processor) {
        super(type.getName(), processor);
        this.propertyName = propertyName;
    }


    protected Element createBeanElement(Element root, Element element, String className) {
        Element property = root.getOwnerDocument().createElement("property");
        String name = propertyName;
        if (name == null) {
            name = getElementNameToPropertyName(element);
        }
        property.setAttribute("name", name);
        root.appendChild(property);
        return super.createBeanElement(property, element, className);
    }
}
