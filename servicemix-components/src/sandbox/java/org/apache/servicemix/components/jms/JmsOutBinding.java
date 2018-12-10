/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.components.util.OutBinding;
import org.logicblaze.lingo.jms.JmsProducer;
import org.logicblaze.lingo.jms.JmsProducerPool;

/**
 * Consumers JBI messages and sends them to a JMS destination
 *
 * @version $Revision$
 */
public class JmsOutBinding extends OutBinding {

    private JmsProducerPool producerPool;
    private DestinationChooser destinationChooser;
    private JmsMarshaler marshaler = new JmsMarshaler();


    // Properties
    //-------------------------------------------------------------------------
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


    // Implementation methods
    //-------------------------------------------------------------------------

    protected void process(MessageExchange messageExchange, NormalizedMessage inMessage) throws MessagingException {
        JmsProducer producer = producerPool.borrowProducer();
        try {
            Session session = producer.getSession();
            Message message = marshaler.createMessage(inMessage, session);
            Destination destination = destinationChooser.chooseDestination(messageExchange);
            producer.getMessageProducer().send(destination, message);
            done(messageExchange);
        }
        catch (JMSException e) {
            throw new MessagingException(e);
        }
        catch (TransformerException e) {
            throw new MessagingException(e);
        }
        finally {
            producerPool.returnProducer(producer);
        }
    }
}
