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
package org.apache.servicemix.soap.interceptors.jbi;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.apache.servicemix.soap.api.Message;
import org.apache.servicemix.soap.api.model.Operation;
import org.apache.servicemix.soap.bindings.soap.impl.Wsdl1SoapMessageImpl;
import org.apache.servicemix.soap.bindings.soap.impl.Wsdl1SoapOperationImpl;
import org.apache.servicemix.soap.core.MessageImpl;
import org.apache.servicemix.soap.util.DomUtil;
import org.apache.servicemix.soap.util.stax.StaxUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JbiInWsdl1InterceptorTest extends TestCase {

    public void test() throws Exception {
        Wsdl1SoapOperationImpl wsdlOperation = new Wsdl1SoapOperationImpl();
        Wsdl1SoapMessageImpl wsdlMessage = new Wsdl1SoapMessageImpl();
        wsdlMessage.setName(new QName("urn:test", "message"));
        wsdlOperation.setInput(wsdlMessage);

        String input = "<hello />";
        
        Message message = new MessageImpl();
        message.put(Operation.class, wsdlOperation);
        XMLStreamReader reader = StaxUtil.createReader(new ByteArrayInputStream(input.getBytes()));
        reader.nextTag();
        message.setContent(XMLStreamReader.class, reader);
        
        JbiInWsdl1Interceptor interceptor = new JbiInWsdl1Interceptor(true);
        interceptor.handleMessage(message);
        Source source = message.getContent(Source.class);
        assertNotNull(source);
        Document doc = DomUtil.parse(source);
        Element root = doc.getDocumentElement();
        assertEquals(JbiConstants.WSDL11_WRAPPER_NAMESPACE, root.getNamespaceURI());
        assertEquals(JbiConstants.WSDL11_WRAPPER_MESSAGE_LOCALNAME, root.getLocalName());
        
        DomUtil.getTransformerFactory().newTransformer().transform(new DOMSource(doc), new StreamResult(System.err));
    }
    
}
