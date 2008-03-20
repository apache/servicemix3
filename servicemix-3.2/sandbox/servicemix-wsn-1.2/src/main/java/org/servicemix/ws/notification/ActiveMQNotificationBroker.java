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
package org.servicemix.ws.notification;

import org.activemq.ActiveMQConnection;
import org.activemq.ActiveMQConnectionFactory;
import org.activemq.command.ActiveMQTopic;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.GetCurrentMessage;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.NotificationMessageHolderType;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf._2004._06.wsrf_ws_resourceproperties_1_2_draft_01.QueryExpressionType;
import org.servicemix.ws.notification.invoke.InvokerSupport;
import org.servicemix.ws.notification.invoke.NotificationConsumerInvoker;
import org.servicemix.wspojo.notification.InvalidResourcePropertyQNameFault;
import org.servicemix.wspojo.notification.InvalidTopicExpressionFault;
import org.servicemix.wspojo.notification.NoCurrentMessageOnTopicFault;
import org.servicemix.wspojo.notification.NotificationBroker;
import org.servicemix.wspojo.notification.NotificationConsumer;
import org.servicemix.wspojo.notification.PublisherRegistrationFailedFault;
import org.servicemix.wspojo.notification.PublisherRegistrationManager;
import org.servicemix.wspojo.notification.ResourceUnknownFault;
import org.servicemix.wspojo.notification.SubscribeCreationFailedFault;
import org.servicemix.wspojo.notification.SubscriptionManager;
import org.servicemix.wspojo.notification.TopicNotSupportedFault;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;

import javax.jms.JMSException;
import javax.jms.Topic;
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import java.io.IOException;
import java.util.List;

/**
 * An implementation of {@link NotificationBroker} which uses ActiveMQ  as the underlying messaging system.
 * 
 * @version $Revision$
 */
@WebService(endpointInterface = "org.servicemix.wspojo.notification.NotificationBroker")
public class ActiveMQNotificationBroker implements NotificationBroker {

    private ActiveMQPublisherRegistrationManager publisherManager = new ActiveMQPublisherRegistrationManager();
    private ActiveMQSubscriptionManager subscriptionManager = new ActiveMQSubscriptionManager();
    private TopicExpressionConverter topicConverter = new TopicExpressionConverter();

    private ActiveMQConnectionFactory factory;
    private ActiveMQConnection connection;
    private ActiveMQPublisherRegistration anonymousPublisher;

    public ActiveMQNotificationBroker() throws JMSException {
        this("vm://localhost");
    }

    public ActiveMQNotificationBroker(String url) throws JMSException {
        this(new ActiveMQConnectionFactory(url));
    }

    public ActiveMQNotificationBroker(ActiveMQConnectionFactory factory) throws JMSException {
        this.factory = factory;
        connection = (ActiveMQConnection) factory.createConnection();
        connection.start();

        anonymousPublisher = new ActiveMQPublisherRegistration(connection);

        /**
         * TODO NotificationBrokerRPDocument document =
         * NotificationBrokerRPDocument.Factory.newInstance();
         * resourceProperties = document.addNewNotificationBrokerRP();
         * resourceProperties.setFixedTopicSet(false);
         * resourceProperties.setRequiresRegistration(false);
         * resourceProperties.setTopicExpressionDialectsArray(new
         * String[]{"ActiveMQ"}); xmlResourceProperties = new
         * XmlObjectResourceProperties(resourceProperties);
         */
    }

    // Properties
    // -------------------------------------------------------------------------
    public ActiveMQConnection getConnection() {
        return connection;
    }

