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

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.messaging.URLEndpoint;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;

import junit.framework.TestCase;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.components.http.HttpConnector;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.StringSource;

public class SaajSoapActionTest extends TestCase {

    private JBIContainer jbi;
    private SaajBinding saaj;
    private HttpConnector http;
    
    protected void setUp() throws Exception {
        jbi = new JBIContainer();
        jbi.setEmbedded(true);
        jbi.setUseMBeanServer(false);
        jbi.setCreateMBeanServer(false);
        jbi.init();
    }
    
    protected void tearDown() throws Exception {
        jbi.shutDown();
    }
    
    protected String testSoapAction(String soapAction, MessageFactory messageFactory) throws Exception {
        saaj = new SaajBinding();
        saaj.setService(new QName("saaj"));
        saaj.setEndpoint("endpoint");
        saaj.setSoapEndpoint(new URLEndpoint("http://localhost:8192/"));
        saaj.setSoapAction(soapAction);
        SaajMarshaler marshaler = new SaajMarshaler();
        marshaler.setMessageFactory(messageFactory);
        saaj.setMarshaler(marshaler);
        jbi.activateComponent(saaj, "saaj");
        
        http = new HttpConnector(new org.mortbay.jetty.nio.SelectChannelConnector());
        http.setHost("localhost");
        http.setPort(8192);
        //http.setDefaultInOut(false);
        ActivationSpec httpAs = new ActivationSpec();
        httpAs.setComponent(http);
        httpAs.setComponentName("http");
        httpAs.setDestinationService(new QName("receiver"));
        jbi.activateComponent(httpAs);
        
        SoapActionReceiver receiver = new SoapActionReceiver();
        jbi.activateComponent(receiver, "receiver");

        jbi.start();
        
        DefaultServiceMixClient client = new DefaultServiceMixClient(jbi);
        InOut me = client.createInOutExchange();
        me.setService(new QName("saaj"));
        me.getInMessage().setContent(new StringSource("<hello>world</hello>"));
        client.sendSync(me);
        if (me.getStatus() == ExchangeStatus.ERROR) {
            if (me.getError() != null) {
                throw me.getError();
            }
        }
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        client.done(me);
        
        Thread.sleep(50);
        
        return receiver.sentSoapAction;
    }
    
    public void testNullSoapActionAxis() throws Exception {
        String received = testSoapAction(null, new org.apache.axis.soap.MessageFactoryImpl());
        assertEquals("\"\"", received);
    }
    
    /*
    public void testNullSoapActionSun() throws Exception {
        String received = testSoapAction(null, new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl());
        assertEquals("\"\"", received);
    }
    */
    
    public void testEmptySoapActionAxis() throws Exception {
        String received = testSoapAction("", new org.apache.axis.soap.MessageFactoryImpl());
        assertEquals("\"\"", received);
    }
    
    /*
    public void testEmptySoapActionSun() throws Exception {
        String received = testSoapAction("", new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl());
        assertEquals("", received);
    }
    */
    
    public void testQuotesSoapActionAxis() throws Exception {
        String received = testSoapAction("\"\"", new org.apache.axis.soap.MessageFactoryImpl());
        assertEquals("\"\"", received);
    }
    
    /*
    public void testQuotesSoapActionSun() throws Exception {
        String received = testSoapAction("\"\"", new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl());
        assertEquals("\"\"", received);
    }
    */
    
    public void testWithSoapActionAxis() throws Exception {
        String received = testSoapAction("action", new org.apache.axis.soap.MessageFactoryImpl());
        assertEquals("action", received);
    }
    
    /*
    public void testWithSoapActionSun() throws Exception {
        String received = testSoapAction("action", new com.sun.xml.messaging.saaj.soap.MessageFactoryImpl());
        assertEquals("action", received);
    }
    */
    
    protected static class SoapActionReceiver extends ComponentSupport implements MessageExchangeListener {
        public String sentSoapAction;
        public SoapActionReceiver() {
            setService(new QName("receiver"));
            setEndpoint("endpoint");
        }
        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                NormalizedMessage msg = exchange.getMessage("in");
                sentSoapAction = (String) msg.getProperty("SOAPAction");
                NormalizedMessage out = exchange.createMessage();
                out.setContent(msg.getContent());
                answer(exchange, out);
            }
        }
        
    }
    
}
