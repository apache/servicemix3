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
package loanbroker;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

import com.sun.org.apache.xpath.internal.CachedXPathAPI;

public class CreditAgency extends ComponentSupport implements MessageExchangeListener {

    public CreditAgency() {
        setService(new QName("urn:logicblaze:soa:creditagency", "CreditAgencyService"));
        setEndpoint("agency");
    }
    
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        InOut inOut = (InOut) exchange;
        if (inOut.getStatus() == ExchangeStatus.DONE) {
            return;
        } else if (inOut.getStatus() == ExchangeStatus.ERROR) {
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
        else if (root != null) {
            return root.getNodeValue();
        } else {
            return null;
        }
    }
}