    public PublisherRegistrationManager getPublisherManager() {
        return publisherManager;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public TopicExpressionConverter getTopicConverter() {
        return topicConverter;
    }

    public void setTopicConverter(TopicExpressionConverter topicConverter) {
        this.topicConverter = topicConverter;
    }

    // NotificationBroker interface
    // -------------------------------------------------------------------------

    @WebMethod(action = "http://servicemix.org/wspojo/notification/Notify", operationName = "Notify")
    @Oneway
    @RequestWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Notify", localName = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    public void notify(
            @WebParam(name = "NotificationMessage", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            List<NotificationMessageHolderType> list) {

        try {
            for (NotificationMessageHolderType messageHolder : list) {
                Topic topic = topicConverter.toActiveMQTopic(messageHolder.getTopic());
                EndpointReferenceType producerReference = messageHolder.getProducerReference();
                ActiveMQPublisherRegistration publisher = getPublisher(producerReference);
                publisher.notify(topic, messageHolder.getMessage());
            }
        }
        catch (TransformerException e) {
            throw new NotificationException(e);
        }
        catch (JMSException e) {
            throw new NotificationException(e);
        }
        catch (IOException e) {
            throw new NotificationException(e);
        }
    }

    @WebMethod(action = "http://servicemix.org/wspojo/notification/GetResourceProperty", operationName = "GetResourceProperty")
    @WebResult(name = "GetResourcePropertyResponse", partName = "GetResourcePropertyResponse", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public GetResourcePropertyResponse getResourceProperty(
            @WebParam(name = "GetResourceProperty", partName = "GetResourcePropertyRequest", targetNamespace = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd")
            QName qName) throws InvalidResourcePropertyQNameFault, ResourceUnknownFault {
        GetResourcePropertyResponse answer = new GetResourcePropertyResponse();
        /** TODO */
        return answer;
    }

    @WebMethod(action = "http://servicemix.org/wspojo/notification/Subscribe", operationName = "Subscribe")
    @WebResult(name = "SubscriptionReference", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    @RequestWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Subscribe", localName = "Subscribe", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    @ResponseWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.SubscribeResponse", localName = "SubscribeResponse", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
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
            XMLGregorianCalendar terminationTime) throws ResourceUnknownFault, SubscribeCreationFailedFault {

        terminationTime = validateTerminationTime(terminationTime);

        ActiveMQSubscription subscription;

        try {

            subscription = new ActiveMQSubscription(connection, consumerReference, topicConverter);
            subscription.setTopicExpression(topicExpression);
            subscription.setUseNotify(useNotify);
            subscription.setPrecondition(precondition);
            subscription.setSelector(selector);
            subscription.setSubscriptionPolicy(subscriptionPolicy);
            subscription.setTerminationTime(terminationTime);
            subscription.setDispatcher(createDispatcher(subscription));
            subscription.start();

        }
        catch (JMSException e) {
            throw new NotificationException(e);
        }

        return subscriptionManager.register(subscription);
    }

    @WebMethod(action = "http://servicemix.org/wspojo/notification/GetCurrentMessage", operationName = "GetCurrentMessage")
    @WebResult(name = "GetCurrentMessageResponse", partName = "GetCurrentMessageResponse", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public GetCurrentMessageResponse getCurrentMessage(
            @WebParam(name = "GetCurrentMessage", partName = "GetCurrentMessageRequest", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            GetCurrentMessage getCurrentMessage) throws InvalidTopicExpressionFault, NoCurrentMessageOnTopicFault,
            ResourceUnknownFault, TopicNotSupportedFault {
        throw new NotificationException("Not supported");
    }

    @WebMethod(action = "http://servicemix.org/wspojo/notification/RegisterPublisher", operationName = "RegisterPublisher")
    @WebResult(name = "PublisherRegistrationReference", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
    @RequestWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_brokerednotification_1_2_draft_01.RegisterPublisher", localName = "RegisterPublisher", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
    @ResponseWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_brokerednotification_1_2_draft_01.RegisterPublisherResponse", localName = "RegisterPublisherResponse", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
    public EndpointReferenceType registerPublisher(
            @WebParam(name = "PublisherReference", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
            EndpointReferenceType publisherReference,
            @WebParam(name = "Topic", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
            List<TopicExpressionType> list,
            @WebParam(name = "Demand", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
            Boolean demand,
            @WebParam(name = "InitialTerminationTime", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BrokeredNotification-1.2-draft-01.xsd")
            XMLGregorianCalendar terminationTime) throws InvalidTopicExpressionFault, PublisherRegistrationFailedFault,
            ResourceUnknownFault, TopicNotSupportedFault {

        // Check request.
        ActiveMQTopic topic = topicConverter.toActiveMQTopic(list);

        terminationTime = validateTerminationTime(terminationTime);

        // Create publisher and assoicate an EndpointReference with it.
        EndpointReferenceType registrationEndpointReference = null;
        try {

            ActiveMQPublisherRegistration publisher = new ActiveMQPublisherRegistration(connection);
            registrationEndpointReference = publisherManager.register(publisher);
            publisher.setEndpointReference(registrationEndpointReference);

            publisher.setTerminationTime(terminationTime);
            publisher.setDemand(demand);

            if (publisher.isDemand()) {
                publisher.setPublisherReference(publisherReference);
            }

            publisher.setTopic(topic);
            publisher.start();

        }
        catch (JMSException e) {
            throw new NotificationException(e);
        }
        return registrationEndpointReference;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    protected ActiveMQPublisherRegistration getPublisher(EndpointReferenceType producerReference) {
        ActiveMQPublisherRegistration publisher = null;
        if (producerReference != null) {
            publisher = publisherManager.getProducer(producerReference);
        }
        if (publisher == null) {
            publisher = anonymousPublisher;
        }
        return publisher;
    }

    protected XMLGregorianCalendar validateTerminationTime(XMLGregorianCalendar terminationTime) {
        if (terminationTime != null) {
            if (isInThePast(terminationTime)) {
                throw new NotificationException("Termination time cannot be in the past.");
            }
        }
        else {
            // We could default a sensible timeout here.
        }
        return terminationTime;
    }

    protected boolean isInThePast(XMLGregorianCalendar terminationTime) {
        // TODO implement!
        return false;
    }

    protected InvokerSupport createDispatcher(ActiveMQSubscription subscribe) throws NotificationException {
        return new NotificationConsumerInvoker(createNotificationConsumer(subscribe));
    }

    protected NotificationConsumer createNotificationConsumer(final ActiveMQSubscription consumerReference) {
        return new NotificationConsumer() {
            @WebMethod(operationName = "Notify")
            @Oneway
            @RequestWrapper(className = "org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.Notify", localName = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
            public void notify(
                    @WebParam(name = "NotificationMessage", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                    List<NotificationMessageHolderType> list) {

                System.out.println("WS invoke not yet implemented");
                System.out.println("Target: " + consumerReference);
                System.out.println("Notify Message: " + list);
            }
        };
    }
}
