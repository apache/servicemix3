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
package org.apache.servicemix.camel;

import java.util.List;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.xml.namespace.QName;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.converter.jaxp.StringSource;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.jbi.container.ActivationSpec;

/**
 * Tests on handling JBI InOnly exchanges by Camel 
 */
public class JbiInOnlyTest extends JbiTestSupport {
    
    private static final String MESSAGE = "<just><a>test</a></just>";

    public void testInOnlyExchangeConvertBody() throws Exception {
        MockEndpoint done = getMockEndpoint("mock:done");
        done.expectedBodiesReceived(MESSAGE);
        
        ServiceMixClient client = new DefaultServiceMixClient(jbiContainer);
        InOnly exchange = client.createInOnlyExchange();
        exchange.setService(new QName("urn:test", "in-only"));
        exchange.getInMessage().setContent(new StringSource(MESSAGE));
        client.send(exchange);
        client.receive(1000);
        assertEquals(ExchangeStatus.DONE, exchange.getStatus());
        done.assertIsSatisfied();
    }
    
    public void testInOnlyExchangeForwardAndConvertBody() throws Exception {
        MockEndpoint done = getMockEndpoint("mock:done");
        done.expectedBodiesReceived(MESSAGE);
        
        ServiceMixClient client = new DefaultServiceMixClient(jbiContainer);
        InOnly exchange = client.createInOnlyExchange();
        exchange.setService(new QName("urn:test", "forward"));
        exchange.getInMessage().setContent(new StringSource(MESSAGE));
        client.sendSync(exchange);
        
        done.assertIsSatisfied();
    }

    @Override
    protected void appendJbiActivationSpecs(List<ActivationSpec> activationSpecList) {
        // no additional activation specs required
    }
    
    public void testInOnlyToAggregator() throws Exception {
        ServiceMixClient smxClient = getServicemixClient();
        getMockEndpoint("mock:aggregated").expectedMessageCount(1);
        for (int i = 0; i < 50; i++) {
            InOnly exchange = smxClient.createInOnlyExchange();
            exchange.setService(new QName("urn:test", "in-only-aggregator"));
            exchange.getInMessage().setProperty("key", "aggregate-this");
            exchange.getInMessage().setContent(new StringSource("<request>Could you please aggregate this?</request>"));
            smxClient.send(exchange);
        }
        getMockEndpoint("mock:aggregated").assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRoutes() {
        return new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                from("jbi:service:urn:test:forward").to("jbi:service:urn:test:in-only?mep=in-only");
                from("jbi:service:urn:test:in-only").convertBodyTo(String.class).to("mock:done");
                from("jbi:service:urn:test:in-only-aggregator")
                    .aggregator(header("key"))
                    .setHeader("aggregated").constant(true)
                    .to("mock:aggregated");
            }
            
        };
    }
}
