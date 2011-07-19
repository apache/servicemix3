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
package org.apache.servicemix.jbi.nmr.flow.jca;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.resource.ResourceException;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.activemq.ra.ActiveMQActivationSpec;
import org.apache.activemq.ra.ActiveMQManagedConnectionFactory;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.geronimo.transaction.manager.NamedXAResource;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.executors.Executor;
import org.apache.servicemix.executors.ExecutorFactory;
import org.apache.servicemix.executors.WorkManagerWrapper;
import org.apache.servicemix.jbi.event.ComponentAdapter;
import org.apache.servicemix.jbi.event.ComponentEvent;
import org.apache.servicemix.jbi.event.ComponentListener;
import org.apache.servicemix.jbi.event.EndpointAdapter;
import org.apache.servicemix.jbi.event.EndpointEvent;
import org.apache.servicemix.jbi.event.EndpointListener;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.apache.servicemix.jbi.servicedesc.EndpointSupport;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
import org.jencks.SingletonEndpointFactory;
import org.jencks.factory.ConnectionManagerFactoryBean;


/**
 * Use for message routing among a network of containers. All
 * routing/registration happens automatically.
 * 
 * @version $Revision$
 * @org.apache.xbean.XBean element="jcaFlow"
 */
public class JCAFlow extends AbstractFlow implements MessageListener {

    private static final String INBOUND_PREFIX = "org.apache.servicemix.jca.";

    private String jmsURL = "tcp://localhost:61616";
    private ActiveMQConnectionFactory connectionFactory;
    private ConnectionFactory managedConnectionFactory;
    private String broadcastDestinationName = "org.apache.servicemix.JCAFlow";
    private ActiveMQTopic broadcastTopic;
    private Map<String, Connector> connectorMap = new ConcurrentHashMap<String, Connector>();
    private AtomicBoolean started = new AtomicBoolean(false);
    private Set<String> subscriberSet = new CopyOnWriteArraySet<String>();
    private ConnectionManager connectionManager;
    private Connector containerConnector;
    private Connector broadcastConnector;
    private Connector advisoryConnector;
    private ActiveMQTopic advisoryTopic;
    private EndpointListener endpointListener;
    private ComponentListener componentListener;
    private String userName;
    private String password;

    public JCAFlow() {
    }

    public JCAFlow(String jmsURL) {
        this.jmsURL = jmsURL;
    }

    /**
     * The type of Flow
     * 
     * @return the type
     */
    public String getDescription() {
        return "jca";
    }

    /**
     * Returns the JMS URL for this flow
     * 
     * @return Returns the jmsURL.
     */
    public String getJmsURL() {
        return jmsURL;
    }

    /**
     * Sets the JMS URL for this flow
     * 
     * @param jmsURL
     *            The jmsURL to set.
     */
    public void setJmsURL(String jmsURL) {
        this.jmsURL = jmsURL;
    }

    /**
     * Returns the ConnectionFactory for this flow
     * 
     * @return Returns the connectionFactory.
     */
    public ActiveMQConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets the ConnectionFactory for this flow
     * 
     * @param connectionFactory
     *            The connectionFactory to set.
     */
    public void setConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Returns the Broadcast Destination Name for this flow
     * 
     * @return Returns the broadcastDestinationName.
     */
    public String getBroadcastDestinationName() {
        return broadcastDestinationName;
    }

    /**
     * Sets the Broadcast Destination Name for this flow
     * 
     * @param broadcastDestinationName
     *            The broadcastDestinationName to set.
     */
    public void setBroadcastDestinationName(String broadcastDestinationName) {
        this.broadcastDestinationName = broadcastDestinationName;
    }

    public TransactionManager getTransactionManager() {
        return (TransactionManager) broker.getContainer().getTransactionManager();
    }

