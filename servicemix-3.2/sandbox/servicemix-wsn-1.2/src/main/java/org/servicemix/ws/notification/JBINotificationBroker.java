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

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jms.JMSException;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.activemq.ActiveMQConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.servicemix.ws.notification.invoke.InvokerSupport;
import org.servicemix.ws.notification.invoke.JBIInvoker;
import org.xmlsoap.schemas.ws._2003._03.addressing.AttributedQName;
import org.xmlsoap.schemas.ws._2003._03.addressing.EndpointReferenceType;
import org.xmlsoap.schemas.ws._2003._03.addressing.ServiceNameType;

/**
 * A JBI component which supports both inbound JBI messages to be dispatched to
 * WS-Notification and which dispatches WS-Notification messages into the JBI
 * container to dispatch to the endpoint consumer.
 * 
 * @version $Revision$
 */
@WebService(endpointInterface = "org.servicemix.wspojo.notification.NotificationBroker")
public class JBINotificationBroker extends ActiveMQNotificationBroker implements ComponentLifeCycle {
	
    private static final transient Log log = LogFactory.getLog(JBINotificationBroker.class);

    private ComponentContext context;

    public JBINotificationBroker() throws JMSException {
    }

    public JBINotificationBroker(String url) throws JMSException {
        super(url);
    }

    public JBINotificationBroker(ActiveMQConnectionFactory factory) throws JMSException {
        super(factory);
    }

    @WebMethod(exclude=true)
    public void init(ComponentContext context) throws JBIException {
        this.context = context;
    }

    @WebMethod(exclude=true)
    public void shutDown() throws JBIException {
    }

    @WebMethod(exclude=true)
    public void start() throws JBIException {
    }

    @WebMethod(exclude=true)
    public void stop() throws JBIException {
    }

    @WebMethod(exclude=true)
    public ObjectName getExtensionMBeanName() {
    	return null;
    }
    
    protected InvokerSupport createDispatcher(ActiveMQSubscription subscribe) {
        try {
            ServiceEndpoint endpoint = findSubscriberEndpoint(subscribe);
            if (endpoint == null) {
                throw new NotificationException(
                        "Could not find a suitable JBI ServiceEndpoint reference for the consumerReference: "
                                + subscribe.getConsumerReference());
            }
            JBIInvoker invoker = new JBIInvoker(context.getDeliveryChannel(), endpoint, subscribe);
            return invoker;
        }
        catch (MessagingException e) {
            throw new NotificationException(e);
        }
    }

    /**
     * Extracts the subscriber endpoint from the subscription request using the
     * serviceName or the portType
     */
    protected ServiceEndpoint findSubscriberEndpoint(ActiveMQSubscription subscribe) {
        EndpointReferenceType consumerReference = subscribe.getConsumerReference();
        if (consumerReference == null) {
            throw new NotificationException("No consumerReference specified for subscription: " + subscribe);
        }
        ServiceNameType serviceNameType = consumerReference.getServiceName();
        if (serviceNameType == null) {
            log.warn("No service name available for subscription: " + subscribe);
        }
        else {
            QName serviceName = serviceNameType.getValue();
            ServiceEndpoint[] endpoints = context.getEndpointsForService(serviceName);
            if (endpoints != null && endpoints.length > 0) {
                // lets just return the first
                return endpoints[0];
            }
        }
        AttributedQName portTypeType = consumerReference.getPortType();
        if (portTypeType != null) {
            QName portType = portTypeType.getValue();
            ServiceEndpoint[] endpoints = context.getEndpoints(portType);
            if (endpoints != null && endpoints.length > 0) {
                // lets just return the first
                return endpoints[0];
            }
        }

        // TODO try resolve the endpoint?

        return null;
    }

    public ComponentContext getContext() {
        return context;
    }

    public void setContext(ComponentContext context) {
        this.context = context;
    }
}
