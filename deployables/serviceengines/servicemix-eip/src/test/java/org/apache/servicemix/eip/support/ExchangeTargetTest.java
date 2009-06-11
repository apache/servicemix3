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
package org.apache.servicemix.eip.support;

import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.servicemix.components.util.EchoComponent;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.tck.mock.MockMessageExchange;

// ExchangeTargetTest creates an ExchangeTarget object and tests the methods on it
// that are not covered by other tests.
public class ExchangeTargetTest extends TestCase {

    private ExchangeTarget exchangeTarget;

    protected void setUp() throws Exception {
        super.setUp();
        exchangeTarget = new ExchangeTarget();
    }

    protected void tearDown() throws Exception {
        exchangeTarget = null;
        super.tearDown();
    }

    // Test afterPropertiesSet() when interface, service, and uri are all null.
    public void testAfterPropertiesSetException() throws Exception {
        try {
            exchangeTarget.afterPropertiesSet();
            fail("afterPropertiesSet should fail when interface, service, and uri are null.");
        } catch (MessagingException me) {
            // test succeeds
        }
    }

    // Test configureTarget() when interface, service, and uri are all null.
    public void testConfigureTargetException() throws Exception {
        try {
            exchangeTarget.configureTarget(null, null);
            fail("configureTarget should fail when interface, service, and uri are null.");
        } catch (MessagingException me) {
            // test succeeds
        }
    }

    // Test configureTarget() when interface, service, uri, and endpoint are set.
    public void testConfigureTargetSet() throws Exception {
        MockMessageExchange exchange = new MockMessageExchange();
        EchoComponent echo = new EchoComponent();
        echo.setService(new QName("urn:test", "echo"));
        echo.setEndpoint("endpoint");

        JBIContainer container = new JBIContainer();
        container.init();

        container.activateComponent(echo, "echo");
        container.start();

        exchangeTarget.setInterface(new QName("test-interface"));
        exchangeTarget.setService(new QName("urn:test", "echo"));
        exchangeTarget.setUri("urn:test:echo");
        exchangeTarget.setEndpoint("endpoint");

        // configureTarget should set the interface, service, and endpoint on the
        // exchange.
        exchangeTarget.configureTarget(exchange, echo.getContext());

        assertNotNull("Service name should be set on the exchange", exchange.getService());
        assertNotNull("Interface name should be set on the exchange", exchange.getInterfaceName());
        assertNotNull("Endpoint should be set on the exchange", exchange.getEndpoint());

        container.stop();

    }

}
