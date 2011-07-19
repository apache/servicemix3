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
package org.apache.servicemix.components.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.messaging.URLEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.servicemix.components.saaj.SaajBinding;
import org.apache.servicemix.components.util.MockServiceComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.CachedXPathAPI;
import org.springframework.core.io.ClassPathResource;

public class HttpSoapAndSaajTest extends TestCase {
    
    private static final int PORT_1 = 7012;
    private static final int PORT_2 = 7012;
    private static final int PORT_WS_1 = 7014;
    private static final int PORT_WS_2 = 7014;

    private static transient Logger logger = LoggerFactory.getLogger(HttpSoapAndSaajTest.class);

    protected JBIContainer container;
	protected BrokerService broker;
    
    protected void setUp() throws Exception {
        BrokerFactoryBean bfb = new BrokerFactoryBean(new ClassPathResource("broker.xml"));
        bfb.afterPropertiesSet();
        broker = bfb.getBroker();
        broker.start();
        container = new JBIContainer();
        container.setMonitorInstallationDirectory(false);
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.setEmbedded(true);
        container.setFlowName("jms?jmsURL=tcp://localhost:61626");
        container.init();
        container.start();
    }
    
    protected void tearDown() throws Exception {
        if (container != null) {
            container.shutDown();
        }
        if (broker != null) {
        	broker.stop();
        }
    }
    
    public void testInOut() throws Exception {
        ActivationSpec as = new ActivationSpec();
        as.setId("saaj");
        SaajBinding saaj = new SaajBinding();
        saaj.setSoapEndpoint(new URLEndpoint("http://localhost:" + PORT_WS_1)); 
        as.setComponent(saaj);
        as.setService(new QName("saaj"));
        container.activateComponent(as);
        
        as = new ActivationSpec();
        as.setId("xfireBinding");
        as.setComponent(new HttpSoapConnector(null, PORT_2, true));
        as.setDestinationService(new QName("saaj"));
        container.activateComponent(as);
        
        as = new ActivationSpec();
        as.setId("webservice");
        as.setComponent(new HttpConnector(null, PORT_WS_2));
        as.setDestinationService(new QName("mock"));
        container.activateComponent(as);
        
        as = new ActivationSpec();
        as.setId("mock");
        MockServiceComponent mock = new MockServiceComponent();
        mock.setResponseResource(new ClassPathResource("soap-response.xml", getClass()));
        as.setComponent(mock);
        as.setService(new QName("mock"));
        container.activateComponent(as);

        URLConnection connection = new URL("http://localhost:" + PORT_1).openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        ((HttpURLConnection) connection).setRequestProperty("Content-Type", "text/xml; charset=utf-8;");
        OutputStream os = connection.getOutputStream();
        // Post the request file.
        InputStream fis = getClass().getResourceAsStream("soap-request.xml");
        FileUtil.copyInputStream(fis, os);
        // Read the response.
        InputStream is = connection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copyInputStream(is, baos);
        
        // Check xml validity
        Node node = new SourceTransformer().toDOMNode(new StringSource(baos.toString()));

        OutputFormat format = new OutputFormat((Document) node);
        format.setLineWidth(65);
        format.setIndenting(true);
        format.setIndent(2);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLSerializer serializer = new XMLSerializer(output, format);
        serializer.serialize((Document) node);
        logger.info(output.toString());
        
        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
        NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, "//*[local-name() = 'userId']");
        Element root = (Element) iterator.nextNode();
        QName qname = DOMUtil.createQName(root, root.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type"));
        assertEquals("http://www.w3.org/2001/XMLSchema", qname.getNamespaceURI());
        assertEquals("string", qname.getLocalPart());
        
    }

    protected String textValueOfXPath(Node node, String xpath) throws TransformerException {
        CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
        NodeIterator iterator = cachedXPathAPI.selectNodeIterator(node, xpath);
        Node root = iterator.nextNode();
        if (root instanceof Element) {
            Element element = (Element) root;
            if (element == null) {
                return "";
            }
            String text = DOMUtil.getElementText(element);
            return text;
        }
        else {
            return root.getNodeValue();
        }
    }

}
