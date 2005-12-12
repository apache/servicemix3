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

import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * A useful base class for mapping a number of processors to names in a given namespace.
 *
 * @version $Revision$
 */
public abstract class CompositeElementProcessor implements ElementProcessor {
    private String namespaceURI;
    private Map localNameToProcessor;
    private ElementProcessor defaultProcessor;

    public CompositeElementProcessor() {
        this("");
    }

    public CompositeElementProcessor(String namespaceURI) {
        this(namespaceURI, new ElementToPropertyProcessor());
    }

    protected CompositeElementProcessor(String namespaceURI, ElementProcessor defaultProcessor) {
        this.namespaceURI = namespaceURI;
        this.defaultProcessor = defaultProcessor;
        /*
        if (defaultProcessor == null && namespaceURI != null && namespaceURI.length() > 0) {
            defaultProcessor = new ElementToPropertyProcessor();
        }
        */
        localNameToProcessor = new HashMap();
        loadLocalNameToProcessorMap();
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public void processElement(Element element, BeanDefinitionReader beanDefinitionReader) {
        String uri = element.getNamespaceURI();
        if (sameNamespace(uri, namespaceURI)) {
            String localName = element.getLocalName();
            ElementProcessor processor = (ElementProcessor) localNameToProcessor.get(localName);
            if (processor == null) {
                processor = defaultProcessor;
            }
            if (processor != null) {
                processor.processElement(element, beanDefinitionReader);
            }
        }
    }

    protected abstract void loadLocalNameToProcessorMap();

    protected boolean sameNamespace(String uri, String uri2) {
        return uri == uri2 || (uri != null && uri.equals(uri2)) || uri == null && uri2.length() == 0;
    }

    protected void registerProcessor(String localName, ElementProcessor processor) {
        localNameToProcessor.put(localName, processor);
    }

    protected ElementToPropertyProcessor registerPropertyAlias(String elementName, String propertyName) {
        ElementToPropertyProcessor processor = new ElementToPropertyProcessor(propertyName);
        registerProcessor(elementName, processor);
        return processor;
    }

    protected ElementToValueProcessor registerValueAlias(String elementName) {
        ElementToValueProcessor processor = new ElementToValueProcessor();
        registerProcessor(elementName, processor);
        return processor;
    }

    protected BeanElementProcessor registerBeanProcessor(String elementName, Class beanType) {
        return registerBeanProcessor(elementName, beanType, null);
    }

    protected BeanElementProcessor registerBeanProcessor(String elementName, Class beanType, String textPropertyName) {
        BeanElementProcessor processor = new BeanElementProcessor(beanType.getName(), textPropertyName, this);
        registerProcessor(elementName, processor);
        return processor;
    }

    protected BeanPropertyElementProcessor registerBeanPropertyProcessor(String elementName, Class type, String propertyName) {
        BeanPropertyElementProcessor processor = new BeanPropertyElementProcessor(type, propertyName, this);
        registerProcessor(elementName, processor);
        return processor;
    }

    protected BeanPropertyElementProcessor registerBeanPropertyProcessor(String elementName, Class type) {
        return registerBeanPropertyProcessor(elementName, type, null);
    }


}
