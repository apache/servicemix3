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
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.event.ComponentAdapter;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.event.EndpointAdapter;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.EndpointListener;
import org.apache.servicemix.jbi.framework.ComponentMBeanImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * Use for message routing among a network of containers. All routing/registration happens automatically.
 * 
 * @version $Revision$
 * @org.apache.xbean.XBean element="jmsFlow"
 */
public class JMSFlow extends AbstractFlow implements MessageListener {

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
    
    private ComponentListener componentListener;

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
    public void init(Broker broker) throws JBIException {
        log.debug(broker.getContainer().getName() + ": Initializing jms flow");
        super.init(broker);
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
        // Create and register component listener
        componentListener = new ComponentAdapter() {
            public void componentStarted(ComponentEvent event) {
                onComponentStarted(event);
            }
            public void componentStopped(ComponentEvent event) {
                onComponentStopped(event);
            }
        };
        broker.getContainer().addListener(componentListener);
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
            connection.setClientID(broker.getContainer().getName());
            connection.start();
            inboundSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = inboundSession.createQueue(INBOUND_PREFIX + broker.getContainer().getName());
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
            log.debug(broker.getContainer().getName() + ": Starting jms flow");
            super.start();
            try {
                broadcastConsumer = broadcastSession.createConsumer(broadcastTopic, null, true);
                broadcastConsumer.setMessageListener(new MessageListener() {
                    public void onMessage(Message message) {
                        try {
                            Object obj = ((ObjectMessage) message).getObject();
                            if (obj instanceof EndpointEvent) {
                                EndpointEvent event = (EndpointEvent) obj;
                                String container = ((InternalEndpoint) event.getEndpoint()).getComponentNameSpace().getContainerName();
                                if (!getBroker().getContainer().getName().equals(container)) {
                                    if (event.getEventType() == EndpointEvent.INTERNAL_ENDPOINT_REGISTERED) {
                                        onRemoteEndpointRegistered(event);
                                    } else if (event.getEventType() == EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED) {
                                        onRemoteEndpointUnregistered(event);
                                    }
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
                for (Iterator it = broker.getContainer().getRegistry().getComponents().iterator(); it.hasNext();) {
                    ComponentMBeanImpl cmp = (ComponentMBeanImpl) it.next();
                    if (cmp.isStarted()) {
                        onComponentStarted(new ComponentEvent(cmp, ComponentEvent.COMPONENT_STARTED));
                    }
                }
                // Start queue consumers for all endpoints
                ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
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
            log.debug(broker.getContainer().getName() + ": Stopping jms flow");
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
                log.debug("JMSException caught in stop", e);
            }
        }
    }

    public void shutDown() throws JBIException {
        super.shutDown();
        stop();
        // Remove endpoint listener
        broker.getContainer().removeListener(endpointListener);
        // Remove component listener
        broker.getContainer().removeListener(componentListener);
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
            String key = EndpointSupport.getKey(event.getEndpoint());
            if (!consumerMap.containsKey(key)) {
                Queue queue = inboundSession.createQueue(INBOUND_PREFIX + key);
                MessageConsumer consumer = inboundSession.createConsumer(queue);
                consumer.setMessageListener(this);
                consumerMap.put(key, consumer);
            }
            if (broadcast) {
                log.debug(broker.getContainer().getName() + ": broadcasting info for " + event);
                ObjectMessage msg = broadcastSession.createObjectMessage(event);
                topicProducer.send(msg);
            }
        } catch (Exception e) {
            log.error("Cannot create consumer for " + event.getEndpoint(), e);
        }
    }

    public void onInternalEndpointUnregistered(EndpointEvent event, boolean broadcast) {
        try {
            String key = EndpointSupport.getKey(event.getEndpoint());
            MessageConsumer consumer = (MessageConsumer) consumerMap.remove(key);
            if (consumer != null) {
                consumer.close();
            }
            if (broadcast) {
                ObjectMessage msg = broadcastSession.createObjectMessage(event);
                log.debug(broker.getContainer().getName() + ": broadcasting info for " + event);
                topicProducer.send(msg);
            }
        } catch (Exception e) {
            log.error("Cannot destroy consumer for " + event, e);
        }
    }
    
    public void onComponentStarted(ComponentEvent event) {
        if (!started.get()) {
            return;
        }
        try {
            String key = event.getComponent().getName();
            if (!consumerMap.containsKey(key)) {
                Queue queue = inboundSession.createQueue(INBOUND_PREFIX + key);
                MessageConsumer consumer = inboundSession.createConsumer(queue);
                consumer.setMessageListener(this);
                consumerMap.put(key, consumer);
            }
        } catch (Exception e) {
            log.error("Cannot create consumer for component " + event.getComponent().getName(), e);
        }
    }
    
    public void onComponentStopped(ComponentEvent event) {
        try {
            String key = event.getComponent().getName();
            MessageConsumer consumer = (MessageConsumer) consumerMap.remove(key);
            if (consumer != null) {
                consumer.close();
            }
        } catch (Exception e) {
            log.error("Cannot destroy consumer for component " + event.getComponent().getName(), e);
        }
    }

    public void onRemoteEndpointRegistered(EndpointEvent event) {
        log.debug(broker.getContainer().getName() + ": adding remote endpoint: " + event.getEndpoint());
        broker.getContainer().getRegistry().registerRemoteEndpoint(event.getEndpoint());
    }

    public void onRemoteEndpointUnregistered(EndpointEvent event) {
        log.debug(broker.getContainer().getName() + ": removing remote endpoint: " + event.getEndpoint());
        broker.getContainer().getRegistry().unregisterRemoteEndpoint(event.getEndpoint());
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
                if (me.getDestinationId() == null) {
                    destination = INBOUND_PREFIX + EndpointSupport.getKey(me.getEndpoint());
                } else if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_PROVIDER)) && !isSynchronous(me)) {
                    destination = INBOUND_PREFIX + me.getDestinationId().getName();
                } else {
                    destination = INBOUND_PREFIX + me.getDestinationId().getContainerName();
                }
            } else {
                if (me.getSourceId() == null) {
                    throw new IllegalStateException("No sourceId set on the exchange");
                } else if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_CONSUMER)) && !isSynchronous(me)) {
                    // If the consumer is stateless and has specified a sender endpoint,
                    // this exchange will be sent to the given endpoint queue, so that
                    // fail-over and load-balancing can be achieved
                    // This property must have been created using EndpointSupport.getKey
                    if (me.getProperty(JbiConstants.SENDER_ENDPOINT) != null) {
                        destination = INBOUND_PREFIX + me.getProperty(JbiConstants.SENDER_ENDPOINT);
                    } else {
                        destination = INBOUND_PREFIX + me.getSourceId().getName();
                    }
                } else {
                    destination = INBOUND_PREFIX + me.getSourceId().getContainerName();
                }
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
                broker.getContainer().getWorkManager().scheduleWork(new Work() {
                    public void release() {
                    }

                    public void run() {
                        try {
                            if (me.getDestinationId() == null) {
                                ServiceEndpoint se = me.getEndpoint();
                                se = broker.getContainer().getRegistry()
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
            ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
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
