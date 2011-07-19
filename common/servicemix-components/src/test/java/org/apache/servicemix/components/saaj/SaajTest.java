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

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class SaajTest extends TestSupport {

    private static transient Logger logger = LoggerFactory.getLogger(SaajTest.class);

    protected String quote = "SUNW";

    public void testCurrencyQuotes() throws Exception {
        QName serviceName = new QName("http://servicemix.org/cheese/", "stockQuote");
        String file = "request.xml";

        Object answer = requestServiceWithFileRequest(serviceName, file);
        assertTrue("Shoud return a DOM Node: " + answer, answer instanceof Node);
        Node node = (Node) answer;
        logger.info(transformer.toString(node));

        String text = textValueOfXPath(node, "//Result").trim();

        logger.info("Found price: {}", text);

        assertTrue("price text should not be empty", text.length() > 0);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/saaj/example.xml");
    }

}