    /**
     *
     * @return Returns the password used for the JMS connections.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password to use in JMS connections.
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     *
     * @return Returns the userName used for the JMS connections.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the userName to use in JMS connections.
     *
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker) throws JBIException {
        LOGGER.debug(broker.getContainer().getName() + ": Initializing jca flow");
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
                connectionFactory = new ActiveMQConnectionFactory(jmsURL);
            }

            // Inbound connector
            ActiveMQDestination dest = new ActiveMQQueue(INBOUND_PREFIX + broker.getContainer().getName());
            containerConnector = new Connector(dest, this, true);
            containerConnector.start();

            // Outbound connector
            ActiveMQResourceAdapter outboundRa = new ActiveMQResourceAdapter();
            outboundRa.setConnectionFactory(connectionFactory);
            //
            // We need to explicitly set the server url unless we use the
            // default jms url, so set it.
            //
            if (outboundRa.getInfo().getServerUrl() == null) {
                LOGGER.info("ActiveMQResourceAdapter server url was null.  Setting it to: " + jmsURL);
                outboundRa.getInfo().setServerUrl(jmsURL);
            }
            ActiveMQManagedConnectionFactory mcf = new ActiveMQManagedConnectionFactory();
            mcf.setResourceAdapter(outboundRa);
            managedConnectionFactory = (ConnectionFactory) mcf.createConnectionFactory(getConnectionManager());

            // Inbound broadcast
            broadcastTopic = new ActiveMQTopic(broadcastDestinationName);
            advisoryTopic = AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) broadcastTopic);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize JCAFlow", e);
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
            super.start();
            try {
                // Inbound broadcast
                MessageListener listener = new MessageListener() {
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
                            LOGGER.error("Error processing incoming broadcast message", e);
                        }
                    }
                };
                broadcastConnector = new Connector(broadcastTopic, listener, false);
                broadcastConnector.start();

                listener = new MessageListener() {
                    public void onMessage(Message message) {
                        if (started.get()) {
                            onAdvisoryMessage(((ActiveMQMessage) message).getDataStructure());
                        }
                    }
                };
                advisoryConnector = new Connector(advisoryTopic, listener, false);
                advisoryConnector.start();
            } catch (Exception e) {
                throw new JBIException("JMSException caught in start: " + e.getMessage(), e);
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
            super.stop();
            try {
                broadcastConnector.stop();
            } catch (Exception e) {
                LOGGER.debug("Error closing jca connector", e);
            }
            try {
                advisoryConnector.stop();
            } catch (Exception e) {
                LOGGER.debug("Error closing jca connector", e);
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
        // Destroy connectors
        while (!connectorMap.isEmpty()) {
            Connector connector = connectorMap.remove(connectorMap.keySet().iterator().next());
            try {
                connector.stop();
            } catch (Exception e) {
                LOGGER.debug("Error closing jca connector", e);
            }
        }
        try {
            containerConnector.stop();
        } catch (Exception e) {
            LOGGER.debug("Error closing jca connector", e);
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

    /**
     * Check if the flow can support the requested QoS for this exchange
     * 
     * @param me
     *            the exchange to check
     * @return true if this flow can handle the given exchange
     */
    public boolean canHandle(MessageExchange me) {
        if (isSynchronous(me)) {
            return false;
        }
        return true;
    }

    public void onInternalEndpointRegistered(EndpointEvent event, boolean broadcast) {
        if (!started.get()) {
            return;
        }
        try {
            String key = EndpointSupport.getKey(event.getEndpoint());
            if (!connectorMap.containsKey(key)) {
                ActiveMQDestination dest = new ActiveMQQueue(INBOUND_PREFIX + key);
                Connector connector = new Connector(dest, this, true);
                connector.start();
                connectorMap.put(key, connector);
            }
            // broadcast change to the network
            if (broadcast) {
                LOGGER.debug(broker.getContainer().getName() + ": broadcasting info for " + event);
                sendJmsMessage(broadcastTopic, event, false, false);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot create consumer for " + event.getEndpoint(), e);
        }
    }

    public void onInternalEndpointUnregistered(EndpointEvent event, boolean broadcast) {
        try {
            String key = EndpointSupport.getKey(event.getEndpoint());
            Connector connector = connectorMap.remove(key);
            if (connector != null) {
                connector.stop();
            }
            // broadcast change to the network
            if (broadcast) {
                LOGGER.debug(broker.getContainer().getName() + ": broadcasting info for " + event);
                sendJmsMessage(broadcastTopic, event, false, false);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot destroy consumer for " + event, e);
        }
    }

    public void onComponentStarted(ComponentEvent event) {
        if (!started.get()) {
            return;
        }
        try {
            String key = event.getComponent().getName();
            if (!connectorMap.containsKey(key)) {
                ActiveMQDestination dest = new ActiveMQQueue(INBOUND_PREFIX + key);
                Connector connector = new Connector(dest, this, true);
                connector.start();
                connectorMap.put(key, connector);
            }
        } catch (Exception e) {
            LOGGER.error("Cannot create consumer for component " + event.getComponent().getName(), e);
        }
    }

    public void onComponentStopped(ComponentEvent event) {
        try {
            String key = event.getComponent().getName();
            Connector connector = connectorMap.remove(key);
            if (connector != null) {
                connector.stop();
            }
        } catch (Exception e) {
            LOGGER.error("Cannot destroy consumer for component " + event.getComponent().getName(), e);
        }
    }

    public void onRemoteEndpointRegistered(EndpointEvent event) {
        LOGGER.debug(broker.getContainer().getName() + ": adding remote endpoint: " + event.getEndpoint());
        broker.getContainer().getRegistry().registerRemoteEndpoint(event.getEndpoint());
    }

    public void onRemoteEndpointUnregistered(EndpointEvent event) {
        LOGGER.debug(broker.getContainer().getName() + ": removing remote endpoint: " + event.getEndpoint());
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
    public void doRouting(final MessageExchangeImpl me) throws MessagingException {
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
                    // If the consumer is stateless and has specified a sender
                    // endpoint,
                    // this exchange will be sent to the given endpoint queue,
                    // so that
                    // This property must have been created using
                    // EndpointSupport.getKey
                    // fail-over and load-balancing can be achieved
                    if (me.getProperty(JbiConstants.SENDER_ENDPOINT) != null) {
                        destination = INBOUND_PREFIX + me.getProperty(JbiConstants.SENDER_ENDPOINT);
                    } else {
                        destination = INBOUND_PREFIX + me.getSourceId().getName();
                    }
                } else {
                    destination = INBOUND_PREFIX + me.getSourceId().getContainerName();
                }
            }
            if (me.isTransacted()) {
                me.setTxState(MessageExchangeImpl.TX_STATE_ENLISTED);
            }
            sendJmsMessage(new ActiveMQQueue(destination), me, isPersistent(me), me.isTransacted());
        } catch (JMSException e) {
            LOGGER.error("Failed to send exchange: " + me + " internal JMS Network", e);
            throw new MessagingException(e);
        } catch (SystemException e) {
            LOGGER.error("Failed to send exchange: " + me + " transaction problem", e);
            throw new MessagingException(e);
        }
    }

