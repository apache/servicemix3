/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.notification.invoke;

import org.activemq.command.ActiveMQTopic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.servicemix.ws.notification.TopicExpressionConverter;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * A base class for a JMS consumer which is capable of dispatching messages to
 * some WS endpoint.
 * 
 * @version $Revision$
 */
public abstract class InvokerSupport implements MessageListener {
    private static final transient Log log = LogFactory.getLog(NotificationConsumerInvoker.class);

    private TopicExpressionConverter topicConverter = new TopicExpressionConverter();
    private EndpointReferenceType producerReference;

    public void onMessage(Message msg) {
        try {
            TopicExpressionType topic = extractTopic(msg);
            dispatchMessage(topic, msg);
        }
        catch (Throwable e) {
            log.error("Caught exception trying to dispatch message: " + e, e);
        }
    }

    public EndpointReferenceType getProducerReference() {
        return producerReference;
    }

    public void setProducerReference(EndpointReferenceType producerReference) {
        this.producerReference = producerReference;
    }

    public TopicExpressionConverter getTopicConverter() {
        return topicConverter;
    }

    public void setTopicConverter(TopicExpressionConverter topicConverter) {
        this.topicConverter = topicConverter;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected TopicExpressionType extractTopic(Message msg) throws JMSException {
        return topicConverter.toTopicExpression((ActiveMQTopic) msg.getJMSDestination());
    }

    protected abstract void dispatchMessage(TopicExpressionType topic, Message message) throws Exception;
}
