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
package org.apache.servicemix.jbi.nmr.flow.jms;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.event.EndpointAdapter;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.EndpointListener;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * Use for message routing among a network of containers. All routing/registration happens automatically.
 * 
 * @version $Revision$
 */
public class JMSFlow extends AbstractFlow implements MessageListener {

    private static final Log log = LogFactory.getLog(JMSFlow.class);

    private static final String INBOUND_PREFIX = "org.apache.servicemix.jms.";

    private String jmsURL = "peer://org.apache.servicemix?persistent=false";

    private String userName;

    private String password;

    private ActiveMQConnectionFactory connectionFactory;

    private ActiveMQConnection connection;

    private String broadcastDestinationName = "org.apache.servicemix.JMSFlow";

    private MessageProducer queueProducer;

    private MessageProducer topicProducer;

    private Topic broadcastTopic;

    private Session broadcastSession;

    private MessageConsumer broadcastConsumer;

    private Session inboundSession;

    private MessageConsumer advisoryConsumer;

    private Set subscriberSet = new CopyOnWriteArraySet();

    private Map consumerMap = new ConcurrentHashMap();

    private AtomicBoolean started = new AtomicBoolean(false);

    private EndpointListener endpointListener;

    /**
     * The type of Flow
     * 
     * @return the type
     */
    public String getDescription() {
        return "jms";
    }

    /**
     * @return Returns the jmsURL.
     */
    public String getJmsURL() {
        return jmsURL;
    }

