/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
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

package org.servicemix.jbi.nmr.flow.cluster;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

import org.activecluster.Cluster;
import org.activecluster.ClusterEvent;
import org.activecluster.ClusterException;
import org.activecluster.ClusterFactory;
import org.activecluster.ClusterListener;
import org.activecluster.Node;
import org.activecluster.impl.ActiveMQClusterFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.jbi.framework.ComponentConnector;
import org.servicemix.jbi.framework.ComponentNameSpace;
import org.servicemix.jbi.framework.ComponentPacket;
import org.servicemix.jbi.framework.ComponentPacketEvent;
import org.servicemix.jbi.framework.LocalComponentConnector;
import org.servicemix.jbi.messaging.MessageExchangeImpl;
import org.servicemix.jbi.nmr.Broker;
import org.servicemix.jbi.nmr.flow.seda.SedaFlow;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Use for message routing among a cluster of containers. 
 * All routing/cluster registration happens automatically.
 * 
 * A cluster Flow is used for cases where you need collaboration between 
 * more than one ServiceMix JBIContainer (for fail-over or scalability).
 * Component deployment happens in the same way as a normal SericeMix JBI 
 * container (both for POJO and archive Component deployment) but all the 
 * containers in the cluster are notified of a deployment, and the Cluster 
 * Flow will handle automatic routing (and failover) of MessageExchange(s) 
 * between the members of the cluster.
 * 
 * @version $Revision$
 */
public class ClusterFlow extends SedaFlow implements ClusterListener, MessageListener {
    private static final Log log = LogFactory.getLog(ClusterFlow.class);
    private String clusterDestination = ActiveMQClusterFactory.DEFAULT_CLUSTER_URL;
    private Cluster cluster;
    private Map clusterNodeKeyMap = new ConcurrentHashMap();
    private Map clusterComponentKeyMap = new ConcurrentHashMap();

    /**
     * The type of Flow
     * 
     * @return the type
     */
    public String getDescription() {
        return "cluster";
    }

    /**
     * Initialize the Region
     * 
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, String subType) throws JBIException {
        super.init(broker, subType);
        ClusterFactory fac = new ActiveMQClusterFactory();
        try {
            this.cluster = fac.createCluster(clusterDestination);
            this.cluster.addClusterListener(this);
            MessageConsumer consumer = this.cluster.createConsumer(cluster.getDestination());
            consumer.setMessageListener(this);
            consumer = this.cluster.createConsumer(cluster.getLocalNode().getDestination());
            consumer.setMessageListener(this);
        }
        catch (ClusterException e) {
            JBIException jbiEx = new JBIException("ClusterException caught in init: " + e.getMessage());
            throw jbiEx;
        }
        catch (JMSException e) {
            JBIException jbiEx = new JBIException("JMSException caught in init: " + e.getMessage());
            throw jbiEx;
        }
    }

    /**
     * start the flow
     * 
     * @throws JBIException
     */
    public void start() throws JBIException {
        super.start();
        try {
            cluster.start();
        }
        catch (JMSException e) {
            JBIException jbiEx = new JBIException("JMSException caught in start: " + e.getMessage());
            throw jbiEx;
        }
    }

    /**
     * stop the flow
     * 
     * @throws JBIException
     */
    public void stop() throws JBIException {
        super.stop();
        try {
            cluster.stop();
        }
        catch (JMSException e) {
            JBIException jbiEx = new JBIException("JMSException caught in stop: " + e.getMessage());
            throw jbiEx;
        }
    }

    /**
     * shutDown the flow
     * 
     * @throws JBIException
     */
    public void shutDown() throws JBIException {
        super.shutDown();
    }

    /**
     * Utility for testing
     * 
     * @param expectedCount
     * @param timeout
     * @return true if cluster has expeged number of nodes
     * @throws JBIException
     */
    public boolean waitForClusterToComplete(int expectedCount, long timeout) throws JBIException {
        try {
            return cluster.waitForClusterToComplete(expectedCount, timeout);
        }
        catch (InterruptedException e) {
            throw new JBIException("Interupted: " + e.getMessage());
        }
    }

    /**
     * useful for testing
     * 
     * @return number of containers in the cluster
     */
    public int numberInCluster() {
        return cluster.getNodes().size();
    }

    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws JBIException
     */
    protected void doSend(MessageExchangeImpl me) throws JBIException {
    	enqueuePacket(me);
    }

    /**
     * Process state changes in Components
     * 
     * @param event
     */
    public void onEvent(ComponentPacketEvent event) {
        super.onEvent(event);
        try {
            // broadcast change to the cluster
            ObjectMessage msg = cluster.createObjectMessage(event);
            msg.setJMSReplyTo(cluster.getLocalNode().getDestination());
            cluster.send(cluster.getDestination(), msg);
            log.info("broadcast to cluster: " + event);
        }
        catch (JMSException e) {
            log.error("failed to boradcast to the cluster: " + event, e);
        }
    }

    /**
     * @return Returns the clusterDestination.
     */
    public String getClusterDestination() {
        return clusterDestination;
    }

    /**
     * @param clusterDestination The clusterDestination to set.
     */
    public void setClusterDestination(String clusterDestination) {
        this.clusterDestination = clusterDestination;
    }

    /**
     * Cluster Listener Implementation
     */
    /**
     * A new node has been added
     * 
     * @param event
     */
    public void onNodeAdd(ClusterEvent event) {
        // send all registered Components to the remote cluster node
        for (Iterator i = broker.getRegistry().getLocalComponentConnectors().iterator();i.hasNext();) {
            LocalComponentConnector lcc = (LocalComponentConnector) i.next();
            ComponentPacket packet = lcc.getPacket();
            ComponentPacketEvent cpe = new ComponentPacketEvent(packet, ComponentPacketEvent.ACTIVATED);
            onEvent(cpe);
        }
    }

