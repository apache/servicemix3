/**
 * 
 * Copyright RAJD Consultanct Ltd
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
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.components.util.ComponentSupport;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.SessionCallback;

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

    /**
     * called by Spring framework after initialization
     * @throws Exception 
     */
    public void afterPropertiesSet() throws Exception {
        if (template == null) {
            throw new IllegalArgumentException("Must have a template set");
        }
        template.execute(new SessionCallback() {
            public Object doInJms(Session session) throws JMSException {
                Destination defaultDestination = template.getDefaultDestination();
                if (defaultDestination == null) {
                    defaultDestination = template.getDestinationResolver().resolveDestinationName(session,
                            template.getDefaultDestinationName(), template.isPubSubDomain());
                }
                consumer = session.createConsumer(defaultDestination, selector);
                return null;
            }
        }, true);
        consumer.setMessageListener(this);
    }

    /**
     *  called by spring framework on disposal
     * @throws Exception 
     */
    public void destroy() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
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
    public JmsMarshaler getMarshaller() {
        return marshaler;
    }

    /**
     * Set the JMSMarshaller
     * 
     * @param marshaler
     */
    public void setMarshaller(JmsMarshaler marshaler) {
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

    /**
     * MessageListener implementation
     * @param jmsMessage 
     */
    public void onMessage(final Message jmsMessage) {
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
