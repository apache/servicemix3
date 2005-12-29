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
package org.apache.servicemix.components.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.messaging.URLEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.activemq.broker.BrokerService;
import org.activemq.xbean.BrokerFactoryBean;
import org.apache.servicemix.components.saaj.SaajBinding;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.servicemix.jbi.util.FileUtil;
import org.apache.xpath.CachedXPathAPI;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

public class HttpSoapAndSaajTest extends TestCase {
    
    private static final int PORT = 7012;

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
        saaj.setSoapEndpoint(new URLEndpoint("http://64.124.140.30/soap")); 
        as.setComponent(saaj);
        as.setService(new QName("saaj"));
        container.activateComponent(as);
        
        as = new ActivationSpec();
        as.setId("xfireBinding");
        as.setComponent(new HttpSoapConnector(null, PORT, true));
        as.setDestinationService(new QName("saaj"));
        container.activateComponent(as);

        URLConnection connection = new URL("http://localhost:" + PORT).openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        OutputStream os = connection.getOutputStream();
        // Post the request file.
        InputStream fis = getClass().getResourceAsStream("soap-request.xml");
        FileUtil.copyInputStream(fis, os);
        // Read the response.
        InputStream is = connection.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copyInputStream(is, baos);
        System.out.println(baos.toString());
        
        // Check xml validity
        Node node = new SourceTransformer().toDOMNode(new StringSource(baos.toString()));
        
        String text = textValueOfXPath(node, "//Result").trim();

        System.out.println("Found price: " + text);

        assertTrue("price text should not be empty", text.length() > 0);
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
