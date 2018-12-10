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
package org.apache.servicemix.components.jmx;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision: 359186 $
 */
public class CounterMonitorComponentTest extends TestSupport {

    public void testCounter() throws Exception {
        int nb = 10;
        for (int i = 0; i < nb; i++) {
            client.send(null, new StringSource("<hello>world</hello>"));
            // We need to sleep more than the granularity period length
            // so that we do not loose notifications
            Thread.sleep(50);
        }
        receiver.getMessageList().assertMessagesReceived(nb);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/jmx/example.xml");
    }
    
    
}
