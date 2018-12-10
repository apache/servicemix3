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
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.xml.DefaultXmlBeanDefinitionParser;
import org.w3c.dom.Element;

import java.io.IOException;

/**
 * A debug version of the XML definition reader which dumps the XML after its produced.
 *
 * @version $Revision$
 */
public class DebugXmlBeanDefinitionParser extends DefaultXmlBeanDefinitionParser {
    private static final transient Log log = LogFactory.getLog(DebugXmlBeanDefinitionParser.class);

    protected int parseBeanDefinitions(Element element) throws BeanDefinitionStoreException {
        dumpXml(element);
        return super.parseBeanDefinitions(element);
    }

    protected void dumpXml(Element node) {
        try {
            OutputFormat format = new OutputFormat(node.getOwnerDocument(), "UTF-8", true);
            XMLSerializer serializer = new XMLSerializer(System.out, format);
            serializer.serialize(node);
        }
        catch (IOException e) {
            log.error("Failed to dump the XML node: " + e, e);
        }

    }
}
