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
package org.apache.servicemix.components.util;

import javax.jbi.component.Component;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.jbi.NoInMessageAvailableException;

/**
 * A useful base class for developers wishing to implement a JBI Component.
 *
 * @version $Revision$
 */
public abstract class ComponentSupport extends PojoSupport implements Component {

    private ComponentLifeCycle lifeCycle;
    private ServiceUnitManager serviceManager;
    private MessageTransformer messageTransformer = CopyTransformer.getInstance();

    protected ComponentSupport() {
    }


    protected ComponentSupport(QName service, String endpoint) {
        super(service, endpoint);
    }

    /**
     * @return the lifecycel control implementation
     */
    public ComponentLifeCycle getLifeCycle() {
        synchronized (this) {
            if (lifeCycle == null) {
                lifeCycle = createComponentLifeCycle();
            }
        }
        return lifeCycle;
    }

    /**
     * @return the ServiceUnitManager or null if there isn't one
     */
    public ServiceUnitManager getServiceUnitManager() {
        initializeServiceUnitManager();
        return serviceManager;
    }

    /**
     * @param fragment
     * @return the description of the specified reference
     */
    public ServiceEndpoint resolveEndpointReference(DocumentFragment fragment) {
        return null;
    }

    /**
     * Retrieves a DOM representation containing metadata which describes the
     * service provided by this component, through the given endpoint. The
     * result can use WSDL 1.1 or WSDL 2.0.
     *
     * @param endpoint the service endpoint.
     * @return the description for the specified service endpoint.
     */

    public Document getServiceDescription(ServiceEndpoint endpoint) {
        return null;
    }

    /**
     * This method is called by JBI to check if this component, in the role of
     * provider of the service indicated by the given exchange, can actually
     * perform the operation desired.
     *
     * @param endpoint the endpoint to be used by the consumer; must be
     *                 non-null.
     * @param exchange the proposed message exchange to be performed; must be
     *                 non-null.
     * @return <code>true</code> if this provider component can perform the
     *         given exchange with the described consumer.
     */
    public boolean isExchangeWithConsumerOkay(ServiceEndpoint endpoint,
                                              MessageExchange exchange) {
        return true;
    }

