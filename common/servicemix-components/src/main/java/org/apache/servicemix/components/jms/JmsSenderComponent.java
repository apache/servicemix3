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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.components.util.OutBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Consumers JBI messages and sends them to a JMS destination using the Spring
 * {@link JmsTemplate}
 *
 * @version $Revision$
 */
public class JmsSenderComponent extends OutBinding {

    private static final Logger logger = LoggerFactory.getLogger(JmsSenderComponent.class);

    private JmsTemplate template;
    private JmsMarshaler marshaler = new JmsMarshaler();


    // Properties
    //-------------------------------------------------------------------------
    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public JmsMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(JmsMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return Returns the needJavaIdentifiers.
     */
    public boolean isNeedJavaIdentifiers() {
        return this.marshaler.isNeedJavaIdentifiers();
    }

    /**
     * @param needJavaIdentifiers The needJavaIdentifiers to set.
     */
    public void setNeedJavaIdentifiers(boolean needJavaIdentifiers) {
        this.marshaler.setNeedJavaIdentifiers(needJavaIdentifiers);
    }    
    
    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange exchange, final NormalizedMessage inMessage) throws Exception {
        template.send(new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                try {
                    Message message = marshaler.createMessage(inMessage, session);
                    logger.trace("Sending message to: {}", template.getDefaultDestinationName());
                    logger.trace("Message: {}", message);
                    return message;
                }
                catch (TransformerException e) {
                    JMSException jmsEx =  new JMSException("Failed to create JMS Message: " + e);
                    jmsEx.setLinkedException(e);
                    throw jmsEx;
                }
            }
        });
        done(exchange);
    }

}