    /**
     * A node has updated its state
     * 
     * @param event
     */
    public void onNodeUpdate(ClusterEvent event) {
    }

    /**
     * A node has been removed (a clean shutdown)
     * 
     * @param event
     */
    public void onNodeRemoved(ClusterEvent event) {
        Node node = event.getNode();
        removeAllPackets(node.getDestination());
    }

    /**
     * A node has failed due to process or network failure
     * 
     * @param event
     */
    public void onNodeFailed(ClusterEvent event) {
        Node node = event.getNode();
        removeAllPackets(node.getDestination());
    }

    /**
     * An election has occurred and a new coordinator has been selected
     * 
     * @param event
     */
    public void onCoordinatorChanged(ClusterEvent event) {
    }

    /** ************End ClusterListener implementation************************** */
    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws MessagingException
     */
    public void doRouting(MessageExchangeImpl me) throws MessagingException {
        ComponentNameSpace id = me.getRole() == Role.PROVIDER ? me.getDestinationId() : me.getSourceId();
        ComponentConnector cc = broker.getRegistry().getLoadBalancedComponentConnector(id);
        if (cc != null) {
            if (cc.isLocal()) {
                super.doRouting(me);
            }
            else {
                Destination destination = (Destination) clusterComponentKeyMap.get(id);
                if (destination != null) {
                    ObjectMessage msg;
                    try {
                        msg = cluster.createObjectMessage(me);
                        msg.setJMSReplyTo(cluster.getLocalNode().getDestination());
                        cluster.send(cluster.getDestination(), msg);
                    }
                    catch (JMSException e) {
                        log.error("Could not send cluster message", e);
                        throw new MessagingException(e);
                    }
                }
                else {
                    throw new MessagingException("No remote component with id (" + id
                            + ") exists - Couldn't route ExchangePacket " + me);
                }
            }
        }
        else {
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
                Destination replyTo = message.getJMSReplyTo();
                if (replyTo != null && !replyTo.equals(cluster.getLocalNode().getDestination())) {
                    ObjectMessage objMsg = (ObjectMessage) message;
                    Object obj = objMsg.getObject();
                    if (obj != null) {
                        if (obj instanceof ComponentPacketEvent) {
                            ComponentPacketEvent event = (ComponentPacketEvent) obj;
                            processInBoundPacket(replyTo, event);
                        }
                        else if (obj instanceof MessageExchangeImpl) {
                            MessageExchangeImpl me = (MessageExchangeImpl) obj;
                            super.doRouting(me);
                        }
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
    }

    /**
     * Process Inbound packets
     * 
     * @param nodeName
     * @param event
     */
    protected void processInBoundPacket(Destination nodeName, ComponentPacketEvent event) {
        ComponentPacket packet = event.getPacket();
        if (!packet.getComponentNameSpace().getContainerName().equals(broker.getContainerName())) {
            if (event.getStatus() == ComponentPacketEvent.ACTIVATED) {
                addRemotePacket(nodeName, packet);
            }
            else if (event.getStatus() == ComponentPacketEvent.DEACTIVATED) {
                removeRemotePacket(nodeName, packet);
            }
            else if (event.getStatus() == ComponentPacketEvent.STATE_CHANGE) {
                updateRemotePacket(nodeName, packet);
            }
            else {
                log.warn("Unable to determine ComponentPacketEvent type: " + event.getStatus() + " for packet: "
                        + packet);
            }
        }
    }

    private void addRemotePacket(Destination nodeName, ComponentPacket packet) {
        clusterComponentKeyMap.put(packet.getComponentNameSpace(), nodeName);
        Set set = (Set) clusterNodeKeyMap.get(nodeName);
        if (set == null) {
            set = new CopyOnWriteArraySet();
            clusterNodeKeyMap.put(nodeName, set);
            ComponentConnector cc = new ComponentConnector(packet);
            log.info("Adding Remote Component: " + cc);
            broker.getRegistry().addRemoteComponentConnector(cc);
        }
        set.add(packet);
    }

    private void updateRemotePacket(Destination nodeName, ComponentPacket packet) {
        Set set = (Set) clusterNodeKeyMap.get(nodeName);
        if (set != null) {
            set.remove(packet);
            set.add(packet);
        }
        ComponentConnector cc = new ComponentConnector(packet);
        log.info("Updating remote Component: " + cc);
        broker.getRegistry().updateRemoteComponentConnector(cc);
    }

    private void removeRemotePacket(Destination nodeName, ComponentPacket packet) {
        clusterComponentKeyMap.remove(packet.getComponentNameSpace());
        Set set = (Set) clusterNodeKeyMap.get(nodeName);
        if (set != null) {
            set.remove(packet);
            ComponentConnector cc = new ComponentConnector(packet);
            log.info("Removing remote Component: " + cc);
            broker.getRegistry().removeRemoteComponentConnector(cc);
            if (set.isEmpty()) {
                clusterNodeKeyMap.remove(nodeName);
            }
        }
    }

    private void removeAllPackets(Destination nodeName) {
        Set set = (Set) clusterNodeKeyMap.remove(nodeName);
        if (set != null) {
	        for (Iterator i = set.iterator();i.hasNext();) {
	            ComponentPacket packet = (ComponentPacket) i.next();
	            ComponentConnector cc = new ComponentConnector(packet);
	            log.info("Cluster node: " + nodeName + " Stopped. Removing remote Component: " + cc);
	            broker.getRegistry().removeRemoteComponentConnector(cc);
	            clusterComponentKeyMap.remove(packet.getComponentNameSpace());
	        }
        }
    }
}