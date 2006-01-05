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
package org.apache.servicemix.components.saaj;

import java.io.InputStream;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import junit.framework.TestCase;

public class SaajMarshalerTest extends TestCase {


	public void testAxis() throws Exception {
		MessageFactory messageFactory = new org.apache.axis.soap.MessageFactoryImpl();
		testFactory(messageFactory);
	}
	
	/*
	public void testAxis2() throws Exception {
		MessageFactory messageFactory = new org.apache.axis2.saaj.MessageFactoryImpl();
		testFactory(messageFactory);
	}
	*/
	
	public void testSun() throws Exception {
		MessageFactory messageFactory = new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl();
		testFactory(messageFactory);
	}
	
	protected void testFactory(MessageFactory messageFactory) throws Exception {
		MimeHeaders headers = new MimeHeaders();
		headers.addHeader("Content-Type", "text/xml;");
		InputStream is = getClass().getClassLoader().getResourceAsStream("org/apache/servicemix/components/http/soap-response.xml");
		SOAPMessage sm = messageFactory.createMessage(headers, is);
		NormalizedMessage nm = new NormalizedMessageImpl();
		new SaajMarshaler().toNMS(nm, sm);

        Node node = new SourceTransformer().toDOMNode(new SourceTransformer().toStreamSource(nm.getContent()));
        System.out.println(new SourceTransformer().toString(node));
		
        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
        NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, "//*[local-name() = 'userId']");
        Element root = (Element) iterator.nextNode();
        QName qname = DOMUtil.createQName(root, root.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type"));
        assertEquals("http://www.w3.org/2001/XMLSchema", qname.getNamespaceURI());
        assertEquals("string", qname.getLocalPart());
	}
	
}
