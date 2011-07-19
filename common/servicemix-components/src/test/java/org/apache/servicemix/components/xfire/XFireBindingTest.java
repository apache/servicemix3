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
package org.apache.servicemix.components.xfire;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import org.apache.servicemix.tck.TestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.support.AbstractXmlApplicationContext;

public class XFireBindingTest extends TestSupport {

    private static transient Logger logger = LoggerFactory.getLogger(XFireBindingTest.class);

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        Object answer = requestServiceWithFileRequest(new QName("http://xfire.components.servicemix.org", "Echo"),
                "/org/apache/servicemix/components/xfire/echo.xml");
        assertTrue("Shoud return a DOM Node: " + answer, answer instanceof Node);
        Node node = (Node) answer;
        logger.info(transformer.toString(node));
        
        Echo echo = (Echo) context.getBean("xfireReceiverService");
        assertEquals(1, echo.getCount());
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(new String[] {
                "/org/apache/servicemix/components/xfire/xfire-inout.xml",
                "/org/codehaus/xfire/spring/xfire.xml"
        });
    }

}
