/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.apache.servicemix.jbi.config;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.tck.SpringTestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;

import javax.xml.namespace.QName;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @version $Revision$
 */
public class XmlParseTest extends SpringTestSupport {

    protected static final String NAMESPACE = "http://servicemix.org/cheese/";

    public void testParse() throws Exception {
        List activationSpecs = Arrays.asList(jbi.getActivationSpecs());
        assertEquals("Size of activation specs: " + activationSpecs, 1, activationSpecs.size());

        ActivationSpec activationSpec = (ActivationSpec) activationSpecs.get(0);
        SubscriptionSpec[] subscriptions = activationSpec.getSubscriptions();
        assertEquals("Size of subscriptions", 1, subscriptions.length);

        SubscriptionSpec subscription = subscriptions[0];

        QName producer = new QName(NAMESPACE, "producer");
        assertEquals("subscriber.service", producer, subscription.getService());
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new DebugClassPathXmlApplicationContext("org/apache/servicemix/jbi/config/subscription.xml");
    }
}
