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
package org.apache.servicemix.tck;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.api.EndpointResolver;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.resolver.NullEndpointFilter;

/**
 * @version $Revision$
 */
public class SenderComponent extends ComponentSupport implements Sender {

    public static final QName SERVICE = new QName("http://servicemix.org/example/", "sender");
    public static final String ENDPOINT = "sender";

    private EndpointResolver resolver;
    private String message = "<s12:Envelope xmlns:s12='http://www.w3.org/2003/05/soap-envelope'>" 
                           + "  <s12:Body>"
                           + "    <foo>Hello!</foo>"
                           + "  </s12:Body>"
                           + "</s12:Envelope>";

    public SenderComponent() {
        super(SERVICE, ENDPOINT);
    }

    public EndpointResolver getResolver() {
        return resolver;
    }

    public void setResolver(EndpointResolver resolver) {
        this.resolver = resolver;
    }

    public void sendMessages(int messageCount) throws JBIException {
        sendMessages(messageCount, false);
    }

    public void sendMessages(int messageCount, boolean sync) throws JBIException {
        ComponentContext context = getContext();

        for (int i = 0; i < messageCount; i++) {
            InOnly exchange = context.getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
            NormalizedMessage msg = exchange.createMessage();

            ServiceEndpoint destination = null;
            if (resolver != null) {
                destination = resolver.resolveEndpoint(getContext(), exchange, NullEndpointFilter.getInstance());
            }
            if (destination != null) {
                // lets explicitly specify the destination - otherwise
                // we'll let the container choose for us
                exchange.setEndpoint(destination);
            }

            exchange.setInMessage(msg);
            // lets set the XML as a byte[], String or DOM etc
            msg.setContent(new StringSource(this.message));
            if (sync) {
                boolean result = context.getDeliveryChannel().sendSync(exchange, 1000);
                if (!result) {
                    throw new MessagingException("Message delivery using sendSync has timed out");
                }
            } else {
                context.getDeliveryChannel().send(exchange);
            }
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
