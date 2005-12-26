/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.apache.servicemix.jbi.nmr.flow.jca;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;


import org.activemq.advisory.AdvisorySupport;
import org.activemq.command.ActiveMQDestination;
import org.activemq.command.ActiveMQTopic;
import org.activemq.command.ConsumerId;
import org.activemq.command.ConsumerInfo;
import org.activemq.command.RemoveInfo;
import org.activemq.ra.ActiveMQActivationSpec;
import org.activemq.ra.ActiveMQManagedConnectionFactory;
import org.activemq.ra.ActiveMQResourceAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.BootstrapContextImpl;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.servicemix.jbi.framework.ComponentConnector;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.framework.ComponentPacket;
import org.apache.servicemix.jbi.framework.ComponentPacketEvent;
import org.apache.servicemix.jbi.framework.ComponentPacketEventListener;
import org.apache.servicemix.jbi.framework.LocalComponentConnector;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.jencks.JCAConnector;
import org.jencks.SingletonEndpointFactory;
import org.jencks.factory.ConnectionManagerFactoryBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Use for message routing among a network of containers. All routing/registration happens automatically.
 * 
 * @version $Revision$
 */
public class JCAFlow extends AbstractFlow implements  MessageListener, ComponentPacketEventListener {
    
    private static final Log log = LogFactory.getLog(JCAFlow.class);
    private static final String INBOUND_PREFIX = "org.apache.servicemix.inbound.";
    private String jmsURL = "tcp://localhost:61616";
    private String userName;
    private String password;
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private String broadcastDestinationName = "org.apache.servicemix.JMSFlow";
    private Topic broadcastTopic;
    private Map networkNodeKeyMap = new ConcurrentHashMap();
    private Map networkComponentKeyMap = new ConcurrentHashMap();
    private Map connectorMap = new ConcurrentHashMap();
    private AtomicBoolean started = new AtomicBoolean(false);
    private Set subscriberSet=new CopyOnWriteArraySet();
    private TransactionContextManager transactionContextManager;
    private ConnectionManager connectionManager;
    private JmsTemplate jmsTemplate;
    private JmsTemplate jmsPersistentTemplate;
    private BootstrapContext bootstrapContext;
    private ResourceAdapter resourceAdapter;
    private JCAConnector containerConnector;
    private JCAConnector broadcastConnector;
    private Session broadcastSession;
    private Topic advisoryTopic;
    private MessageConsumer advisoryConsumer;

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
     * @param jmsURL The jmsURL to set.
     */
    public void setJmsURL(String jmsURL) {
        this.jmsURL = jmsURL;
    }

