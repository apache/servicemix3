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
package org.apache.servicemix.components.script;

import java.util.List;

import org.apache.servicemix.components.script.ScriptComponent;
import org.apache.servicemix.tck.Sender;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ScriptTest extends org.apache.servicemix.tck.SpringTestSupport {
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
        System.out.println("Found results: " + result);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/script/spring-groovy.xml");
    }
}
