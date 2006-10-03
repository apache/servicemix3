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
package org.apache.servicemix.tck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.xpath.CachedXPathAPI;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.ExchangeStatus;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Revision$
 */
public abstract class SpringTestSupport extends TestCase {
    protected transient Log log = LogFactory.getLog(getClass());

    protected AbstractXmlApplicationContext context;
    protected SourceTransformer transformer;
    protected int messageCount = 20;
    protected SpringJBIContainer jbi;

    protected void setUp() throws Exception {
        transformer = new SourceTransformer();
        context = createBeanFactory();
        jbi = (SpringJBIContainer) context.getBean("jbi");
        assertNotNull("JBI Container not found in spring!", jbi);
    }

    protected void tearDown() throws Exception {
        if (context != null) {
            log.info("Closing down the spring context");
            context.destroy();
        }
    }

    protected Object getBean(String name) {
        Object answer = null;
        if (jbi != null) {
            answer = jbi.getBean(name);
        }
        if (answer == null) {
            answer = context.getBean(name);
        }
        assertNotNull("Could not find object in Spring for key: " + name, answer);
        return answer;
    }

    protected abstract AbstractXmlApplicationContext createBeanFactory();

    /**
     * Performs an XPath expression and returns the Text content of the root node.
     *
     * @param node
     * @param xpath
     * @return
     * @throws TransformerException
     */
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

    protected Source getSourceFromClassPath(String fileOnClassPath) {
        InputStream stream = getClass().getResourceAsStream(fileOnClassPath);
        assertNotNull("Could not find file: " + fileOnClassPath + " on the classpath", stream);
        Source content = new StreamSource(stream);
        return content;
    }

    protected void assertMessagesReceived(MessageList messageList, int messageCount) throws MessagingException, TransformerException, ParserConfigurationException, IOException, SAXException {
        messageList.assertMessagesReceived(messageCount);
        List list = messageList.getMessages();
        int counter = 0;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            NormalizedMessage message = (NormalizedMessage) iter.next();
            log.info("Message " + (counter++) + " is: " + message);
            log.info(transformer.contentToString(message));
        }
    }


    protected void assertExchangeWorked(MessageExchange me) throws Exception {
        if (me.getStatus() == ExchangeStatus.ERROR) {
            if (me.getError() != null) {
                throw me.getError();
            }
            else {
                fail("Received ERROR status");
            }
        }
        else if (me.getFault() != null) {
            fail("Received fault: " + new SourceTransformer().toString(me.getFault().getContent()));
        }
    }
}