    /**
     * @param jmsURL The jmsURL to set.
     */
    public void setJmsURL(String jmsURL) {
        this.jmsURL = jmsURL;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return Returns the connectionFactory.
     */
    public ActiveMQConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * @param connectionFactory The connectionFactory to set.
     */
    public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @return Returns the broadcastDestinationName.
     */
    public String getBroadcastDestinationName() {
        return broadcastDestinationName;
    }

    /**
     * @param broadcastDestinationName The broadcastDestinationName to set.
     */
    public void setBroadcastDestinationName(String broadcastDestinationName) {
        this.broadcastDestinationName = broadcastDestinationName;
    }

    /**
     * Check if the flow can support the requested QoS for this exchange
     * @param me the exchange to check
     * @return true if this flow can handle the given exchange
     */
    public boolean canHandle(MessageExchange me) {
        if (isTransacted(me)) {
            return false;
        }
        return true;
    }

    /**
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, String subType) throws JBIException {
        log.info(broker.getContainerName() + ": Initializing jms flow");
        super.init(broker, subType);
        // Create and register endpoint listener
        endpointListener = new EndpointAdapter() {
            public void internalEndpointRegistered(EndpointEvent event) {
                onInternalEndpointRegistered(event, true);
            }

            public void internalEndpointUnregistered(EndpointEvent event) {
                onInternalEndpointUnregistered(event, true);
            }
        };
        broker.getContainer().addListener(endpointListener);
        try {
            if (connectionFactory == null) {
                if (jmsURL != null) {
                    connectionFactory = new ActiveMQConnectionFactory(jmsURL);
                } else {
                    connectionFactory = new ActiveMQConnectionFactory();
                }
            }
            if (userName != null) {
                connection = (ActiveMQConnection) connectionFactory.createConnection(userName, password);
            } else {
                connection = (ActiveMQConnection) connectionFactory.createConnection();
            }
            connection.setClientID(broker.getContainerName());
            connection.start();
            inboundSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = inboundSession.createQueue(INBOUND_PREFIX + broker.getContainerName());
            MessageConsumer inboundQueue = inboundSession.createConsumer(queue);
            inboundQueue.setMessageListener(this);
            queueProducer = inboundSession.createProducer(null);
            broadcastSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            broadcastTopic = broadcastSession.createTopic(broadcastDestinationName);
            topicProducer = broadcastSession.createProducer(broadcastTopic);
            topicProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (JMSException e) {
            log.error("Failed to initialize JMSFlow", e);
            throw new JBIException(e);
        }
    }

    /**
     * start the flow
     * 
     * @throws JBIException
     */
    public void start() throws JBIException {
        if (started.compareAndSet(false, true)) {
            log.info(broker.getContainerName() + ": Starting jms flow");
            super.start();
            try {
                broadcastConsumer = broadcastSession.createConsumer(broadcastTopic, null, true);
                broadcastConsumer.setMessageListener(new MessageListener() {
                    public void onMessage(Message message) {
                        try {
                            Object obj = ((ObjectMessage) message).getObject();
                            if (obj instanceof EndpointEvent) {
                                EndpointEvent event = (EndpointEvent) obj;
                                if (event.getEventType() == EndpointEvent.INTERNAL_ENDPOINT_REGISTERED) {
                                    onRemoteEndpointRegistered(event);
                                } else if (event.getEventType() == EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED) {
                                    onRemoteEndpointUnregistered(event);
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error processing incoming broadcast message", e);
                        }
                    }
                });
                Topic advisoryTopic = AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) broadcastTopic);
                advisoryConsumer = broadcastSession.createConsumer(advisoryTopic);
                advisoryConsumer.setMessageListener(new MessageListener() {
                    public void onMessage(Message message) {
                        if (started.get()) {
                            onAdvisoryMessage(((ActiveMQMessage) message).getDataStructure());
                        }
                    }
                });

                // Start queue consumers for all components
                ServiceEndpoint[] endpoints = broker.getRegistry().getEndpointsForInterface(null);
                for (int i = 0; i < endpoints.length; i++) {
                    if (endpoints[i] instanceof InternalEndpoint && ((InternalEndpoint) endpoints[i]).isLocal()) {
                        onInternalEndpointRegistered(new EndpointEvent(endpoints[i],
                                EndpointEvent.INTERNAL_ENDPOINT_REGISTERED), false);
                    }
                }
            } catch (JMSException e) {
                JBIException jbiEx = new JBIException("JMSException caught in start: " + e.getMessage());
                throw jbiEx;
            }
        }
    }

    /**
     * stop the flow
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException {
        if (started.compareAndSet(true, false)) {
            log.info(broker.getContainerName() + ": Stopping jms flow");
            super.stop();
            for (Iterator it = subscriberSet.iterator(); it.hasNext();) {
                String id = (String) it.next();
                removeAllPackets(id);
            }
            subscriberSet.clear();
            try {
                advisoryConsumer.close();
                broadcastConsumer.close();
            } catch (JMSException e) {
                JBIException jbiEx = new JBIException("JMSException caught in stop: " + e.getMessage());
                throw jbiEx;
            }
        }
    }

    public void shutDown() throws JBIException {
        super.shutDown();
        stop();
        // Remove endpoint listener
        broker.getContainer().removeListener(endpointListener);
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (JMSException e) {
                log.warn("Error closing JMS Connection", e);
            }
        }
    }

    /**
     * useful for testing
     * 
     * @return number of containers in the network
     */
    public int numberInNetwork() {
        return subscriberSet.size();
    }

    public void onInternalEndpointRegistered(EndpointEvent event, boolean broadcast) {
        if (!started.get()) {
            return;
        }
        try {
            String key = event.getEndpoint().getServiceName() + event.getEndpoint().getEndpointName();
            if (!consumerMap.containsKey(key)) {
                Queue queue = inboundSession.createQueue(INBOUND_PREFIX + key);
                MessageConsumer consumer = inboundSession.createConsumer(queue);
                consumer.setMessageListener(this);
                consumerMap.put(key, consumer);
            }
            if (broadcast) {
                log.info(broker.getContainerName() + ": broadcasting info for " + event);
                ObjectMessage msg = broadcastSession.createObjectMessage(event);
                topicProducer.send(msg);
            }
        } catch (Exception e) {
            log.error("Cannot create consumer for " + event.getEndpoint(), e);
        }
    }

    public void onInternalEndpointUnregistered(EndpointEvent event, boolean broadcast) {
        try {
            String key = event.getEndpoint().getServiceName() + event.getEndpoint().getEndpointName();
            MessageConsumer consumer = (MessageConsumer) consumerMap.remove(key);
            if (consumer != null) {
                consumer.close();
            }
            if (broadcast) {
                ObjectMessage msg = broadcastSession.createObjectMessage(event);
                log.info(broker.getContainerName() + ": broadcasting info for " + event);
                topicProducer.send(msg);
            }
        } catch (Exception e) {
            log.error("Cannot destroy consumer for " + event, e);
        }
    }

    public void onRemoteEndpointRegistered(EndpointEvent event) {
        log.info(broker.getContainerName() + ": adding remote endpoint: " + event.getEndpoint());
        broker.getRegistry().registerRemoteEndpoint(event.getEndpoint());
    }

    public void onRemoteEndpointUnregistered(EndpointEvent event) {
        log.info(broker.getContainerName() + ": removing remote endpoint: " + event.getEndpoint());
        broker.getRegistry().unregisterRemoteEndpoint(event.getEndpoint());
    }

    /**
     * Distribute an ExchangePacket
     * 
     * @param me
     * @throws MessagingException
     */
    protected void doSend(MessageExchangeImpl me) throws MessagingException {
        doRouting(me);
    }

    /**
     * Distribute an ExchangePacket
     * 
     * @param me
     * @throws MessagingException
     */
    public void doRouting(MessageExchangeImpl me) throws MessagingException {
        // let ActiveMQ do the routing ...
        try {
            String destination;
            if (me.getRole() == Role.PROVIDER) {
                destination = INBOUND_PREFIX + me.getEndpoint().getServiceName() + me.getEndpoint().getEndpointName();
            } else {
                ComponentNameSpace id = me.getRole() == Role.PROVIDER ? me.getDestinationId() : me.getSourceId();
                destination = INBOUND_PREFIX + id.getContainerName();
            }
            Queue queue = inboundSession.createQueue(destination);
            ObjectMessage msg = inboundSession.createObjectMessage(me);
            queueProducer.send(queue, msg);
        } catch (JMSException e) {
            log.error("Failed to send exchange: " + me + " internal JMS Network", e);
            throw new MessagingException(e);
        }
    }

    /**
     * MessageListener implementation
     * 
     * @param message
     */
    public void onMessage(final Message message) {
        try {
            if (message != null && started.get()) {
                ObjectMessage objMsg = (ObjectMessage) message;
                final MessageExchangeImpl me = (MessageExchangeImpl) objMsg.getObject();
                // Dispatch the message in another thread so as to free the jms session
                // else if a component do a sendSync into the jms flow, the whole
                // flow is deadlocked 
                broker.getWorkManager().scheduleWork(new Work() {
                    public void release() {
                    }

                    public void run() {
                        try {
                            if (me.getDestinationId() == null) {
                                ServiceEndpoint se = me.getEndpoint();
                                se = broker.getRegistry()
                                        .getInternalEndpoint(se.getServiceName(), se.getEndpointName());
                                me.setEndpoint(se);
                                me.setDestinationId(((InternalEndpoint) se).getComponentNameSpace());
                            }
                            JMSFlow.super.doRouting(me);
                        } catch (Throwable e) {
                            log.error("Caught an exception routing ExchangePacket: ", e);
                        }
                    }
                });
            }
        } catch (JMSException jmsEx) {
            log.error("Caught an exception unpacking JMS Message: ", jmsEx);
        } catch (WorkException e) {
            log.error("Caught an exception routing ExchangePacket: ", e);
        }
    }

    protected void onAdvisoryMessage(Object obj) {
        if (obj instanceof ConsumerInfo) {
            ConsumerInfo info = (ConsumerInfo) obj;
            subscriberSet.add(info.getConsumerId().getConnectionId());
            ServiceEndpoint[] endpoints = broker.getRegistry().getEndpointsForInterface(null);
            for (int i = 0; i < endpoints.length; i++) {
                if (endpoints[i] instanceof InternalEndpoint && ((InternalEndpoint) endpoints[i]).isLocal()) {
                    onInternalEndpointRegistered(new EndpointEvent(endpoints[i],
                            EndpointEvent.INTERNAL_ENDPOINT_REGISTERED), true);
                }
            }
        } else if (obj instanceof RemoveInfo) {
            ConsumerId id = (ConsumerId) ((RemoveInfo) obj).getObjectId();
            subscriberSet.remove(id.getConnectionId());
            removeAllPackets(id.getConnectionId());
        }
    }

    private void removeAllPackets(String containerName) {
        //TODO: broker.getRegistry().unregisterRemoteEndpoints(containerName);
    }
}
