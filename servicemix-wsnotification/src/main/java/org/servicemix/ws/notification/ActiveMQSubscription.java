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

package org.servicemix.ws.notification;

import org.activemq.ActiveMQConnection;
import org.activemq.ActiveMQMessageConsumer;
import org.activemq.service.Service;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryExpressionType;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

public class ActiveMQSubscription implements Service {

    private final ActiveMQConnection connection;

    private EndpointReferenceType consumerReference;
    private TopicExpressionConverter topicConverter;
    private Session session;
    private ActiveMQMessageConsumer consumer;

    private QueryExpressionType selector;
    private QueryExpressionType precondition;
    private Object subscriptionPolicy;
    private TopicExpressionType topicExpression;
    private boolean useNotify;
    private XMLGregorianCalendar terminationTime;
    private MessageListener dispatcher;

    public ActiveMQSubscription(ActiveMQConnection connection, EndpointReferenceType consumerReference,
            TopicExpressionConverter topicConverter) {
        this.connection = connection;
        this.consumerReference = consumerReference;
        this.topicConverter = topicConverter;
    }

    public void start() throws JMSException {
        if (session == null) {
            if (dispatcher == null) {
                throw new IllegalArgumentException("Must specify a dispatcher");
            }
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            consumer = (ActiveMQMessageConsumer) session.createConsumer(
                    topicConverter.toActiveMQTopic(topicExpression), toJMSSelector(selector));
            consumer.setMessageListener(dispatcher);
        }
        consumer.start();
    }

    public void stop() throws JMSException {
        if (session != null) {
            consumer.stop();
        }
    }

    public void close() throws JMSException {
        if (session != null) {
            session.close();
            session = null;
        }
    }

    public ActiveMQConnection getConnection() {
        return connection;
    }

    public EndpointReferenceType getConsumerReference() {
        return consumerReference;
    }

    public MessageListener getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(MessageListener dispatcher) {
        this.dispatcher = dispatcher;
    }

    public QueryExpressionType getSelector() {
        return selector;
    }

    public void setSelector(QueryExpressionType selector) {
        this.selector = selector;
    }

    public QueryExpressionType getPrecondition() {
        return precondition;
    }

    public void setPrecondition(QueryExpressionType precondition) {
        this.precondition = precondition;
    }

    public Object getSubscriptionPolicy() {
        return subscriptionPolicy;
    }

    public void setSubscriptionPolicy(Object subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }

    public TopicExpressionType getTopicExpression() {
        return topicExpression;
    }

    public void setTopicExpression(TopicExpressionType topicExpression) {
        this.topicExpression = topicExpression;
    }

    public boolean isUseNotify() {
        return useNotify;
    }

    public void setUseNotify(boolean useNotify) {
        this.useNotify = useNotify;
    }

    public XMLGregorianCalendar getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(XMLGregorianCalendar terminationTime) {
        this.terminationTime = terminationTime;
    }

    public GetResourcePropertyResponse getResourceProperty(EndpointReferenceType resource, QName request) {
        GetResourcePropertyResponse answer = new GetResourcePropertyResponse();
        return answer;
    }

    protected static String toJMSSelector(QueryExpressionType selector) {
        if (selector != null && isValidSelectorDialect(selector.getDialect())) {
            for (Object item : selector.getContent()) {
                if (item instanceof String) {
                    return (String) item;
                }
            }
        }
        return null;
    }

    protected static boolean isValidSelectorDialect(String dialect) {
        return true;
    }

}
