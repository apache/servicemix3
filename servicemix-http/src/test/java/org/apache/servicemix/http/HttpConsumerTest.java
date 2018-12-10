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
package org.apache.servicemix.http;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.components.http.HttpInvoker;
import org.apache.servicemix.components.http.HttpSoapClientMarshaler;
import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;

public class HttpConsumerTest extends TestCase {

    protected JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.setEmbedded(true);
        container.init();
    }
    
    protected void tearDown() throws Exception {
        if (container != null) {
            container.shutDown();
        }
    }
    
    protected long testInOnly(String msg, boolean streaming) throws Exception {
        // HTTP Component
        HttpComponent component = new HttpComponent();
        ((HttpLifeCycle) component.getLifeCycle()).getConfiguration().setStreamingEnabled(streaming);
        container.activateComponent(component, "HTTPComponent");
        
        // Add a receiver component
        Receiver receiver = new ReceiverComponent();
        ActivationSpec asReceiver = new ActivationSpec("receiver", receiver);
        asReceiver.setService(new QName("http://http.servicemix.org/Test", "ConsumerInOnly"));
        container.activateComponent(asReceiver);
        
        // Add the http invoker
        HttpInvoker invoker = new HttpInvoker();
        invoker.setDefaultInOut(false);
        invoker.setUrl("http://localhost:8192/InOnly/");
        invoker.setMarshaler(new HttpSoapClientMarshaler(true));
        ActivationSpec asInvoker = new ActivationSpec("invoker", invoker);
        asInvoker.setService(new QName("urn:test", "invoker"));
        container.activateComponent(asInvoker);
        
        // Start container
        container.start();

        // Deploy SU
        URL url = getClass().getClassLoader().getResource("consumer/http.wsdl");
        File path = new File(new URI(url.toString()));
        path = path.getParentFile();
        component.getServiceUnitManager().deploy("consumer", path.getAbsolutePath());
        component.getServiceUnitManager().start("consumer");
        
        // Call it
        DefaultServiceMixClient client = new DefaultServiceMixClient(container);
        RobustInOnly in = client.createRobustInOnlyExchange();
        in.setService(new QName("urn:test", "invoker"));
        in.getInMessage().setContent(new StreamSource(new ByteArrayInputStream(msg.getBytes())));
        
        long t0 = System.currentTimeMillis();
        client.sendSync(in);
        long t1 = System.currentTimeMillis();
        assertTrue(in.getStatus() == ExchangeStatus.DONE);
        
        // Check we received the message
        receiver.getMessageList().assertMessagesReceived(1);
        
        return t1 - t0;
    }
        

    protected long testInOut(String msg, boolean streaming) throws Exception {
        // HTTP Component
        HttpComponent component = new HttpComponent();
        ((HttpLifeCycle) component.getLifeCycle()).getConfiguration().setStreamingEnabled(streaming);
        container.activateComponent(component, "HTTPComponent");
        
        // Add a receiver component
        EchoComponent echo = new EchoComponent();
        ActivationSpec asReceiver = new ActivationSpec("echo", echo);
        asReceiver.setService(new QName("http://http.servicemix.org/Test", "ConsumerInOut"));
        container.activateComponent(asReceiver);
        
        // Add the http invoker
        HttpInvoker invoker = new HttpInvoker();
        invoker.setDefaultInOut(true);
        invoker.setUrl("http://localhost:8192/InOut/");
        ActivationSpec asInvoker = new ActivationSpec("invoker", invoker);
        asInvoker.setService(new QName("urn:test", "invoker"));
        container.activateComponent(asInvoker);
        
        // Start container
        container.start();

        // Deploy SU
        URL url = getClass().getClassLoader().getResource("consumer/http.wsdl");
        File path = new File(new URI(url.toString()));
        path = path.getParentFile();
        component.getServiceUnitManager().deploy("consumer", path.getAbsolutePath());
        component.getServiceUnitManager().start("consumer");

        // Call it
        DefaultServiceMixClient client = new DefaultServiceMixClient(container);
        InOut inout = client.createInOutExchange();
        inout.setService(new QName("urn:test", "invoker"));
        inout.getInMessage().setContent(new StreamSource(new ByteArrayInputStream(msg.getBytes())));
        
        long t0 = System.currentTimeMillis();
        client.sendSync(inout);
        long t1 = System.currentTimeMillis();
        assertTrue(inout.getStatus() == ExchangeStatus.ACTIVE);
        
        // Check we received the message
        assertNotNull(inout.getOutMessage());
        assertNotNull(inout.getOutMessage().getContent());
        System.out.println(new SourceTransformer().toString(inout.getOutMessage().getContent()));
        
        return t1 - t0;
    }
        
    public void testInOnly() throws Exception {
        testInOnly("<hello>world</hello>", false);
    }
    
    public void testInOut() throws Exception {
        testInOut("<hello>world</hello>", true);
    }
    
    public void testPerfInOnlyWithBigMessage() throws Exception {
        int nbRuns = 2;
        int sizeInKb = 64;
        
        StringBuffer sb = new StringBuffer();
        sb.append("<hello>");
        for (int i = 0; i < sizeInKb; i++) {
            sb.append("<hello>");
            for (int j = 0; j < 1024 - 15; j++) {
                sb.append((char) ('A' + (int)(Math.random() * ('Z' - 'A' + 1))));
            }
            sb.append("</hello>");
        }
        sb.append("</hello>");
        String str = sb.toString();
        
        for(int i = 0; i < nbRuns; i++) { 
            System.gc();
            long dt = testInOnly(str, false);
            System.err.println("No Streaming: " + dt);
            tearDown();
            setUp();
        }
        
        for(int i = 0; i < nbRuns; i++) { 
            System.gc();
            long dt = testInOnly(str, true);
            System.err.println("Streaming: " + dt);
            tearDown();
            setUp();
        }
    }
        
}
