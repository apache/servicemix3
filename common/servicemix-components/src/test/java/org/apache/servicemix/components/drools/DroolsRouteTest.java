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
package org.apache.servicemix.components.drools;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version $Revision$
 */
public class DroolsRouteTest extends TestSupport {

    public void testFiringRules() throws Exception {
        QName service = new QName("http://servicemix.org/cheese/", "droolsRouter");

        InOut exchange = client.createInOutExchange();
        exchange.setService(service);
        NormalizedMessage message = exchange.getInMessage();
        message.setContent(new StringSource(createMessageXmlText(1)));
        client.sendSync(exchange);
        assertEquals(ExchangeStatus.ACTIVE, exchange.getStatus());
        Node node = transformer.toDOMNode(exchange.getOutMessage().getContent());
        Element e = null;
        if (node instanceof Element) {
            e = (Element) node;
        } else if (node instanceof Document) {
            e = ((Document) node).getDocumentElement();
        } else {
            fail("Node should be an Element or a Document");
        }
        assertEquals("hello", e.getLocalName());
        client.done(exchange);

        exchange = client.createInOutExchange();
        exchange.setService(service);
        message = exchange.getInMessage();
        message.setContent(new StringSource(createMessageXmlText(2)));
        client.sendSync(exchange);
        assertEquals(ExchangeStatus.ACTIVE, exchange.getStatus());
        node = transformer.toDOMNode(exchange.getOutMessage().getContent());
        e = null;
        if (node instanceof Element) {
            e = (Element) node;
        } else if (node instanceof Document) {
            e = ((Document) node).getDocumentElement();
        } else {
            fail("Node should be an Element or a Document");
        }
        assertEquals("world", e.getLocalName());
        client.done(exchange);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/drools/jbi-example-route.xml");
    }
}
