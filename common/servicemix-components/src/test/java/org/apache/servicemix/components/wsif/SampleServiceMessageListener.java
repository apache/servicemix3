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
package org.apache.servicemix.components.wsif;


import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * This is a simple MDB that processes a message containing an integer
 * that represents a zip code, and returns a message containing a
 * boolean indicating whether DSL service is available at that zip code
 * or not.
 * Internally the bean just returns true for zip codes < 50000 and
 * false otherwise.
 *
 * @version $Revision$
 */
public class SampleServiceMessageListener implements MessageListener, InitializingBean {

    private static transient Logger logger = LoggerFactory.getLogger(SampleServiceMessageListener.class);

    private JmsTemplate template;

    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public void afterPropertiesSet() throws Exception {
        if (template == null) {
            throw new IllegalArgumentException("The template property is not set");
        }
    }

    public void onMessage(Message msg) {
        try {
            TextMessage message = (TextMessage) msg;
            // assume we have an integer
            String text = message.getText();
            logger.info("Text: {}", text);
            int zipCode = new Integer(text).intValue();
            // return true if zip code < 50000, false otherwise
            if (zipCode < 50000) {
                sendMessage(message, "true");
            }
            else {
                sendMessage(message, "false");
            }
        }
        catch (Exception e) {
            // aargh - this should not happen usually
            logger.error(e.getMessage(), e);
        }
    }

    public void sendMessage(final TextMessage requestMsg, final String serviceAvailable) throws JMSException {
        template.send(requestMsg.getJMSReplyTo(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage();
                // set the correlation ID
                message.setJMSCorrelationID(requestMsg.getJMSMessageID());
                // set the text
                message.setText(serviceAvailable);
                return message;
            }
        });
    }

}	

