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

import javax.jbi.JBIException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * A component which uses a {@link JmsTemplate} to consume messages from a destination, forward then intot the JBI
 * container for processing, and send back the result to the JMS requestor - used for the TopipcRequestor and
 * QueueRequestor pattern
 * 
 * @version $Revision$
 */
public class JmsServiceComponent extends ComponentSupport implements MessageListener, InitializingBean, DisposableBean {
    private static final Log log = LogFactory.getLog(JmsServiceComponent.class);
    private DestinationChooser destinationChooser;
    private JmsMarshaler marshaler = new JmsMarshaler();
    private JmsTemplate template;
    private String selector;
    private MessageConsumer consumer;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private WorkManager workManager;

    /**
     * called by Spring framework after initialization
     * @throws Exception 
     */
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

    /**
     *  called by spring framework on disposal
     * @throws Exception 
     */
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

    protected void init() throws JBIException {
        if (workManager == null) {
            ComponentContextImpl context = (ComponentContextImpl) getContext();
            workManager = context.getWorkManager();
        }
        super.init();
    }

    /**
     * @return Return the DestinationChooser
     */
    public DestinationChooser getDestinationChooser() {
        return destinationChooser;
    }

    /**
     * Set the DestinationChooser
     * 
     * @param destinationChooser
     */
    public void setDestinationChooser(DestinationChooser destinationChooser) {
        this.destinationChooser = destinationChooser;
    }

    /**
     * Get the JMSMarshaller
     * 
     * @return the Marshaller
     */
    public JmsMarshaler getMarshaler() {
        return marshaler;
    }

    /**
     * Set the JMSMarshaller
     * 
     * @param marshaler
     */
    public void setMarshaler(JmsMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return the JmsTemplate
     */
    public JmsTemplate getTemplate() {
        return template;
    }

    /**
     * Set the JmsTemplate
     * 
     * @param template
     */
    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    /**
     * @return Return the selector
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Set the Selector
     * 
     * @param selector
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

     public WorkManager getWorkManager() {
        return workManager;
    }

    public void setWorkManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    /**
     * MessageListener implementation
     * @param jmsMessage 
     */
    public void onMessage(final Message jmsMessage) {
        try {
            workManager.scheduleWork(new Work() {
                public void release() {
                }
                public void run() {
                    handleMessage(jmsMessage);
                }
            });
        } catch (WorkException e) {
            log.error(e);
        }
    }
    
    protected void handleMessage(final Message jmsMessage) {
        try {
            final InOut messageExchange = getDeliveryChannel().createExchangeFactory().createInOutExchange();
            NormalizedMessage inMessage = messageExchange.createMessage();
            try {
                marshaler.toNMS(inMessage, jmsMessage);
                messageExchange.setInMessage(inMessage);
                if (getDeliveryChannel().sendSync(messageExchange)) {
                    Destination destination = getReplyToDestination(jmsMessage, messageExchange);
                    try {
                        template.send(destination, new MessageCreator() {
                            public Message createMessage(Session session) throws JMSException {
                                try {
                                    Message message = marshaler.createMessage(messageExchange.getOutMessage(), session);
                                    message.setJMSCorrelationID(jmsMessage.getJMSCorrelationID());
                                    if (log.isTraceEnabled()) {
                                        log.trace("Sending message to: " + template.getDefaultDestinationName()
                                                + " message: " + message);
                                    }
                                    return message;
                                }
                                catch (TransformerException e) {
                                    JMSException jmsEx = new JMSException("Failed to create a JMS Message: " + e);
                                    jmsEx.setLinkedException(e);
                                    throw jmsEx;
                                }
                            }
                        });
                        done(messageExchange);
                    }
                    catch (JmsException e) {
                        fail(messageExchange, e);
                    }
                }
            }
            catch (JMSException e) {
                log.error("Couldn't process " + jmsMessage, e);
                messageExchange.setError(e);
                messageExchange.setStatus(ExchangeStatus.ERROR);
            }
        }
        catch (MessagingException e) {
            log.error("Failed to process inbound JMS Message: " + jmsMessage, e);
        }
    }

    protected Destination getReplyToDestination(Message jmsMessage, final InOut messageExchange) throws JMSException {
        if (destinationChooser == null) {
            return jmsMessage.getJMSReplyTo();
        }
        return destinationChooser.chooseDestination(messageExchange);
    }

    /**
     * Choose the out bound destination to send the repsonse from JBI too If a DestinatonChooser is set, this is used,
     * else the replyTo destination on the inbound message is used
     * 
     * @param exchange
     * @param inboundMessage
     * @return the choosen outbound destination or null
     * @throws JMSException if no destination can be found
     */
    protected Destination chooseOutBoundDestination(MessageExchange exchange, Message inboundMessage)
            throws JMSException {
        Destination result = null;
        if (destinationChooser != null) {
            result = destinationChooser.chooseDestination(exchange);
        }
        else if (inboundMessage != null && inboundMessage.getJMSReplyTo() != null) {
            result = inboundMessage.getJMSReplyTo();
        }
        if (result == null) {
            log.error("Could not find an outbound destination for " + inboundMessage);
            throw new JMSException("No outbound JMS Destination can be found");
        }
        return result;
    }
}
