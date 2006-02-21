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
package org.apache.servicemix.bpe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.bpe.BPEComponent;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.http.HttpEndpoint;
import org.apache.servicemix.http.HttpSpringComponent;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.MessageExchangeSupport;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.CachedXPathAPI;

public class BPEComponentTest extends TestCase {

    private JBIContainer jbi;
    private BPEComponent bpe;
    private ServiceMixClient client;
    
    protected void setUp() throws Exception {
        jbi = new JBIContainer();
        jbi.setFlowName("st");
        jbi.setEmbedded(true);
        jbi.setUseMBeanServer(false);
        jbi.init();
        client = new DefaultServiceMixClient(jbi);
        bpe = new BPEComponent();
        jbi.activateComponent(bpe, "bpe");
    }
    
    protected void tearDown() throws Exception {
        if (jbi != null) {
            jbi.shutDown();
        }
    }
    
    protected void registerCreditAgency() throws Exception {
        ActivationSpec creditAgency = new ActivationSpec();
        creditAgency.setInterfaceName(new QName("urn:logicblaze:soa:creditagency", "CreditAgency"));
        creditAgency.setComponent(new CreditAgency());
        jbi.activateComponent(creditAgency);
    }
    
    protected void registerBanks() throws Exception {
        for (int i = 1; i <= 5; i++) {
            ActivationSpec bank = new ActivationSpec();
            bank.setInterfaceName(new QName("urn:logicblaze:soa:bank", "Bank"));
            bank.setComponent(new Bank(i));
            jbi.activateComponent(bank);
        }
    }
    
    
    protected void registerHttp() throws Exception {
        HttpSpringComponent http = new HttpSpringComponent();
        HttpEndpoint ep = new HttpEndpoint();
        ep.setSoap(true);
        ep.setDefaultMep(MessageExchangeSupport.IN_OUT);
        ep.setRoleAsString("consumer");
        ep.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        ep.setEndpoint("loanbroker");
        ep.setLocationURI("http://localhost:8192");
        http.setEndpoints(new HttpEndpoint[] { ep });
        jbi.activateComponent(http, "http");
    }
    
