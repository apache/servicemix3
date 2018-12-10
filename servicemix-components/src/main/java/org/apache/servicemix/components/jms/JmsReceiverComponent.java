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

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;

/**
 * A component which uses a {@link JmsTemplate} to consume messages from a
 * destination.
 *
 * @version $Revision$
 */
public class JmsReceiverComponent extends JmsInBinding implements InitializingBean, DisposableBean {
    private JmsTemplate template;
    private String selector;
    private MessageConsumer consumer;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;

    public void afterPropertiesSet() throws Exception {
        if (template == null) {
            throw new IllegalArgumentException("Must have a template set");
        }
        connectionFactory = template.getConnectionFactory();
        /*
         * Component code did not work for JMS 1.02 compliant provider because uses APIs
         * that did not exist in JMS 1.02 : ConnectionFactory.createConnection,
         * Connection.createSession
         */
        if (template instanceof org.springframework.jms.core.JmsTemplate102) {
            //Note1 - would've preferred to call JmsTemplate102 methods but they are protected.
            if (template.isPubSubDomain()) {
                javax.jms.TopicConnection tc;
                connection = tc = ((javax.jms.TopicConnectionFactory)connectionFactory).createTopicConnection();
                session = tc.createTopicSession(template.isSessionTransacted(), template.getSessionAcknowledgeMode());
            }
            else {
                javax.jms.QueueConnection qc;
                connection = qc = ((javax.jms.QueueConnectionFactory)connectionFactory).createQueueConnection();
                session = qc.createQueueSession(template.isSessionTransacted(), template.getSessionAcknowledgeMode());
            }
        } else { // JMS 1.1 style
            connection = connectionFactory.createConnection();
            session = connection.createSession(template.isSessionTransacted(), template.getSessionAcknowledgeMode());
        }

        Destination defaultDestination = template.getDefaultDestination();
        if (defaultDestination == null) {
            defaultDestination = template.getDestinationResolver().resolveDestinationName(session, template.getDefaultDestinationName(),
                    template.isPubSubDomain());
        }

        /*
         * Component code did not work for JMS 1.02 compliant provider because uses APIs
         * that did not exist in JMS 1.02: Session.createConsumer
         */
        if (template instanceof org.springframework.jms.core.JmsTemplate102) {
            //Note1 - would've preferred to call JmsTemplate102.createConsumer but it is protected. Code below is same.
            //Note2 - assert that defaultDestination is correct type according to isPubSubDomain()
            if (template.isPubSubDomain()) {
                consumer = ((javax.jms.TopicSession)session).createSubscriber((javax.jms.Topic)defaultDestination, selector, template.isPubSubNoLocal());
            } else {
                consumer = ((javax.jms.QueueSession)session).createReceiver((javax.jms.Queue)defaultDestination, selector);
            }
        } else { // JMS 1.1 style
            consumer = session.createConsumer(defaultDestination, selector);
        }

        consumer.setMessageListener(this);
        connection.start();
    }

    public void destroy() throws Exception {
        try {
            if (connection != null) {
                connection.close();
            } else if (session != null) {
                session.close();
            } else if (consumer != null) {
                consumer.close();
            }
        } finally {
            connection = null;
            session = null;
            consumer = null;
        }
    }

    public JmsTemplate getTemplate() {
        return template;
    }

    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

}

