/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.xpath.CachedXPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class SaajMarshalerTest extends TestCase {

	private static final transient Logger logger = LoggerFactory.getLogger(SaajMarshalerTest.class);
	
	public void testAxisToNMS() throws Exception {
		MessageFactory messageFactory = new org.apache.axis.soap.MessageFactoryImpl();
		testToNMS(messageFactory);
	}
	
	public void testAxisCreateSOAPMessage() throws Exception {
		MessageFactory messageFactory = new org.apache.axis.soap.MessageFactoryImpl();
		testCreateSOAPMessage(messageFactory);
	}
	
	/*
	public void testAxis2() throws Exception {
		MessageFactory messageFactory = new org.apache.axis2.saaj.MessageFactoryImpl();
		testFactory(messageFactory);
	}
	*/
	
    /*
	public void testSunToNMS() throws Exception {
		MessageFactory messageFactory = new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl();
		testToNMS(messageFactory);
	}
    */
	
	/*
	public void testSunCreateSOAPMessage() throws Exception {
		MessageFactory messageFactory = new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl();
		testCreateSOAPMessage(messageFactory);
	}
	*/
	
	protected void testToNMS(MessageFactory messageFactory) throws Exception {
		MimeHeaders headers = new MimeHeaders();
		headers.addHeader("Content-Type", "text/xml;");
		InputStream is = getClass().getClassLoader().getResourceAsStream("org/apache/servicemix/components/http/soap-response.xml");
		SOAPMessage sm = messageFactory.createMessage(headers, is);
		NormalizedMessage nm = new NormalizedMessageImpl();
		new SaajMarshaler().toNMS(nm, sm);

        Node node = new SourceTransformer().toDOMNode(new SourceTransformer().toStreamSource(nm.getContent()));
        logger.info(new SourceTransformer().toString(node));
		
        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
        NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, "//*[local-name() = 'userId']");
        Element root = (Element) iterator.nextNode();
        QName qname = DOMUtil.createQName(root, root.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type"));
        assertEquals("http://www.w3.org/2001/XMLSchema", qname.getNamespaceURI());
        assertEquals("string", qname.getLocalPart());
	}
	
	protected void testCreateSOAPMessage(MessageFactory messageFactory) throws Exception { 
        MimeHeaders headers = new MimeHeaders(); 
        headers.addHeader("Content-Type", "text/xml;"); 
         
        InputStream is = getClass().getClassLoader().getResourceAsStream("org/apache/servicemix/components/saaj/xml-request.xml");         
        logger.info("Raw XML: {}", new SourceTransformer().toString(new StreamSource(is)));
         
        is = getClass().getClassLoader().getResourceAsStream("org/apache/servicemix/components/saaj/xml-request.xml");         
        NormalizedMessage nm = new NormalizedMessageImpl(); 
        nm.setContent(new StreamSource(is)); 
           
        SaajMarshaler marshaler = new SaajMarshaler();
        marshaler.setMessageFactory(messageFactory);
        SOAPMessage msg = marshaler.createSOAPMessage(nm);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);
        String soapEnv = new String(baos.toByteArray());
        logger.info("Prepared SOAP: {}", soapEnv);
        Node node2 = new SourceTransformer().toDOMNode(new StringSource(soapEnv)); 
          
        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI(); 
        NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node2, "//*[local-name() = 'userId']"); 
        Element root = (Element) iterator.nextNode(); 
        QName qname = DOMUtil.createQName(root, root.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type")); 
        assertEquals("http://www.w3.org/2001/XMLSchema", qname.getNamespaceURI()); 
        assertEquals("string", qname.getLocalPart()); 
    } 

}
