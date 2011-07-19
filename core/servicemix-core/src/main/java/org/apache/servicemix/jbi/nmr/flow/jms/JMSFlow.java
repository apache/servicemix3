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

import javax.jbi.JBIException;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ConsumerId;
import org.apache.activemq.command.ConsumerInfo;
import org.apache.activemq.command.RemoveInfo;
import org.apache.activemq.pool.PooledConnectionFactory;

/**
 * Use for message routing among a network of containers. All
 * routing/registration happens automatically.
 * 
 * @version $Revision$
 * @org.apache.xbean.XBean element="jmsFlow"
 */
public class JMSFlow extends AbstractJMSFlow {

    private PooledConnectionFactory factory;

    protected ConnectionFactory createConnectionFactoryFromUrl(String jmsURL) {
        factory = (jmsURL != null) ? new PooledConnectionFactory(jmsURL) : new PooledConnectionFactory();
        return factory;
    }

    /**
     * Listener on the ActiveMQ advisory topic so we get messages when a cluster
     * node is added or removed
     */
    protected void onConsumerMonitorMessage(Message advisoryMessage) {
        if (!started.get()) {
            return;
        }
        Object obj = ((ActiveMQMessage) advisoryMessage).getDataStructure();
        if (obj instanceof ConsumerInfo) {
            ConsumerInfo info = (ConsumerInfo) obj;
            addClusterNode(info.getConsumerId().getConnectionId());
        } else if (obj instanceof RemoveInfo) {
            ConsumerId consumerId = (ConsumerId) ((RemoveInfo) obj).getObjectId();
            removeClusterNode(consumerId.getConnectionId());
        }
    }

    public void startConsumerMonitor() throws JMSException {
        Session broadcastSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic broadcastTopic = broadcastSession.createTopic(getBroadcastDestinationName());
        Topic advisoryTopic = AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) broadcastTopic);
        monitorMessageConsumer = broadcastSession.createConsumer(advisoryTopic);
        monitorMessageConsumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                onConsumerMonitorMessage(message);
            }
        });
    }

    @Override
    public void shutDown() throws JBIException {
        super.shutDown();
        if (factory != null) {
            try {
                factory.stop();
            } catch (Exception e) {
                LOGGER.warn("Unable to stop JMS connection pool: " + e.getMessage(), e);
            }
        }
    }
}
