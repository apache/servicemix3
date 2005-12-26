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
package org.apache.servicemix.components.validation;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.tck.TestSupport;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.xbean.spring.context.ClassPathXmlApplicationContext;

/**
 * @version $Revision$
 */
public class ValidationTest extends TestSupport {

    public void testValidMessage() throws Exception {
        InOut exchange = client.createInOutExchange();
        exchange.getInMessage().setContent(getSourceFromClassPath("requestValid.xml"));
        client.sendSync(exchange);

        NormalizedMessage out = exchange.getOutMessage();
        Fault fault = exchange.getFault();
        Exception error = exchange.getError();

        assertEquals("error", null, error);
        assertEquals("fault", null, fault);

        assertNotNull("Should have an out message", out);
    }

    public void testInvalidMessage() throws Exception {
        InOut exchange = client.createInOutExchange();
        exchange.getInMessage().setContent(getSourceFromClassPath("requestInvalid.xml"));
        client.sendSync(exchange);

        NormalizedMessage out = exchange.getOutMessage();
        Fault fault = exchange.getFault();
        Exception error = exchange.getError();

        assertEquals("out", null, out);
        assertNotNull("Should have a fault", fault);

        System.out.println("error is: " + error);

        System.out.println("Fault is...");
        System.out.println(transformer.toString(fault.getContent()));

        // TODO?
        //assertEquals("error", null, error);
    }

    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext("org/apache/servicemix/components/validation/example.xml");
    }
}