    public static void copyInputStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
    }
    
    public void testWithHttp() throws Exception {
        registerCreditAgency();
        registerBanks();
        registerHttp();
        jbi.start();
        
        URL url = getClass().getClassLoader().getResource("loanbroker/loanbroker.bpel");
        File path = new File(new URI(url.toString()));
        path = path.getParentFile();
        bpe.getServiceUnitManager().deploy("loanbroker", path.getAbsolutePath());
        bpe.getServiceUnitManager().start("loanbroker");
        
        HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8192").openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        InputStream is = getClass().getClassLoader().getResourceAsStream("request.xml");
        OutputStream os = con.getOutputStream();
        copyInputStream(is, os);
        copyInputStream(con.getInputStream(), System.out);
    }
    
    public void testBPEOk() throws Exception {
        registerCreditAgency();
        registerBanks();
        jbi.start();
        
        URL url = getClass().getClassLoader().getResource("loanbroker/loanbroker.bpel");
        File path = new File(new URI(url.toString()));
        path = path.getParentFile();
        bpe.getServiceUnitManager().deploy("loanbroker", path.getAbsolutePath());
        bpe.getServiceUnitManager().start("loanbroker");
        
        //
        // Message for bank1 and bank2
        //
        MessageExchange me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource("<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn>1234341</ssn><amount>100000.0</amount><duration>12</duration></getLoanQuoteRequest>"));
        long t0 = System.currentTimeMillis();
        client.sendSync(me);
        long t1 = System.currentTimeMillis();
        if (me.getError() != null) {
            throw me.getError();
        }
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        String out = new SourceTransformer().contentToString(me.getMessage("out"));
        System.err.println(out);
        System.err.println("Time: " + (t1 - t0));
        client.done(me);
        
        //
        // Message for bank3 and bank4
        //
        me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource("<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn>1234341</ssn><amount>50000.0</amount><duration>12</duration></getLoanQuoteRequest>"));
        t0 = System.currentTimeMillis();
        client.sendSync(me);
        t1 = System.currentTimeMillis();
        if (me.getError() != null) {
            throw me.getError();
        }
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        out = new SourceTransformer().contentToString(me.getMessage("out"));
        System.err.println(out);
        System.err.println("Time: " + (t1 - t0));
        client.done(me);
        
        //
        // Message for bank5
        //
        me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource("<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn>1234341</ssn><amount>1200.0</amount><duration>12</duration></getLoanQuoteRequest>"));
        t0 = System.currentTimeMillis();
        client.sendSync(me);
        t1 = System.currentTimeMillis();
        if (me.getError() != null) {
            throw me.getError();
        }
        assertEquals(ExchangeStatus.ACTIVE, me.getStatus());
        out = new SourceTransformer().contentToString(me.getMessage("out"));
        System.err.println(out);
        System.err.println("Time: " + (t1 - t0));
        client.done(me);
    }
    
    public void testBPEWithFault() throws Exception {
        registerCreditAgency();
        jbi.start();
        
        URL url = getClass().getClassLoader().getResource("loanbroker/loanbroker.bpel");
        File path = new File(new URI(url.toString()));
        path = path.getParentFile();
        bpe.getServiceUnitManager().deploy("loanbroker", path.getAbsolutePath());
        bpe.getServiceUnitManager().start("loanbroker");
        
        MessageExchange me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource("<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn>234341</ssn></getLoanQuoteRequest>"));
        client.sendSync(me);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        assertNotNull(me.getFault());
        client.done(me);
    }
    
    public void testBPEWithException() throws Exception {
        registerCreditAgency();
        jbi.start();
        
        URL url = getClass().getClassLoader().getResource("loanbroker/loanbroker.bpel");
        File path = new File(new URI(url.toString()));
        path = path.getParentFile();
        bpe.getServiceUnitManager().deploy("loanbroker", path.getAbsolutePath());
        bpe.getServiceUnitManager().start("loanbroker");
        
        MessageExchange me = client.createInOutExchange();
        me.setService(new QName("urn:logicblaze:soa:loanbroker", "LoanBrokerService"));
        me.setOperation(new QName("getLoanQuote"));
        me.getMessage("in").setContent(new StringSource("<getLoanQuoteRequest xmlns=\"urn:logicblaze:soa:loanbroker\"><ssn></ssn></getLoanQuoteRequest>"));
        client.sendSync(me);
        assertEquals(ExchangeStatus.ERROR, me.getStatus());
        assertNotNull(me.getError());
        client.done(me);
    }
    
    public static class Bank extends ComponentSupport implements MessageExchangeListener {
        
        public Bank(int number) {
            setService(new QName("urn:logicblaze:soa:bank", "Bank" + number));
            setEndpoint("bank");
        }
        
        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            InOut inOut = (InOut) exchange;
            if (inOut.getStatus() == ExchangeStatus.DONE) {
                return;
            } else if (inOut.getStatus() == ExchangeStatus.ERROR) {
                done(inOut);
                return;
            }
            System.err.println(getService().getLocalPart() + " requested");
            try {
                String output = "<getLoanQuoteResponse xmlns=\"urn:logicblaze:soa:bank\"><rate>" + (Math.ceil(1000 * Math.random()) / 100) + "</rate></getLoanQuoteResponse>";
                NormalizedMessage answer = inOut.createMessage();
                answer.setContent(new StringSource(output));
                answer(inOut, answer);
            } catch (Exception e) {
                throw new MessagingException(e);
            }
        }
    }
    
    public static class CreditAgency extends ComponentSupport implements MessageExchangeListener {

        public CreditAgency() {
            setService(new QName("urn:logicblaze:soa:creditagency", "CreditAgencyService"));
            setEndpoint("agency");
        }
        
        public void onMessageExchange(MessageExchange exchange) throws MessagingException {
            InOut inOut = (InOut) exchange;
            if (inOut.getStatus() == ExchangeStatus.DONE) {
                return;
            } else if (inOut.getStatus() == ExchangeStatus.ERROR) {
                done(inOut);
                return;
            }
            try {
                Document doc = (Document) new SourceTransformer().toDOMNode(inOut.getInMessage());
                String ssn = textValueOfXPath(doc, "//*[local-name()='ssn']");
                if (ssn == null || ssn.length() == 0) {
                    fail(exchange, new NullPointerException());
                    return;
                } 
                if (!ssn.startsWith("1")) {
                    Fault fault = inOut.createFault();
                    fault.setContent(new StringSource("<InvalidSSN xmlns=\"urn:logicblaze:soa:creditagency\"><ssn>" + ssn + "</ssn></InvalidSSN>"));
                    fail(inOut, fault);
                } else {
                    String operation = null;
                    if (inOut.getOperation() != null) {
                        operation = inOut.getOperation().getLocalPart();
                    } else {
                        operation = doc.getDocumentElement().getLocalName();
                    }
                    String output;
                    if ("getCreditScore".equals(operation)) {
                        output = "<getCreditScoreResponse xmlns=\"urn:logicblaze:soa:creditagency\"><score>" + getCreditScore(ssn) + "</score></getCreditScoreResponse>";
                    } else if ("getCreditHistoryLength".equals(operation)) {
                        output = "<getCreditHistoryLengthResponse xmlns=\"urn:logicblaze:soa:creditagency\"><length>" + getCreditHistoryLength(ssn) + "</length></getCreditHistoryLengthResponse>";
                    } else {
                        throw new UnsupportedOperationException(operation);
                    }
                    NormalizedMessage answer = inOut.createMessage();
                    answer.setContent(new StringSource(output));
                    answer(inOut, answer);
                }
            } catch (Exception e) {
                throw new MessagingException(e);
            }
        }
        int getCreditScore(String ssn) {
            //return ((int) (Math.random() * 600) + 300);
            return 1000;
        }
        int getCreditHistoryLength(String ssn) {
            //return ((int) (Math.random() * 19) + 1);
            return 10;
        }
        
    }
    
    protected static String textValueOfXPath(Node node, String xpath) throws TransformerException {
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
        else if (root != null) {
            return root.getNodeValue();
        } else {
            return null;
        }
    }
}