    /**
     * Returns the password for this flow
     * 
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for this flow
     * 
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the User Name for this flow
     * 
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the User Name for this flow
     * 
     * @param userName The userName to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the ConnectionFactory for this flow
     * 
     * @return Returns the connectionFactory.
     */
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Sets the ConnectionFactory for this flow
     * 
     * @param connectionFactory The connectionFactory to set.
     */
    public void setConnectionFactory(ConnectionFactory connectoFactory) {
        this.connectionFactory = connectoFactory;
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
     * @param broadcastDestinationName The broadcastDestinationName to set.
     */
    public void setBroadcastDestinationName(String broadcastDestinationName) {
        this.broadcastDestinationName = broadcastDestinationName;
    }

    protected ResourceAdapter createResourceAdapter() throws ResourceAdapterInternalException {
    	ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
    	ra.setServerUrl(jmsURL);
    	ra.start(getBootstrapContext());
    	return ra;
    }
    
    public TransactionManager getTransactionManager() {
    	return (TransactionManager) broker.getContainer().getTransactionManager();
    }
    
    /**
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, String subType) throws JBIException {
        super.init(broker, subType);
        broker.getRegistry().addComponentPacketListener(this);
        try {
        	resourceAdapter = createResourceAdapter();
        	
        	// Inbound connector
        	ActiveMQActivationSpec ac = new ActiveMQActivationSpec();
        	ac.setDestinationType("javax.jms.Queue");
        	ac.setDestination(INBOUND_PREFIX + broker.getContainerName());
        	containerConnector = new JCAConnector();
        	containerConnector.setBootstrapContext(getBootstrapContext());
        	containerConnector.setActivationSpec(ac);
        	containerConnector.setResourceAdapter(resourceAdapter);
        	containerConnector.setEndpointFactory(new SingletonEndpointFactory(this, getTransactionManager()));
        	containerConnector.afterPropertiesSet();
        	
        	// Outbound connector
        	ActiveMQManagedConnectionFactory mcf = new ActiveMQManagedConnectionFactory();
        	mcf.setResourceAdapter(resourceAdapter);
        	ConnectionFactory cf = (ConnectionFactory) mcf.createConnectionFactory(getConnectionManager());
        	jmsTemplate = new JmsTemplate(cf);
        	jmsTemplate.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        	jmsPersistentTemplate = new JmsTemplate(cf);
        	jmsPersistentTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        	
        	// Inbound broadcast
        	ac = new ActiveMQActivationSpec();
        	ac.setDestinationType("javax.jms.Topic");
        	ac.setDestination(broadcastDestinationName);
        	broadcastConnector = new JCAConnector();
        	broadcastConnector.setBootstrapContext(getBootstrapContext());
        	broadcastConnector.setActivationSpec(ac);
        	broadcastConnector.setResourceAdapter(resourceAdapter);
        	broadcastConnector.setEndpointFactory(new SingletonEndpointFactory(this));
        	broadcastConnector.afterPropertiesSet();
        	
        	// Outbound broadcast
        	connection = ((ActiveMQResourceAdapter) resourceAdapter).makeConnection();
        	connection.start();
        	broadcastTopic = new ActiveMQTopic(broadcastDestinationName);
            
        broadcastSession=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        broadcastTopic = new ActiveMQTopic(broadcastDestinationName);
        advisoryTopic=AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) broadcastTopic);
            
        }
        catch (Exception e) {
            log.error("Failed to initialize JCAFlow", e);
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
                advisoryConsumer=broadcastSession.createConsumer(advisoryTopic);
                advisoryConsumer.setMessageListener(this);
            }
            catch (JMSException e) {
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
                advisoryConsumer.close();
            }
            catch (JMSException e) {
                JBIException jbiEx = new JBIException("JMSException caught in stop: " + e.getMessage());
                throw jbiEx;
            }
        }
    }

    public void shutDown() throws JBIException {
        super.shutDown();
        stop();
        // Destroy connectors
        while (!connectorMap.isEmpty()) {
        	JCAConnector connector = (JCAConnector) connectorMap.remove(connectorMap.keySet().iterator().next());
        	try {
        		connector.destroy();
        	} catch (Exception e) {
        		log.warn("error closing jca connector", e);
        	}
        }
        try {
        	containerConnector.destroy();
    	} catch (Exception e) {
    		log.warn("error closing jca connector", e);
        }
        try {
        	broadcastConnector.destroy();
    	} catch (Exception e) {
    		log.warn("error closing jca connector", e);
        }
        // Destroy the resource adapter
    	resourceAdapter.stop();
        if (this.connection != null) {
            try {
                this.connection.close();
            }
            catch (JMSException e) {
                log.warn("error closing JMS Connection", e);
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

    /**
     * Ability for this flow to persist exchanges.
     * 
     * @return <code>true</code> if this flow can persist messages
     */
    protected boolean canPersist() {
    	return true;
    }

    /**
     * Process state changes in Components
     * 
     * @param event
     */
    public void onEvent(final ComponentPacketEvent event){
        try{
            String componentName=event.getPacket().getComponentNameSpace().getName();
            if(event.getStatus()==ComponentPacketEvent.ACTIVATED){
                if(!connectorMap.containsKey(componentName)){
                    ActiveMQActivationSpec ac=new ActiveMQActivationSpec();
                    ac.setDestinationType("javax.jms.Queue");
                    ac.setDestination(INBOUND_PREFIX+componentName);
                    JCAConnector connector=new JCAConnector();
                    connector.setBootstrapContext(getBootstrapContext());
                    connector.setActivationSpec(ac);
                    connector.setResourceAdapter(resourceAdapter);
                    connector.setEndpointFactory(new SingletonEndpointFactory(this,getTransactionManager()));
                    connector.afterPropertiesSet();
                    connectorMap.put(componentName,connector);
                }
            }else if(event.getStatus()==ComponentPacketEvent.DEACTIVATED){
                JCAConnector connector=(JCAConnector) connectorMap.remove(componentName);
                if(connector!=null){
                    connector.destroy();
                }
            }
            // broadcast change to the network
            log.info("broadcast to internal JMS network: "+event);
            jmsTemplate.send(broadcastTopic,new MessageCreator(){
                public Message createMessage(Session session) throws JMSException{
                    return session.createObjectMessage(event);
                }
            });
        }catch(Exception e){
            log.error("failed to broadcast to the internal JMS network: "+event,e);
        }
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
        
        ComponentNameSpace id = me.getRole() == Role.PROVIDER ? me.getDestinationId() : me.getSourceId();
        ComponentConnector cc = broker.getRegistry().getComponentConnector(id);
        if (cc != null) {
        	if (me.getMirror().getSyncState() != MessageExchangeImpl.SYNC_STATE_ASYNC) {
        		throw new IllegalStateException("sendSync can not be used on jca flow with external components");
        	}
            try {
                final String componentName = cc.getComponentNameSpace().getName();
                JmsTemplate jt = isPersistent(me) ? jmsPersistentTemplate : jmsTemplate;
                String destination = "";
                if (me.getRole() == Role.PROVIDER){
                    destination = INBOUND_PREFIX + componentName;
                }else {
                    destination = INBOUND_PREFIX + id.getContainerName();
                }
                jt.send(destination, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
	                    return session.createObjectMessage(me);
					}
				});
            } catch (Exception e) {
                log.error("Failed to send exchange: " + me + " internal JMS Network", e);
                throw new MessagingException(e);
            }
        } else {
            throw new MessagingException("No component with id (" + id + ") - Couldn't route MessageExchange " + me);
        }
    }

    /**
     * MessageListener implementation
     * 
     * @param message
     */
    public void onMessage(Message message) {
        try {
            if (message != null && message instanceof ObjectMessage) {
                ObjectMessage objMsg = (ObjectMessage) message;
                Object obj = objMsg.getObject();
                if (obj != null) {
                    if (obj instanceof ComponentPacketEvent) {
                        ComponentPacketEvent event = (ComponentPacketEvent) obj;
                        String containerName = event.getPacket().getComponentNameSpace().getContainerName();
                        processInBoundPacket(containerName, event);
                    }
                    else if (obj instanceof MessageExchangeImpl) {
                        MessageExchangeImpl me = (MessageExchangeImpl) obj;
                        TransactionManager tm = (TransactionManager) getTransactionManager();
                        if (tm != null) {
                            me.setTransactionContext(tm.getTransaction());
                        }
                        super.doRouting(me);
                    }else if(obj instanceof ConsumerInfo){
                        ConsumerInfo info=(ConsumerInfo) obj;
                        subscriberSet.add(info.getConsumerId().getConnectionId());
                        if(started.get()){
                            for(Iterator i=broker.getRegistry().getLocalComponentConnectors().iterator();i.hasNext();){
                                LocalComponentConnector lcc=(LocalComponentConnector) i.next();
                                ComponentPacket packet=lcc.getPacket();
                                ComponentPacketEvent cpe=new ComponentPacketEvent(packet,ComponentPacketEvent.ACTIVATED);
                                onEvent(cpe);
                            }
                        }
                    }else if(obj instanceof RemoveInfo){
                        ConsumerId id=(ConsumerId) ((RemoveInfo) obj).getObjectId();
                        subscriberSet.remove(id.getConnectionId());
                        removeAllPackets(id.getConnectionId());
                    }
                }
            }
        }
        catch (JMSException jmsEx) {
            log.error("Caught an exception unpacking JMS Message: ", jmsEx);
        }
        catch (MessagingException e) {
            log.error("Caught an exception routing ExchangePacket: ", e);
        } 
        catch (SystemException e) {
            log.error("Caught an exception acessing transaction context: ", e);
		}
    }

    /**
     * Process Inbound packets
     * 
     * @param containerName
     * @param event
     */
    protected void processInBoundPacket(String containerName, ComponentPacketEvent event) {
        ComponentPacket packet = event.getPacket();
        if (!packet.getComponentNameSpace().getContainerName().equals(broker.getContainerName())) {
        	log.info("received from internal JMS network: " + event);
            int eventStatus = event.getStatus();
            switch (eventStatus) {
                case ComponentPacketEvent.ACTIVATED:
                    addRemotePacket(containerName, packet);
                    break;
                case ComponentPacketEvent.DEACTIVATED:
                    removeRemotePacket(containerName, packet);
                    break;
                case ComponentPacketEvent.STATE_CHANGE:
                    updateRemotePacket(containerName, packet);
                    break;
                default:
                    log.warn("Unable to determine ComponentPacketEvent type: " + eventStatus + " for packet: " + packet);
            }         	
        }
    }

    private void addRemotePacket(String containerName, ComponentPacket packet) {
        networkComponentKeyMap.put(packet.getComponentNameSpace(), containerName);
        Set set = (Set) networkNodeKeyMap.get(containerName);
        if (set == null) {
            set = new CopyOnWriteArraySet();
            networkNodeKeyMap.put(containerName, set);
        }
        ComponentConnector cc = new ComponentConnector(packet);
        log.info("Adding Remote Component: " + cc);
        broker.getRegistry().addRemoteComponentConnector(cc);
        set.add(packet);
    }

    private void updateRemotePacket(String containerName, ComponentPacket packet) {
        Set set = (Set) networkNodeKeyMap.get(containerName);
        if (set != null) {
            set.remove(packet);
            set.add(packet);
        }
        ComponentConnector cc = new ComponentConnector(packet);
        log.info("Updating remote Component: " + cc);
        broker.getRegistry().updateRemoteComponentConnector(cc);
    }

    private void removeRemotePacket(String containerName, ComponentPacket packet) {
        networkComponentKeyMap.remove(packet.getComponentNameSpace());
        Set set = (Set) networkNodeKeyMap.get(containerName);
        if (set != null) {
            set.remove(packet);
            ComponentConnector cc = new ComponentConnector(packet);
            log.info("Removing remote Component: " + cc);
            broker.getRegistry().removeRemoteComponentConnector(cc);
            if (set.isEmpty()) {
                networkNodeKeyMap.remove(containerName);
            }
        }
    }

    private void removeAllPackets(String containerName) {
        Set set = (Set) networkNodeKeyMap.remove(containerName);
        if (set != null) {
	        for (Iterator i = set.iterator();i.hasNext();) {
	            ComponentPacket packet = (ComponentPacket) i.next();
	            ComponentConnector cc = new ComponentConnector(packet);
	            log.info("Network node: " + containerName + " Stopped. Removing remote Component: " + cc);
	            broker.getRegistry().removeRemoteComponentConnector(cc);
	            networkComponentKeyMap.remove(packet.getComponentNameSpace());
	        }
        }
    }

	public ConnectionManager getConnectionManager() throws Exception {
		if (connectionManager == null) {
        	ConnectionManagerFactoryBean cmfb = new ConnectionManagerFactoryBean();
        	cmfb.setTransactionContextManager(getTransactionContextManager());
        	cmfb.setPoolingSupport(new SinglePool(
        			16, // max size 
        			0, // min size
        			100, // blockingTimeoutMilliseconds
                    1, // idleTimeoutMinutes 
                    true, // matchOne
                    true,  // matchAll
                    true)); // selectOneAssumeMatch
        	cmfb.setTransactionSupport(new XATransactions(
        			true, // useTransactionCaching
        			false)); // useThreadCaching
        	cmfb.afterPropertiesSet();
			connectionManager = (ConnectionManager) cmfb.getObject();
		}
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public TransactionContextManager getTransactionContextManager() {
		return transactionContextManager;
	}

	public void setTransactionContextManager(
			TransactionContextManager transactionContextManager) {
		this.transactionContextManager = transactionContextManager;
	}

	public BootstrapContext getBootstrapContext() {
		if (bootstrapContext == null) {
	    	GeronimoWorkManager wm = (GeronimoWorkManager) broker.getWorkManager();
	    	bootstrapContext = new BootstrapContextImpl(wm);
		}
		return bootstrapContext;
	}

	public void setBootstrapContext(BootstrapContext bootstrapContext) {
		this.bootstrapContext = bootstrapContext;
	}
    
    public String toString(){
        return broker.getContainerName() + " JCAFlow";
    }
}