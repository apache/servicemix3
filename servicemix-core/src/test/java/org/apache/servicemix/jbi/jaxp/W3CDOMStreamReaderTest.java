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

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 * @since Oct 26, 2004
 */
public class W3CDOMStreamReaderTest
    extends AbstractStreamReaderTest
{
    
    
    public void testSingleElement() throws Exception
    {
        Document doc = getDocument();
        Element e = doc.createElementNS("urn:test","root");
        e.setAttribute("xmlns", "urn:test");
        doc.appendChild(e);
        
        assertEquals(1, e.getAttributes().getLength());
        System.out.println("start: " + XMLStreamReader.START_ELEMENT);
        System.out.println("attr: " + XMLStreamReader.ATTRIBUTE);
        System.out.println("ns: " + XMLStreamReader.NAMESPACE);
        System.out.println("chars: " + XMLStreamReader.CHARACTERS);
        System.out.println("end: " + XMLStreamReader.END_ELEMENT);
        
        writeXml(doc,System.out);
        W3CDOMStreamReader reader = new W3CDOMStreamReader(doc.getDocumentElement());
        testSingleElement(reader);
    }
    
    private Document getDocument() throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().newDocument();
        return doc;
    }
    
    
    public void testTextChild() throws Exception
    {
        Document doc = getDocument();
        Element e = doc.createElementNS( "urn:test","root");
        e.setAttribute("xmlns", "urn:test");
        doc.appendChild(e);
        Node text = doc.createTextNode("Hello World");
        e.appendChild(text);
        
        writeXml(doc,System.out);
        
        W3CDOMStreamReader reader = new W3CDOMStreamReader(e);
        testTextChild(reader);
    }
    
    
    public void testMixedContent() throws Exception
    {
        Document doc = getDocument();
        Element e = doc.createElementNS( "urn:test","test:root");
        e.setAttribute("xmlns", "urn:test");
        doc.appendChild(e);
        Node text = doc.createTextNode("Hello World");
        e.appendChild(text);
        Element child = doc.createElement("element");
        e.appendChild(child);
        text = doc.createTextNode(" more text");
        e.appendChild(text);
        
        writeXml(doc,System.out);
        
        W3CDOMStreamReader reader = new W3CDOMStreamReader(e);
        testMixedContent(reader);
    }
    
    
    public void testAttributes() throws Exception
    {
        Document doc = getDocument();
        
        Element e = doc.createElementNS("urn:test","root");
        e.setAttribute("xmlns", "urn:test");
        doc.appendChild(e);
        e.setAttribute("att1", "value1");
        
        Attr attr = doc.createAttributeNS("urn:test2","att2");
        attr.setValue("value2");
        attr.setPrefix("p");
        
        e.setAttribute("xmlns:p", "urn:test2");
        
        e.setAttributeNode(attr);
        writeXml(doc,System.out);
        
        W3CDOMStreamReader reader = new W3CDOMStreamReader(doc.getDocumentElement());
        
        testAttributes(reader);
    }
    
    public void testElementChild() throws Exception
    {
        Document doc = getDocument();
        Element e = doc.createElementNS("urn:test","root");
        e.setAttribute("xmlns", "urn:test");
        Element child =  doc.createElementNS("urn:test2","child");
        child.setAttribute("xmlns:a", "urn:test2");
        
        child.setPrefix("a");
        e.appendChild(child);
        doc.appendChild(e);
        writeXml(doc,System.out);
        
        W3CDOMStreamReader reader = new W3CDOMStreamReader(e);
        testElementChild(reader);
    }
    
    protected void writeXml(Document doc, OutputStream out) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        //identity
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(out));
    }
}
