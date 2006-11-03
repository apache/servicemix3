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

import org.apache.servicemix.components.util.ComponentSupport;
import org.logicblaze.lingo.jms.JmsProducer;
import org.logicblaze.lingo.jms.JmsProducerPool;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.xml.transform.TransformerException;

/**
 * A JMS {@link javax.jms.MessageListener}which sends the inbound JMS message into the JBI container for processing
 *
 * @version $Revision$
 */
public class JmsInOutBinding extends ComponentSupport implements MessageListener {
    private JmsProducerPool producerPool;
    private DestinationChooser destinationChooser;
    private JmsMarshaler marshaler = new JmsMarshaler();

    public JmsProducerPool getProducerPool() {
        return producerPool;
    }

    public void setProducerPool(JmsProducerPool producerPool) {
        this.producerPool = producerPool;
    }

    public DestinationChooser getDestinationChooser() {
        return destinationChooser;
    }

    public void setDestinationChooser(DestinationChooser destinationChooser) {
        this.destinationChooser = destinationChooser;
    }

    public JmsMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(JmsMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public void onMessage(Message jmsMessage) {
        JmsProducer producer = producerPool.borrowProducer();
        try {
            InOut messageExchange = getDeliveryChannel().createExchangeFactory().createInOutExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();
            try {
                marshaler.toNMS(inMessage, jmsMessage);
                messageExchange.setInMessage(inMessage);
                if (getDeliveryChannel().sendSync(messageExchange)) {
                    Session session = producer.getSession();
                    Destination destination = destinationChooser.chooseDestination(messageExchange);
                    Message message = marshaler.createMessage(messageExchange.getOutMessage(), session);
                    producer.getMessageProducer().send(destination, message);
                }
            }
            catch (JMSException e) {
                messageExchange.setError(e);
                messageExchange.setStatus(ExchangeStatus.ERROR);
            }
            catch (TransformerException e) {
                messageExchange.setError(e);
                messageExchange.setStatus(ExchangeStatus.ERROR);
            }
        }
        catch (MessagingException me) {
            //
        }

        finally {
            producerPool.returnProducer(producer);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.components.util.ComponentSupport#shutdown()
     */
    public void shutDown() throws JBIException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.components.util.ComponentSupport#start()
     */
    public void start() throws JBIException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.components.util.ComponentSupport#stop()
     */
    public void stop() throws JBIException {
        // TODO Auto-generated method stub

    }
}
