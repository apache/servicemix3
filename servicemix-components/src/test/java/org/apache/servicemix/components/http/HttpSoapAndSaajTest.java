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

import junit.framework.TestCase;

import org.apache.servicemix.components.http.HttpSoapConnector;
import org.apache.servicemix.components.saaj.SaajBinding;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.util.FileUtil;

public class HttpSoapAndSaajTest extends TestCase {
    
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
        new SourceTransformer().toDOMNode(new StringSource(baos.toString()));
    }

}
