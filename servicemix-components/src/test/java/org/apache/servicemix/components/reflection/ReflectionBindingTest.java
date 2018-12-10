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
package org.apache.servicemix.components.reflection;

import junit.framework.TestCase;

import org.apache.servicemix.components.reflection.ProxyInOnlyBinding;
import org.apache.servicemix.jbi.container.SpringJBIContainer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ReflectionBindingTest extends TestCase {

    private AbstractXmlApplicationContext context;
    protected int messageCount = 20;
    private SpringJBIContainer jbi;

    public void testSendMessagesToJmsThenOutofJmsToReceiver() throws Exception {
        Counter counter = (Counter) ((ProxyInOnlyBinding)jbi.getBean("proxyCounter")).createProxy();        
        for (int i = 1; i <= messageCount; i++) {
            counter.increment();
        }        
        Thread.sleep(100);
        SimpleCounter simpleCounter = (SimpleCounter) getBean("counter");
        assertEquals(messageCount, simpleCounter.getValue() );
        
    }

    protected void setUp() throws Exception {
        context = createBeanFactory();
        jbi = (SpringJBIContainer) getBean("jbi");
    }
    
    protected void tearDown() throws Exception {
        if (context != null) {
            context.close();
        }
    }

    protected Object getBean(String name) {
        Object answer = context.getBean(name);
        assertNotNull("Could not find object in Spring for key: " + name, answer);
        return answer;
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/reflection/example.xml");
    }
    
    
}
