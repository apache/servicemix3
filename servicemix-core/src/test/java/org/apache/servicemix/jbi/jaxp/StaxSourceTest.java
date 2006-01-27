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
package org.apache.servicemix.jbi.jaxp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.StaxSource;
import org.w3c.dom.Document;

import junit.framework.TestCase;

public class StaxSourceTest extends TestCase {

    private static final Log log = LogFactory.getLog(StaxSourceTest.class);

    public void testStaxSourceOnStream() throws Exception {
        InputStream is = getClass().getResourceAsStream("test.xml");
        XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);
        StaxSource ss = new StaxSource(xsr);
        StringWriter buffer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(ss, new StreamResult(buffer));
        log.info(buffer.toString());
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(buffer.toString().getBytes()));
        StringWriter buffer2 = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(buffer2));
        log.info(buffer2.toString());
    }

    public void testStaxSourceOnDOM() throws Exception {
        InputStream is = getClass().getResourceAsStream("test.xml");
        XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);
        StaxSource ss = new StaxSource(xsr);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMResult result = new DOMResult();
        transformer.transform(ss, result);
        assertNotNull(result.getNode());
    }

    public void testStaxToDOM() throws Exception {
        InputStream is = getClass().getResourceAsStream("test.xml");
        XMLStreamReader xsr = XMLInputFactory.newInstance().createXMLStreamReader(is);
        StaxSource ss = new StaxSource(xsr);
        DOMSource src = new SourceTransformer().toDOMSource(ss);
        assertNotNull(src);
        assertNotNull(src.getNode());
    }

}
