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
import org.activemq.advisories.ProducerDemandAdvisor;
import org.activemq.advisories.ProducerDemandEvent;
import org.activemq.advisories.ProducerDemandListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.GetCurrentMessage;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryExpressionType;
import org.servicemix.jbi.jaxp.SourceTransformer;
import org.servicemix.wspojo.notification.InvalidResourcePropertyQNameFault;
import org.servicemix.wspojo.notification.InvalidTopicExpressionFault;
import org.servicemix.wspojo.notification.NoCurrentMessageOnTopicFault;
import org.servicemix.wspojo.notification.NotificationProducer;
import org.servicemix.wspojo.notification.ResourceUnknownFault;
import org.servicemix.wspojo.notification.SubscribeCreationFailedFault;
import org.servicemix.wspojo.notification.TopicNotSupportedFault;
import org.servicemix.wspojo.notification.TopicPathDialectUnknownFault;
import org.w3c.dom.Node;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jws.WebParam;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;

public class ActiveMQPublisherRegistration {

    private static final transient Log log = LogFactory.getLog(ActiveMQPublisherRegistration.class);

    private final ActiveMQConnection connection;
    private TopicExpressionConverter topicConverter = new TopicExpressionConverter();
    private boolean demand = true;
    private EndpointReferenceType endpointReference;
    private EndpointReferenceType publisherReference;
    private Topic topic;
    private ProducerDemandAdvisor advisor;
    private Boolean useNotify;
    private QueryExpressionType precondition;
    private QueryExpressionType selector;
    private Object subscriptionPolicy;
    private XMLGregorianCalendar terminationTime;

    public ActiveMQPublisherRegistration(ActiveMQConnection connection) {
        this.connection = connection;
    }

    public void notify(Topic topic, String text) throws IOException, JMSException {
        this.topic = topic;
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            MessageProducer producer = session.createProducer(topic);
            Message message = session.createTextMessage(text);
            producer.send(message);

        }
        finally {
            session.close();
        }
    }

    public void notify(Topic topic, byte[] bytes) throws IOException, JMSException {
        this.topic = topic;
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            MessageProducer producer = session.createProducer(topic);

            BytesMessage bytesMessage = session.createBytesMessage();
            bytesMessage.writeBytes(bytes);
            producer.send(bytesMessage);

        }
        finally {
            session.close();
        }
    }

    public void notify(Topic topic, MessageFactory factory) throws IOException, JMSException {
        this.topic = topic;
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        try {
            MessageProducer producer = session.createProducer(topic);

            Message message = factory.createMessage(session);
            producer.send(message);

        }
        finally {
            session.close();
        }
    }

    public void notify(Topic topic, Object object) throws JMSException, IOException, TransformerException {
    	if (object instanceof Node) {
    		SourceTransformer st = new SourceTransformer();
    		String s = st.toString((Node) object);
            notify(topic, s);
    	} 
    	else if (object instanceof String) {
            notify(topic, (String) object);
        }
        else if (object instanceof Serializable) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            try {
                MessageProducer producer = session.createProducer(topic);

                Message message = session.createObjectMessage((Serializable) object);
                producer.send(message);

            }
            finally {
                session.close();
            }
        }
        else {
            throw new JMSException("Cannot convert: " + object + " into a JMS body");
        }
    }

    public void start() throws JMSException {
        if (demand) {
            advisor = new ProducerDemandAdvisor(connection, topic);
            advisor.setDemandListener(new ProducerDemandListener() {
                public void onEvent(ProducerDemandEvent event) {
                    try {
                        fireDemandChangeEvent(event.isInDemand());
                    }
                    catch (Exception e) {
                        log.error("Failed to perform on demand subscription: " + e, e);
                    }
                }
            });
            advisor.start();
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public boolean isDemand() {
        return demand;
    }

    public void setDemand(boolean demand) {
        this.demand = demand;
    }

    public EndpointReferenceType getPublisherReference() {
        return publisherReference;
    }

    public void setPublisherReference(EndpointReferenceType publisherReference) {
        this.publisherReference = publisherReference;
    }

    public EndpointReferenceType getEndpointReference() {
        return endpointReference;
    }

    public void setEndpointReference(EndpointReferenceType endpointReference) {
        this.endpointReference = endpointReference;
    }

    public Boolean getUseNotify() {
        return useNotify;
    }

    public void setUseNotify(Boolean useNotify) {
        this.useNotify = useNotify;
    }

    public QueryExpressionType getPrecondition() {
        return precondition;
    }

    public void setPrecondition(QueryExpressionType precondition) {
        this.precondition = precondition;
    }

    public QueryExpressionType getSelector() {
        return selector;
    }

    public void setSelector(QueryExpressionType selector) {
        this.selector = selector;
    }

    public Object getSubscriptionPolicy() {
        return subscriptionPolicy;
    }

    public void setSubscriptionPolicy(Object subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }

    public XMLGregorianCalendar getTerminationTime() {
        return terminationTime;
    }

    public void setTerminationTime(XMLGregorianCalendar terminationTime) {
        this.terminationTime = terminationTime;
    }

    public TopicExpressionConverter getTopicConverter() {
        return topicConverter;
    }

    public void setTopicConverter(TopicExpressionConverter topicConverter) {
        this.topicConverter = topicConverter;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void fireDemandChangeEvent(boolean inDemand) throws RemoteException, SubscribeCreationFailedFault,
            ResourceUnknownFault, TopicPathDialectUnknownFault {
        NotificationProducer producer = createPublisherNotificationProducer();
        if (inDemand) {
            producer.subscribe(publisherReference, topicConverter.toTopicExpression(topic), useNotify, precondition,
                    selector, subscriptionPolicy, terminationTime);
        }
        else {

            // TODO how to unsubscribe?
        }
    }

    protected NotificationProducer createPublisherNotificationProducer() {
        return new NotificationProducer() {
            public EndpointReferenceType subscribe(
                    @WebParam(name = "ConsumerReference", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    EndpointReferenceType consumerReference,
                    @WebParam(name = "TopicExpression", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    TopicExpressionType topicExpression,
                    @WebParam(name = "UseNotify", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    Boolean useNotify,
                    @WebParam(name = "Precondition", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    QueryExpressionType precondition,
                    @WebParam(name = "Selector", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    QueryExpressionType selector,
                    @WebParam(name = "SubscriptionPolicy", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    Object subscriptionPolicy,
                    @WebParam(name = "InitialTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    XMLGregorianCalendar initialTerminationTime) throws SubscribeCreationFailedFault,
                    ResourceUnknownFault, TopicPathDialectUnknownFault {
                throw new RuntimeException("Not implemented");
            }

            public GetCurrentMessageResponse getCurrentMessage(
                    @WebParam(name = "GetCurrentMessage", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    GetCurrentMessage getCurrentMessageRequest) throws TopicNotSupportedFault,
                    InvalidTopicExpressionFault, NoCurrentMessageOnTopicFault, ResourceUnknownFault {
                throw new RuntimeException("Not implemented");
            }

            public GetResourcePropertyResponse getResourceProperty(
                    @WebParam(name = "GetResourceProperty", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
                    QName getResourcePropertyRequest) throws InvalidResourcePropertyQNameFault, ResourceUnknownFault {
                throw new RuntimeException("Not implemented");
            }
        };
    }

    public void stop() throws JMSException {
        if (advisor != null) {
            advisor.stop();
            advisor = null;
        }
    }

}
