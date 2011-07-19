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
package org.apache.servicemix.jbi.messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.servicemix.jbi.RuntimeJBIException;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.AsyncReceiverPojo;
import org.apache.servicemix.tck.Receiver;
import org.apache.servicemix.tck.ReceiverComponent;
import org.apache.servicemix.tck.SenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

/**
 * @version $Revision$
 */
public abstract class AbstractPersistenceTest extends AbstractTransactionTest {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AbstractPersistenceTest.class);

    protected JBIContainer createJbiContainer(String name) throws Exception {
        JBIContainer container = new JBIContainer();
        container.setTransactionManager(tm);
        container.setName(name);
        container.setFlow(createFlow());
        container.setPersistent(true);
        container.setMonitorInstallationDirectory(false);
        container.init();
        container.start();
        return container;
    }

    protected void runSimpleTest(final boolean syncSend, final boolean syncReceive) throws Exception {
        // final int numMessages = 1;
        final int numMessages = NUM_MESSAGES;
        final SenderComponent sender = new SenderComponent();
        sender.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));
        final Receiver receiver;
        final Map delivered = new ConcurrentHashMap();
        if (syncReceive) {
            receiver = new ReceiverComponent() {
                public void onMessageExchange(MessageExchange exchange) throws MessagingException {
                    try {
                        if (delivered.get(exchange.getExchangeId()) == null) {
                            AbstractPersistenceTest.LOGGER.info("Message delivery rolled back: {}", exchange.getExchangeId());
                            delivered.put(exchange.getExchangeId(), Boolean.TRUE);
                            tm.setRollbackOnly();
                        } else {
                            AbstractPersistenceTest.LOGGER.info("Message delivery accepted: {}", exchange.getExchangeId());
                            super.onMessageExchange(exchange);
                        }
                    } catch (Exception e) {
                        throw new MessagingException(e);
                    }
                }
            };
        } else {
            receiver = new AsyncReceiverPojo() {
                public void onMessageExchange(MessageExchange exchange) throws MessagingException {
                    try {
                        if (delivered.get(exchange.getExchangeId()) == null) {
                            LOGGER.info("Message delivery rolled back: {}", exchange.getExchangeId());
                            delivered.put(exchange.getExchangeId(), Boolean.TRUE);
                            tm.setRollbackOnly();
                            exchange.setStatus(ExchangeStatus.DONE);
                            getContext().getDeliveryChannel().send(exchange);
                        } else {
                            LOGGER.info("Message delivery accepted: {}", exchange.getExchangeId());
                            super.onMessageExchange(exchange);
                        }
                    } catch (Exception e) {
                        throw new MessagingException(e);
                    }
                }
            };
        }

        senderContainer.activateComponent(new ActivationSpec("sender", sender));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver));

        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                try {
                    sender.sendMessages(numMessages, syncSend);
                } catch (JBIException e) {
                    throw new RuntimeJBIException(e);
                }
                return null;
            }
        });
        //sender.sendMessages(NUM_MESSAGES, syncSend);
        receiver.getMessageList().assertMessagesReceived(numMessages);
    }

}
