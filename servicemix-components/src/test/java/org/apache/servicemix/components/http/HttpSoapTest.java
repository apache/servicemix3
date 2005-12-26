/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.apache.servicemix.components.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.servicemix.components.http.HttpSoapClientMarshaler;
import org.apache.servicemix.components.http.HttpSoapConnector;
import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.components.util.TraceComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.InOnlyImpl;
import org.apache.servicemix.jbi.util.FileUtil;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;

public class HttpSoapTest extends TestCase {
    
    private static final int PORT = 7012;

    protected JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setMonitorInstallationDirectory(false);
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.init();
        container.start();
    }
    
    protected void tearDown() throws Exception {
        if (container != null) {
            container.shutDown();
        }
    }
    
    public void testInOut() throws Exception {
        ActivationSpec as = new ActivationSpec();
        as.setId("echo");
        as.setComponent(new EchoComponent());
        as.setService(new QName("echo"));
        container.activateComponent(as);
        as = new ActivationSpec();
        as.setId("xfireBinding");
        as.setComponent(new HttpSoapConnector(null, PORT, true));
        as.setDestinationService(new QName("echo"));
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
    }

    public void testInOnly() throws Exception {
        ActivationSpec as = new ActivationSpec();
        as.setId("trace");
        as.setComponent(new TraceComponent());
        as.setService(new QName("trace"));
        container.activateComponent(as);
        as = new ActivationSpec();
        as.setId("xfireBinding");
        as.setComponent(new HttpSoapConnector(null, PORT, false));
        as.setDestinationService(new QName("trace"));
        container.activateComponent(as);

        PostMethodWebRequest req = new PostMethodWebRequest(
                "http://localhost:" + PORT + "/?name=Guillaume", getClass().getResourceAsStream("soap-request.xml"), null);
        WebResponse response = new WebConversation().getResponse(req);
        System.out.println(response.getText());
    }
    
    public void testMarhaler() throws Exception {
        String url = "http://64.124.140.30/soap";
        HttpSoapClientMarshaler marshaler = new HttpSoapClientMarshaler();
        PostMethod method = new PostMethod(url);
        method.addRequestHeader("Content-Type", "text/xml");
        method.addRequestHeader("SOAPAction", "urn:xmethods-delayed-quotes#getQuote");
        
        InOnly exchange = new InOnlyImpl("id");
        NormalizedMessage in = exchange.createMessage();
        exchange.setInMessage(in);
        in.setContent(new StringSource("<?xml version='1.0'?><ns1:getQuote xmlns:ns1='urn:xmethods-delayed-quotes' xmlns:xsi='http://www.w3.org/1999/XMLSchema-instance' xmlns:se='http://schemas.xmlsoap.org/soap/envelope/' se:encodingStyle='http://schemas.xmlsoap.org/soap/encoding/'><symbol xsi:type='xsd:string'>SUNW</symbol></ns1:getQuote>"));
        marshaler.fromNMS(method, exchange, in);
        System.out.println(((StringRequestEntity) method.getRequestEntity()).getContent());

        HttpClient httpClient = new HttpClient();
        httpClient.executeMethod(method);
        System.out.println(method.getResponseBodyAsString());
        
        exchange = new InOnlyImpl("id");
        in = exchange.createMessage();
        exchange.setInMessage(in);
        marshaler.toNMS(in, method);
        
        System.out.println(new SourceTransformer().toString(in.getContent()));
    }

}
