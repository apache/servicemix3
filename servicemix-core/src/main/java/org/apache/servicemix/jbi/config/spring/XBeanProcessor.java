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
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xbean.spring.context.SpringApplicationContext;
import org.xbean.spring.context.SpringXmlPreprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class XBeanProcessor implements SpringXmlPreprocessor {

    public static final String BEAN_NAME_DELIMITERS = ",; ";

    /**
     * Value of a T/F attribute that represents true.
     * Anything else represents false. Case seNsItive.
     */
    public static final String TRUE_VALUE = "true";
    public static final String DEFAULT_VALUE = "default";
    public static final String DESCRIPTION_ELEMENT = "description";

    public static final String AUTOWIRE_BY_NAME_VALUE = "byName";
    public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";
    public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";
    public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

    public static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";
    public static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";
    public static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

    public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";
    public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
    public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

    public static final String IMPORT_ELEMENT = "import";
    public static final String RESOURCE_ATTRIBUTE = "resource";

    public static final String ALIAS_ELEMENT = "alias";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String ALIAS_ATTRIBUTE = "alias";

    public static final String BEAN_ELEMENT = "bean";
    public static final String ID_ATTRIBUTE = "id";
    public static final String PARENT_ATTRIBUTE = "parent";

    public static final String CLASS_ATTRIBUTE = "class";
    public static final String ABSTRACT_ATTRIBUTE = "abstract";
    public static final String SINGLETON_ATTRIBUTE = "singleton";
    public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";
    public static final String AUTOWIRE_ATTRIBUTE = "autowire";
    public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";
    public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";
    public static final String INIT_METHOD_ATTRIBUTE = "init-method";
    public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";
    public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";
    public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

    public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
    public static final String INDEX_ATTRIBUTE = "index";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String PROPERTY_ELEMENT = "property";
    public static final String REF_ATTRIBUTE = "ref";
    public static final String VALUE_ATTRIBUTE = "value";
    public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

    public static final String REPLACED_METHOD_ELEMENT = "replaced-method";
    public static final String REPLACER_ATTRIBUTE = "replacer";
    public static final String ARG_TYPE_ELEMENT = "arg-type";
    public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

    public static final String REF_ELEMENT = "ref";
    public static final String IDREF_ELEMENT = "idref";
    public static final String BEAN_REF_ATTRIBUTE = "bean";
    public static final String LOCAL_REF_ATTRIBUTE = "local";
    public static final String PARENT_REF_ATTRIBUTE = "parent";

    public static final String VALUE_ELEMENT = "value";
    public static final String NULL_ELEMENT = "null";
    public static final String LIST_ELEMENT = "list";
    public static final String SET_ELEMENT = "set";
    public static final String MAP_ELEMENT = "map";
    public static final String ENTRY_ELEMENT = "entry";
    public static final String KEY_ELEMENT = "key";
    public static final String KEY_ATTRIBUTE = "key";
    public static final String KEY_REF_ATTRIBUTE = "key-ref";
    public static final String VALUE_REF_ATTRIBUTE = "value-ref";
    public static final String PROPS_ELEMENT = "props";
    public static final String PROP_ELEMENT = "prop";

    /**
     * All the reserved Spring XML element names which cannot be overloaded by an XML extension
     */
    protected static final String[] RESERVED_ELEMENT_NAMES = { "beans", DESCRIPTION_ELEMENT, IMPORT_ELEMENT, ALIAS_ELEMENT,
                                                               BEAN_ELEMENT,
                                                               CONSTRUCTOR_ARG_ELEMENT, PROPERTY_ELEMENT,
                                                               LOOKUP_METHOD_ELEMENT, REPLACED_METHOD_ELEMENT,
                                                               ARG_TYPE_ELEMENT, REF_ELEMENT, IDREF_ELEMENT,
                                                               VALUE_ELEMENT, NULL_ELEMENT, LIST_ELEMENT,
                                                               SET_ELEMENT, MAP_ELEMENT, ENTRY_ELEMENT, KEY_ELEMENT,
                                                               PROPS_ELEMENT, PROP_ELEMENT };

    protected final Log logger = LogFactory.getLog(getClass());
    
    private Set reservedElementNames = new HashSet(Arrays.asList(RESERVED_ELEMENT_NAMES));
    
    public void preprocess(SpringApplicationContext applicationContext, XmlBeanDefinitionReader reader, Document document) {
        preprocessXml(reader, document.getDocumentElement());
    }

    public void preprocessXml(XmlBeanDefinitionReader reader, Element root) {
        String localName = root.getNodeName();
        String uri = root.getNamespaceURI();     
        boolean extensible = true;   
        if (uri == null || uri.length() == 0) {      
            if (reservedElementNames.contains(localName)) {      
                extensible = false;      
            }    
        }    
        if (extensible) {    
            // lets see if we have a custom XML processor    
            ElementProcessor handler = findElementProcessor(uri, localName);     
            if (handler != null) {   
                handler.processElement(root, reader);      
            }    
        }    
     
        // lets recurse into any children    
        NodeList nl = root.getChildNodes();      
        for (int i = 0; i < nl.getLength(); i++) {   
            Node node = nl.item(i);      
            if (node instanceof Element) {   
                Element element = (Element) node;    
                preprocessXml(reader, element);    
            }    
        }        
    }

    /**
     * Uses META-INF/services discovery to find an {@link ElementProcessor} for the given
     * namespace and localNam
     *
     * @param namespaceURI the namespace URI of the element
     * @param localName the local name of the element    
     * @return the custom processor for the given element name if it could be found, otherwise return null   
     */      
    protected ElementProcessor findElementProcessor(String namespaceURI, String localName) throws BeanDefinitionStoreException  {    
        String uri = "META-INF/services/org/springframework/config/" + createDiscoveryPathName(namespaceURI, localName);     
     
        // lets try the thread context class loader first    
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(uri);    
        if (in == null) {    
            in = getClass().getClassLoader().getResourceAsStream(uri);   
            if (in == null) {    
                logger.warn("Could not find resource: " + uri);      
                return null;     
            }    
        }    
     
        // lets load the file    
        BufferedReader reader = null;    
        try {    
            reader = new BufferedReader(new InputStreamReader(in));      
            String line = reader.readLine();     
            if (line == null) {      
                throw new BeanDefinitionStoreException("Empty file found for: " + uri);      
            }    
            line = line.trim();      
            Class answer = null;     
            try {    
                answer = loadClass(line);    
            }    
            catch (ClassNotFoundException e) {   
                throw new BeanDefinitionStoreException("Could not find class: " + line, e);      
            }    
            try {    
                return (ElementProcessor) answer.newInstance();      
            }    
            catch (Exception e) {    
                throw new BeanDefinitionStoreException("Failed to instantiate bean of type: " + answer.getName() + ". Reason: " + e, e);     
            }    
        }    
        catch (IOException e) {      
            throw new BeanDefinitionStoreException("Failed to load file for URI: " + uri + ". Reason: " + e, e);     
        }    
        finally {    
            try {    
                reader.close();      
            }    
            catch (Exception e) {    
                // ignore    
            }    
        }    
    }

    /**
     * Converts the namespace and localName into a valid path name we can use on the classpath to discover a text file
     */
    protected String createDiscoveryPathName(String uri, String localName) {
        if (uri == null || uri.length() == 0) {
            return localName;
        }
        // TODO proper encoding required
        // lets replace any dodgy characters
        return uri.replaceAll("://", "/").replace(':', '/').replace(' ', '_') + "/" + localName;
    }

    /**
     * Attempts to load the class on the current thread context class loader or the class loader which loaded us
     */
    protected Class loadClass(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e) {
            return getClass().getClassLoader().loadClass(name);
        }
    }

}
