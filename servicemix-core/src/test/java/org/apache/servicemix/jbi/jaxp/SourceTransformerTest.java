/**
 * 
 * Copyright 2005 Protique Ltd
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
package org.apache.servicemix.jbi.jaxp;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xbean.spring.context.impl.QNameHelper;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class SourceTransformerTest extends TestCase {

    private SourceTransformer transformer = new SourceTransformer();
    
    /*
     * Test method for 'org.apache.servicemix.jbi.jaxp.SourceTransformer.toDOMNode(Source)'
     */
    public void testToDOMNodeSource() throws Exception {
        Node node = transformer.toDOMNode(new StringSource(
                "<definition xmlns:tns='http://foo.bar.com'><value>tns:bar</value></definition>"));
        
        assertNotNull(node);
        assertTrue(node instanceof Document);
        Document doc = (Document) node;
        Element e = (Element) doc.getDocumentElement().getFirstChild();
        QName q = QNameHelper.createQName(e, e.getFirstChild().getNodeValue());
        assertEquals("http://foo.bar.com", q.getNamespaceURI());
    }

}
