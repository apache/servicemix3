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

import java.lang.reflect.Constructor;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Use for message routing among a network of containers. All
 * routing/registration happens automatically.
 * 
 * @version $Revision$
 * @org.apache.xbean.XBean element="jmsFlowTibco"
 */
public class JMSFlowTibco extends AbstractJMSFlow {

    private static final String TOPIC_NAME_MONITOR_CONSUMER = "$sys.monitor.consumer.*";

    private static final String PROPERTY_NAME_EVENT_CLASS = "event_class";

    private static final String PROPERTY_NAME_TARGET_DEST_NAME = "target_dest_name";

    private static final String PROPERTY_NAME_CONN_CONNID = "conn_connid";

    private static final String EVENT_CLASS_CONSUMER_CREATE = "consumer.create";

    protected ConnectionFactory createConnectionFactoryFromUrl(String jmsURL) {
        try {
            Class connFactoryClass = Class.forName("com.tibco.tibjms.TibjmsConnectionFactory");
            if (jmsURL != null) {
                Constructor cns = connFactoryClass.getConstructor(new Class[] {String.class });
                return (ConnectionFactory) cns.newInstance(new Object[] {jmsURL });
            } else {
                return (ConnectionFactory) connFactoryClass.newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create Tibco connection factory", e);
        }
        /*
        return (jmsURL != null) ? new com.tibco.tibjms.TibjmsConnectionFactory.TibjmsConnectionFactory(jmsURL) : 
                                  new com.tibco.tibjms.TibjmsConnectionFactory.TibjmsConnectionFactory();
        */
    }

    public void onConsumerMonitorMessage(Message message) {
        if (!started.get()) {
            return;
        }
        try {
            String connectionId = "" + message.getLongProperty(PROPERTY_NAME_CONN_CONNID);
            String targetDestName = message.getStringProperty(PROPERTY_NAME_TARGET_DEST_NAME);
            String eventClass = message.getStringProperty(PROPERTY_NAME_EVENT_CLASS);
            if (getBroadcastDestinationName().equals(targetDestName)) {
                if (EVENT_CLASS_CONSUMER_CREATE.equals(eventClass)) {
                    addClusterNode(connectionId);
                } else {
                    removeClusterNode(connectionId);
                }
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void startConsumerMonitor() throws JMSException {
        Session broadcastSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic createTopic = broadcastSession.createTopic(TOPIC_NAME_MONITOR_CONSUMER);
        monitorMessageConsumer = broadcastSession.createConsumer(createTopic);
        monitorMessageConsumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                onConsumerMonitorMessage(message);
            }
        });
    }

}
