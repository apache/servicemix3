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
package org.apache.servicemix.components.script;

import java.util.List;

import org.apache.servicemix.tck.Sender;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ScriptTest extends org.apache.servicemix.tck.SpringTestSupport {

    private static transient Logger logger = LoggerFactory.getLogger(ScriptTest.class);

    private static List result;

    public static List getResult() {
        return result;
    }

    public static void setResult(List result) {
        ScriptTest.result = result;
    }

    public void testSendingAndReceivingMessagesUsingSpring() throws Exception {
        Sender sender = (Sender) getBean("sender");
        assertNotNull(sender);
        ScriptComponent component = (ScriptComponent) getBean("receiver");
        assertNotNull(component);
        sender.sendMessages(1);

        List result = getResult();
        assertNotNull("Have not received any results from groovy!", result);
        logger.info("Found results: {}", result);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/script/spring-groovy.xml");
    }

}
