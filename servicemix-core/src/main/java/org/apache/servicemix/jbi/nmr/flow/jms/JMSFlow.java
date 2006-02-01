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

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

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
import org.apache.servicemix.jbi.framework.ComponentConnector;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.apache.servicemix.jbi.framework.ComponentPacket;
import org.apache.servicemix.jbi.framework.ComponentPacketEvent;
import org.apache.servicemix.jbi.framework.ComponentPacketEventListener;
import org.apache.servicemix.jbi.framework.LocalComponentConnector;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.Broker;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Use for message routing among a network of containers. All routing/registration happens automatically.
 * 
 * @version $Revision$
 */
public class JMSFlow extends AbstractFlow implements MessageListener, ComponentPacketEventListener {
    
    private static final Log log = LogFactory.getLog(JMSFlow.class);
    private static final String INBOUND_PREFIX = "org.apache.servicemix.inbound.";
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
    private Set subscriberSet=new CopyOnWriteArraySet();
    private Map networkNodeKeyMap = new ConcurrentHashMap();
    private Map networkComponentKeyMap = new ConcurrentHashMap();
    private Map consumerMap = new ConcurrentHashMap();
    private AtomicBoolean started = new AtomicBoolean(false);

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
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, String subType) throws JBIException {
        log.info(broker.getContainerName() + ": Initializing jms flow");
        super.init(broker, subType);
        broker.getRegistry().addComponentPacketListener(this);
        try {
            if (connectionFactory == null) {
                if (jmsURL != null) {
                    connectionFactory = new ActiveMQConnectionFactory(jmsURL);
                }
                else {
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
        }
        catch (JMSException e) {
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
                broadcastConsumer.setMessageListener(this);
                Topic advisoryTopic=AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) broadcastTopic);
                advisoryConsumer=broadcastSession.createConsumer(advisoryTopic);
                advisoryConsumer.setMessageListener(this);

                
                // Start queue consumers for all components
                for (Iterator i = broker.getRegistry().getLocalComponentConnectors().iterator();i.hasNext();) {
                    LocalComponentConnector lcc = (LocalComponentConnector) i.next();
                    ComponentPacket packet = lcc.getPacket();
                    ComponentPacketEvent cpe = new ComponentPacketEvent(packet, ComponentPacketEvent.ACTIVATED);
                    onEvent(cpe, false);
                }
            }
            catch (JMSException e) {
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
        if (this.connection != null) {
            try {
                this.connection.close();
            }
            catch (JMSException e) {
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

    /**
     * Process state changes in Components
     * 
     * @param event
     */
    public void onEvent(ComponentPacketEvent event) {
        onEvent(event, true);
    }
    
    /**
     * Process state changes in Components
     * 
     * @param event
     */
    public void onEvent(ComponentPacketEvent event, boolean broadcast) {
        try {
            // broadcast internal changes to the network
            if (started.get() && event.getPacket().getComponentNameSpace().getContainerName().equals(broker.getContainerName())) {
                String componentName = event.getPacket().getComponentNameSpace().getName();
                if (event.getStatus() == ComponentPacketEvent.ACTIVATED) {
                    if (!consumerMap.containsKey(componentName)) {
                        Queue queue = inboundSession.createQueue(INBOUND_PREFIX + componentName);
                        MessageConsumer consumer = inboundSession.createConsumer(queue);
                        consumer.setMessageListener(this);
                        consumerMap.put(componentName,consumer);
                    }
                } else if (event.getStatus() == ComponentPacketEvent.DEACTIVATED) {
                    MessageConsumer consumer = (MessageConsumer) consumerMap.remove(componentName);
                    if (consumer != null){
                        consumer.close();
                    }
                }
                if (broadcast) {
                    ObjectMessage msg = broadcastSession.createObjectMessage(event);
                    log.info(broker.getContainerName() + ": broadcasting info for " + event.getPacket().getComponentNameSpace());
                    topicProducer.send(msg);
                }
            }
        }
        catch (JMSException e) {
            log.error("failed to broadcast to the internal JMS network: " + event, e);
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
    public void doRouting(MessageExchangeImpl me) throws MessagingException{
        ComponentNameSpace id=me.getRole()==Role.PROVIDER?me.getDestinationId():me.getSourceId();
        ComponentConnector cc=broker.getRegistry().getComponentConnector(id);
        if(cc!=null){
            if (me.isTransacted() && me.getMirror().getSyncState() != MessageExchangeImpl.SYNC_STATE_ASYNC) {
                throw new IllegalStateException("transacted sendSync can not be used on jca flow with external components");
            }
            // let ActiveMQ do the routing ...
            try{
                String componentName=cc.getComponentNameSpace().getName();
                String destination = "";
                if (me.getRole() == Role.PROVIDER){
                    destination = INBOUND_PREFIX + componentName;
                }else {
                    destination = INBOUND_PREFIX + id.getContainerName();
                }
                Queue queue=inboundSession.createQueue(destination);
                ObjectMessage msg=inboundSession.createObjectMessage(me);
                queueProducer.send(queue,msg);
            }catch(JMSException e){
                log.error("Failed to send exchange: "+me+" internal JMS Network",e);
                throw new MessagingException(e);
            }
        }else{
            throw new MessagingException("No component with id ("+id+") - Couldn't route MessageExchange "+me);
        }
    }

    /**
     * MessageListener implementation
     * 
     * @param message
     */
    public void onMessage(Message message) {
        if (!started.get() || message == null) {
            return;
        }
        try {
            if (message instanceof ObjectMessage) {
                ObjectMessage objMsg = (ObjectMessage) message;
                Object obj = objMsg.getObject();
                if (obj != null) {
                    if (obj instanceof ComponentPacketEvent) {
                        ComponentPacketEvent event = (ComponentPacketEvent) obj;
                        String containerName = event.getPacket().getComponentNameSpace().getContainerName();
                        processInBoundPacket(containerName, event);
                    }
                    else if (obj instanceof MessageExchangeImpl) {
                        final MessageExchangeImpl me = (MessageExchangeImpl) obj;
                        // Dispatch the message in another thread so as to free the jms session
                        // else if a component do a sendSync into the jms flow, the whole
                        // flow is deadlocked 
                        broker.getWorkManager().scheduleWork(new Work() {
                            public void release() {
                            }
                            public void run() {
                                try {
                                    JMSFlow.super.doRouting(me);
                                }
                                catch (MessagingException e) {
                                    log.error("Caught an exception routing ExchangePacket: ", e);
                                }
                            }
                        });
                    }
                }
            } else if (message instanceof ActiveMQMessage) {
                Object obj = ((ActiveMQMessage) message).getDataStructure();
                if(obj instanceof ConsumerInfo){
                    ConsumerInfo info=(ConsumerInfo) obj;
                    subscriberSet.add(info.getConsumerId().getConnectionId());
                    for(Iterator i=broker.getRegistry().getLocalComponentConnectors().iterator();i.hasNext();){
                        LocalComponentConnector lcc=(LocalComponentConnector) i.next();
                        ComponentPacket packet=lcc.getPacket();
                        ComponentPacketEvent cpe=new ComponentPacketEvent(packet,ComponentPacketEvent.ACTIVATED);
                        onEvent(cpe);
                    }
                }else if(obj instanceof RemoveInfo){
                    ConsumerId id=(ConsumerId) ((RemoveInfo) obj).getObjectId();
                    subscriberSet.remove(id.getConnectionId());
                    removeAllPackets(id.getConnectionId());
                }
            }
        }
        catch (JMSException jmsEx) {
            log.error("Caught an exception unpacking JMS Message: ", jmsEx);
        }
        catch (WorkException e) {
            log.error("Caught an exception routing ExchangePacket: ", e);
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
            int eventStatus = event.getStatus();
            switch (eventStatus) {
                case ComponentPacketEvent.ACTIVATED:
                case ComponentPacketEvent.STATE_CHANGE:
                    updateRemotePacket(containerName, packet);
                    break;
                case ComponentPacketEvent.DEACTIVATED:
                    removeRemotePacket(containerName, packet);
                    break;
                default:
                    log.warn("Unable to determine ComponentPacketEvent type: " + eventStatus + " for packet: " + packet);
            }         	
        }
    }

    private void updateRemotePacket(String containerName, ComponentPacket packet) {
        Set set = (Set) networkNodeKeyMap.get(containerName);
        if (set != null) {
            set.remove(packet);
            set.add(packet);
        }
        ComponentConnector cc = new ComponentConnector(packet);
        log.info(broker.getContainerName() + ": updating remote component: " + cc);
        broker.getRegistry().updateRemoteComponentConnector(cc);
    }

    private void removeRemotePacket(String containerName, ComponentPacket packet) {
        networkComponentKeyMap.remove(packet.getComponentNameSpace());
        Set set = (Set) networkNodeKeyMap.get(containerName);
        if (set != null) {
            set.remove(packet);
            ComponentConnector cc = new ComponentConnector(packet);
            log.info(broker.getContainerName() + ": removing remote component: " + cc);
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
	            log.info(broker.getContainerName() + ": Network node: " + containerName + " Stopped. Removing remote Component: " + cc);
	            broker.getRegistry().removeRemoteComponentConnector(cc);
	            networkComponentKeyMap.remove(packet.getComponentNameSpace());
	        }
        }
    }
}
