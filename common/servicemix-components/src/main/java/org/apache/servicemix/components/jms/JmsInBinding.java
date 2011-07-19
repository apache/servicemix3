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
package org.apache.servicemix.components.jms;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.RuntimeJBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JMS {@link MessageListener} which sends the inbound JMS message into the JBI container
 * for processing
 *
 * @version $Revision$
 */
public class JmsInBinding extends ComponentSupport implements MessageListener, MessageExchangeListener {

    private static final Logger logger = LoggerFactory.getLogger(JmsInBinding.class);

    private JmsMarshaler marshaler = new JmsMarshaler();
    private boolean synchronous = false;

    /**
     * @return the synchronous
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * @param synchronous the synchronous to set
     */
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    public JmsMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(JmsMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public void onMessage(Message jmsMessage) {
        if (logger.isTraceEnabled()) {
            logger.trace("Received: " + jmsMessage);
        }

        try {
            InOnly messageExchange = getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();

            try {
                marshaler.toNMS(inMessage, jmsMessage);

                messageExchange.setInMessage(inMessage);
                if (synchronous) {
                    getDeliveryChannel().sendSync(messageExchange);
                } else {
                    getDeliveryChannel().send(messageExchange);
                }
            }
            catch (JMSException e) {
                messageExchange.setError(e);
                messageExchange.setStatus(ExchangeStatus.ERROR);
            }
        }
        catch (JBIException e) {
            throw new RuntimeJBIException(e);
        }
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        // Do nothing as we only send in-only
        // but this ensure that messages are not queued in the DeliveryChannel
    }

}
