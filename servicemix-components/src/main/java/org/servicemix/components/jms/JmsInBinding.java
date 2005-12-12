/**
 * 
 * Copyright 2005 Protique Ltd
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
package org.servicemix.components.jms;

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.components.util.ComponentSupport;
import org.servicemix.jbi.RuntimeJBIException;

/**
 * A JMS {@link MessageListener} which sends the inbound JMS message into the JBI container
 * for processing
 *
 * @version $Revision$
 */
public class JmsInBinding extends ComponentSupport implements MessageListener {
    private static final Log log = LogFactory.getLog(JmsInBinding.class);

    private JmsMarshaler marshaler = new JmsMarshaler();

    public JmsMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(JmsMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public void onMessage(Message jmsMessage) {
        if (log.isTraceEnabled()) {
            log.trace("Received: " + jmsMessage);
        }

        try {
            InOnly messageExchange = getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();

            try {
                marshaler.toNMS(inMessage, jmsMessage);

                messageExchange.setInMessage(inMessage);
                getDeliveryChannel().send(messageExchange);
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
}