    /**
     * MessageListener implementation
     * 
     * @param message
     */
    public void onMessage(Message message) {
        try {
            if (message != null && started.get()) {
                ObjectMessage objMsg = (ObjectMessage) message;
                final MessageExchangeImpl me = (MessageExchangeImpl) objMsg.getObject();
                // Hack for redelivery: AMQ is too optimized and the object is
                // the same upon redelivery
                // so that there are side effect (the exchange state may have
                // been modified)
                // See http://jira.activemq.org/jira/browse/AMQ-519
                // me = (MessageExchangeImpl) ((ActiveMQObjectMessage)
                // ((ActiveMQObjectMessage) message).copy()).getObject();
                TransactionManager tm = (TransactionManager) getTransactionManager();
                if (tm != null) {
                    me.setTransactionContext(tm.getTransaction());
                }
                if (me.getDestinationId() == null) {
                    ServiceEndpoint se = me.getEndpoint();
                    se = broker.getContainer().getRegistry().getInternalEndpoint(se.getServiceName(), se.getEndpointName());
                    me.setEndpoint(se);
                    me.setDestinationId(((InternalEndpoint) se).getComponentNameSpace());
                }
                super.doRouting(me);
            }
        } catch (JMSException jmsEx) {
            LOGGER.error("Caught an exception unpacking JMS Message: ", jmsEx);
        } catch (MessagingException e) {
            LOGGER.error("Caught an exception routing ExchangePacket: ", e);
        } catch (SystemException e) {
            LOGGER.error("Caught an exception acessing transaction context: ", e);
        }
    }

    protected void onAdvisoryMessage(Object obj) {
        if (obj instanceof ConsumerInfo) {
            ConsumerInfo info = (ConsumerInfo) obj;
            subscriberSet.add(info.getConsumerId().getConnectionId());
            ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
            for (int i = 0; i < endpoints.length; i++) {
                if (endpoints[i] instanceof InternalEndpoint && ((InternalEndpoint) endpoints[i]).isLocal()) {
                    onInternalEndpointRegistered(new EndpointEvent(endpoints[i], EndpointEvent.INTERNAL_ENDPOINT_REGISTERED), true);
                }
            }
        } else if (obj instanceof RemoveInfo) {
            ConsumerId id = (ConsumerId) ((RemoveInfo) obj).getObjectId();
            subscriberSet.remove(id.getConnectionId());
            removeAllPackets(id.getConnectionId());
        }
    }

    private void removeAllPackets(String containerName) {
        // TODO: broker.getRegistry().unregisterRemoteEndpoints(containerName);
    }

