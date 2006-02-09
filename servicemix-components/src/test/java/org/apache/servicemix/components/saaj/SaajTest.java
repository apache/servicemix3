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
package org.apache.servicemix.components.saaj;

import javax.xml.namespace.QName;

import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.w3c.dom.Node;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class SaajTest extends TestSupport {

    protected String quote = "SUNW";

    public void testCurrencyQuotes() throws Exception {
        QName serviceName = new QName("http://servicemix.org/cheese/", "stockQuote");
        String file = "request.xml";

        Object answer = requestServiceWithFileRequest(serviceName, file);
        assertTrue("Shoud return a DOM Node: " + answer, answer instanceof Node);
        Node node = (Node) answer;
        System.out.println(transformer.toString(node));

        String text = textValueOfXPath(node, "//Result").trim();

        System.out.println("Found price: " + text);

        assertTrue("price text should not be empty", text.length() > 0);

    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/saaj/example.xml");
    }
}