    /**
     * This method is called by JBI to check if this component, in the role of
     * consumer of the service indicated by the given exchange, can actually
     * interact with the provider properly. The provider is described by the
     * given endpoint and the service description supplied by that endpoint.
     *
     * @param endpoint the endpoint to be used by the provider; must be
     *                 non-null.
     * @param exchange the proposed message exchange to be performed; must be
     *                 non-null.
     * @return <code>true</code> if this consumer component can interact with
     *         the described provider to perform the given exchange.
     */
    public boolean isExchangeWithProviderOkay(ServiceEndpoint endpoint,
                                              MessageExchange exchange) {
        return true;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected synchronized void initializeServiceUnitManager() {
        if (this.serviceManager == null) {
            this.serviceManager = createServiceUnitManager();
        }
    }

    protected ServiceUnitManager createServiceUnitManager() {
        return new ServiceUnitManagerSupport();
    }

    protected ComponentLifeCycle createComponentLifeCycle() {
        return this;
    }


    /**
     * Returns the in message or throws an exception if there is no in message.
     */
    protected NormalizedMessage getInMessage(MessageExchange exchange) throws NoInMessageAvailableException {
        NormalizedMessage message = exchange.getMessage("in");
        if (message == null) {
            throw new NoInMessageAvailableException(exchange);
        }
        return message;
    }

    public MessageTransformer getMessageTransformer() {
        return messageTransformer;
    }

    public void setMessageTransformer(MessageTransformer transformer) {
        this.messageTransformer = transformer;
    }

    /**
     * Performs an invocation where the service, operation or interface name could be specified
     *
     * @param exchange
     * @param in
     * @param service
     * @param interfaceName
     * @param operation
     */
    public void invoke(MessageExchange exchange, NormalizedMessage in, 
                       QName service, QName interfaceName, QName operation) throws MessagingException {
        InOnly outExchange = createInOnlyExchange(service, interfaceName, operation);
        forwardToExchange(exchange, outExchange, in, operation);
    }

    /**
     * Creates a new InOnly exchange for the given service, interface and/or operation (any of which can be null).
     */
    public InOnly createInOnlyExchange(QName service, QName interfaceName, QName operation) throws MessagingException {
        DeliveryChannel channel = getDeliveryChannel();
        MessageExchangeFactory factory = null;
        if (service != null) {
            factory = channel.createExchangeFactoryForService(service);
        } else if (interfaceName != null) {
            factory = channel.createExchangeFactory(interfaceName);
        } else {
            factory = getExchangeFactory();
        }
        InOnly outExchange = factory.createInOnlyExchange();
        if (service != null) {
            outExchange.setService(service);
        }
        if (interfaceName != null) {
            outExchange.setInterfaceName(interfaceName);
        }
        if (operation != null) {
            outExchange.setOperation(operation);
        }
        return outExchange;
    }

    public InOnly createInOnlyExchange(QName service, QName interfaceName, 
                                       QName operation, MessageExchange beforeExchange) throws MessagingException {
        InOnly inOnly = createInOnlyExchange(service, interfaceName, operation);
        propagateCorrelationId(beforeExchange, inOnly);
        return inOnly;
    }

    /**
     * Creates a new InOut exchange for the given service, interface and/or operation (any of which can be null).
     */
    public InOut createInOutExchange(QName service, QName interfaceName, QName operation) throws MessagingException {
        DeliveryChannel channel = getDeliveryChannel();
        MessageExchangeFactory factory = null;
        if (service != null) {
            factory = channel.createExchangeFactoryForService(service);
        } else if (interfaceName != null) {
            factory = channel.createExchangeFactory(interfaceName);
        } else {
            factory = getExchangeFactory();
        }
        InOut outExchange = factory.createInOutExchange();
        if (service != null) {
            outExchange.setService(service);
        }
        if (interfaceName != null) {
            outExchange.setInterfaceName(interfaceName);
        }
        if (operation != null) {
            outExchange.setOperation(operation);
        }
        return outExchange;
    }

    public InOut creatInOutExchange(QName service, QName interfaceName, 
                                    QName operation, MessageExchange srcExchange) throws MessagingException {
        InOut inOut = createInOutExchange(service, interfaceName, operation);
        propagateCorrelationId(srcExchange, inOut);
        return inOut;
    }

    /**
     * Creates an InOnly exchange and propagates the correlation id from the given exchange
     * to the newly created exchange
     * @param srcExchange
     * @return InOnly
     * @throws MessagingException
     */
    public InOnly createInOnlyExchange(MessageExchange srcExchange) throws MessagingException {
        MessageExchangeFactory factory = getExchangeFactory();
        InOnly inOnly = factory.createInOnlyExchange();

        propagateCorrelationId(srcExchange, inOnly);

        return inOnly;
    }

    /**
     * Creates an InOptionalOut exchange and propagates the correlation id from the given exchange
     * to the newly created exchange
     * @param srcExchange
     * @return InOptionalOut
     * @throws MessagingException
     */
    public InOptionalOut createInOptionalOutExchange(MessageExchange srcExchange) throws MessagingException {
        MessageExchangeFactory factory = getExchangeFactory();
        InOptionalOut inOptionalOut = factory.createInOptionalOutExchange();

        propagateCorrelationId(srcExchange, inOptionalOut);

        return inOptionalOut;
    }

    /**
     * Creates an InOut exchange and propagates the correlation id from the given exchange
     * to the newly created exchange
     * @param srcExchange
     * @return InOut
     * @throws MessagingException
     */
    public InOut createInOutExchange(MessageExchange srcExchange) throws MessagingException {
        MessageExchangeFactory factory = getExchangeFactory();
        InOut inOut = factory.createInOutExchange();

        propagateCorrelationId(srcExchange, inOut);

        return inOut;
    }

    /**
     * Creates an RobustInOnly exchange and propagates the correlation id from the given exchange
     * to the newly created exchange
     * @param srcExchange
     * @return RobustInOnly the created exchange
     * @throws MessagingException
     */
    public RobustInOnly createRobustInOnlyExchange(MessageExchange srcExchange) throws MessagingException {
        MessageExchangeFactory factory = getExchangeFactory();
        RobustInOnly robustInOnly = factory.createRobustInOnlyExchange();

        propagateCorrelationId(srcExchange, robustInOnly);

        return robustInOnly;
    }

    /**
     * Propagates the correlation id from an exchange to a newly created exchange
     * @param source Exchange which already exists
     * @param dest Newly created exchange which should get the correlation id
     */
    public void propagateCorrelationId(MessageExchange source, MessageExchange dest) {
        if (source == null || dest == null) {
            return;
        }
        String correlationId = (String) source.getProperty(JbiConstants.CORRELATION_ID);
        if (correlationId != null) {
            dest.setProperty(JbiConstants.CORRELATION_ID, correlationId);
        } else {
            dest.setProperty(JbiConstants.CORRELATION_ID, source.getExchangeId());
        }
    }

    protected void forwardToExchange(MessageExchange exchange, InOnly outExchange, 
                                     NormalizedMessage in, QName operationName) throws MessagingException {
        if (operationName != null) {
            exchange.setOperation(operationName);
        }
        forwardToExchange(exchange, outExchange, in);
    }

    protected void forwardToExchange(MessageExchange exchange, InOnly outExchange, NormalizedMessage in) throws MessagingException {
        NormalizedMessage out = outExchange.createMessage();
        outExchange.setInMessage(out);
        getMessageTransformer().transform(exchange, in, out);
        getDeliveryChannel().send(outExchange);
    }
}