    public ConnectionManager getConnectionManager() throws Exception {
        if (connectionManager == null) {
            ConnectionManagerFactoryBean cmfb = new ConnectionManagerFactoryBean();
            TransactionManager txmgr = (TransactionManager) broker.getContainer().getTransactionManager();
            if (!(txmgr instanceof RecoverableTransactionManager)) {
                txmgr = new RecoverableTransactionManagerWrapper(txmgr);
            }
            cmfb.setTransactionManager((RecoverableTransactionManager) txmgr);
            cmfb.setTransaction("xa");
            cmfb.afterPropertiesSet();
            connectionManager = (ConnectionManager) cmfb.getObject();
        }
        return connectionManager;
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public String toString() {
        return broker.getContainer().getName() + " JCAFlow";
    }

    private void sendJmsMessage(Destination dest, Serializable object, boolean persistent, boolean transacted) throws JMSException,
                    SystemException {
        if (transacted) {
            TransactionManager tm = (TransactionManager) getBroker().getContainer().getTransactionManager();
            if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                return;
            }
        }
        Connection connection;
        if (userName != null) {
            connection = managedConnectionFactory.createConnection(userName, password);
        } else {
            connection = managedConnectionFactory.createConnection();
        }
        try {
            Session session = connection.createSession(transacted, transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
            ObjectMessage msg = session.createObjectMessage(object);
            MessageProducer producer = session.createProducer(dest);
            producer.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
            producer.send(msg);
        } finally {
            connection.close();
        }
    }

    class Connector {
        private ActiveMQResourceAdapter ra;

        private MessageEndpointFactory endpointFactory;

        private ActiveMQActivationSpec spec;

        private Executor executor;

        public Connector(ActiveMQDestination destination, MessageListener listener, boolean transacted) {
            ra = new ActiveMQResourceAdapter();
            ra.setConnectionFactory(connectionFactory);
            SingletonEndpointFactory ef = new SingletonEndpointFactory(listener, transacted ? getTransactionManager() : null);
            ef.setName(INBOUND_PREFIX + broker.getContainer().getName());
            endpointFactory = ef;
            spec = new ActiveMQActivationSpec();
            spec.setActiveMQDestination(destination);
        }

        public void start() throws ResourceException {
            ExecutorFactory factory = broker.getContainer().getExecutorFactory();
            executor = factory.createExecutor("flow.jca." + spec.getDestination());
            BootstrapContext context = new SimpleBootstrapContext(new WorkManagerWrapper(executor));
            ra.start(context);
            spec.setResourceAdapter(ra);
            ra.endpointActivation(endpointFactory, spec);
        }

        public void stop() throws Exception {
            
            // In some cases these calls on the resource adapter can be blocked, causing
            // servicemix to hang on shutdown.
            FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
                    public Void call() {
                        ra.endpointDeactivation(endpointFactory, spec);
                        ra.stop();
                        executor.shutdown();
                        return null;
                    }
                }
            );
            
            Executors.newFixedThreadPool(1).execute(task);
            task.get(500, TimeUnit.MILLISECONDS);
        }
    }

    class SimpleBootstrapContext implements BootstrapContext {
        private final WorkManager workManager;

        public SimpleBootstrapContext(WorkManager workManager) {
            this.workManager = workManager;
        }

        public Timer createTimer() throws UnavailableException {
            throw new UnsupportedOperationException();
        }

        public WorkManager getWorkManager() {
            return workManager;
        }

        public XATerminator getXATerminator() {
            throw new UnsupportedOperationException();
        }

    }

    public static class RecoverableTransactionManagerWrapper implements RecoverableTransactionManager {
        private final TransactionManager txMgr;

        public RecoverableTransactionManagerWrapper(TransactionManager txMgr) {
            this.txMgr = txMgr;
        }

        public void begin() throws NotSupportedException, SystemException {
            txMgr.begin();
        }

        public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException,
                                    RollbackException, SecurityException, SystemException {
            txMgr.commit();
        }

        public int getStatus() throws SystemException {
            return txMgr.getStatus();
        }

        public Transaction getTransaction() throws SystemException {
            return txMgr.getTransaction();
        }

        public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {
            txMgr.resume(transaction);
        }

        public void rollback() throws IllegalStateException, SecurityException, SystemException {
            txMgr.rollback();
        }

        public void setRollbackOnly() throws IllegalStateException, SystemException {
            txMgr.setRollbackOnly();
        }

        public void setTransactionTimeout(int i) throws SystemException {
            txMgr.setTransactionTimeout(i);
        }

        public Transaction suspend() throws SystemException {
            return txMgr.suspend();
        }

        public void recoveryError(Exception e) {
            throw new UnsupportedOperationException();
        }

        public void recoverResourceManager(NamedXAResource namedXAResource) {
            throw new UnsupportedOperationException();
        }
    }

}
