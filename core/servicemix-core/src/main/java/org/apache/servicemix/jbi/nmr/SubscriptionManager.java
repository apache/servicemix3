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
package org.apache.servicemix.jbi.nmr;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles publish/subscribe style messaging in the NMR.
 *
 * @version $Revision$
 */
public class SubscriptionManager extends ComponentSupport implements MessageExchangeListener {

    public static final String COMPONENT_NAME = "#SubscriptionManager#";

    private static final transient Logger LOGGER = LoggerFactory.getLogger(SubscriptionManager.class);

    // SM-229: Avoid StackOverflowException
    private static final String FROM_SUBSCRIPTION_MANAGER = "org.apache.servicemix.jbi.nmr.from_subman";

    private Registry registry;

    private String flowName;

    /**
     * Initialize the SubscriptionManager
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, Registry reg) throws JBIException {
        this.registry = reg;
        broker.getContainer().activateComponent(this, COMPONENT_NAME);
    }

    /**
     * Dispatches the given exchange to all matching subscribers
     * 
     * @param exchange
     * @return true if dispatched to a matching subscriber(s)
     * 
     * @throws JBIException
     */
    protected boolean dispatchToSubscribers(MessageExchangeImpl exchange) throws JBIException {
        Boolean source = (Boolean) exchange.getProperty(FROM_SUBSCRIPTION_MANAGER);
        if (source == null || !source.booleanValue()) {
            List<InternalEndpoint> list = registry.getMatchingSubscriptionEndpoints(exchange);
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    InternalEndpoint endpoint = list.get(i);
                    dispatchToSubscriber(exchange, endpoint);
                }
            }
            return list != null && !list.isEmpty();
        } else {
            return false;
        }
    }

    /**
     * Dispatches the given message exchange to the given endpoint
     * 
     * @param exchange
     * @param endpoint
     * @throws JBIException
     */
    protected void dispatchToSubscriber(MessageExchangeImpl exchange, InternalEndpoint endpoint) throws JBIException {
        if (LOGGER.isDebugEnabled() && endpoint != null) {
            LOGGER.debug("Subscription Endpoint: {}", endpoint.getEndpointName());
        }
        // SM-229: Avoid StackOverflowException
        Boolean source = (Boolean) exchange.getProperty(FROM_SUBSCRIPTION_MANAGER);
        if (source == null || !source.booleanValue()) {
            DeliveryChannel channel = getDeliveryChannel();
            InOnly me = channel.createExchangeFactory().createInOnlyExchange();
            // SM-229: Avoid StackOverflowException
            me.setProperty(FROM_SUBSCRIPTION_MANAGER, Boolean.TRUE);
            NormalizedMessage in = me.createMessage();
            getMessageTransformer().transform(me, exchange.getInMessage(), in);
            me.setInMessage(in);
            me.setEndpoint(endpoint);
            Set names = exchange.getPropertyNames();
            for (Iterator iter = names.iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                me.setProperty(name, exchange.getProperty(name));
            }
            if (Boolean.TRUE.equals(exchange.getProperty(JbiConstants.SEND_SYNC))) {
                channel.sendSync(me);
            } else {
                channel.send(me);
            }
        }
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // We should only receive done exchanges from subscribers
        // but we need that so that they can be dequeued
    }

}
