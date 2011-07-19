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
package org.apache.servicemix.jbi.nmr.flow.jms;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test service from SM-174
 */
public class PingService extends ComponentSupport implements MessageExchangeListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            LOGGER.info("GOT A MESSAGE; exchange.status={}", exchange.getStatus());
            NormalizedMessage out = exchange.createMessage();
            out.setContent(new StringSource("<response>Ping back at ya!</response>"));
            LOGGER.info("SENDING RESPONSE; exchange.status={}", exchange.getStatus());
            exchange.setMessage(out, "out");
            getDeliveryChannel().sendSync(exchange);
            LOGGER.info("RESPONSE SENT; exchange.status={}", exchange.getStatus());
        } else {
            LOGGER.info("GOT A MESSAGE; exchange.status={}", exchange.getStatus());
        }
    }
}