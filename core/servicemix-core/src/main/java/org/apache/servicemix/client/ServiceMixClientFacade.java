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
package org.apache.servicemix.client;

import java.util.Iterator;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
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

import org.w3c.dom.DocumentFragment;

import org.apache.servicemix.jbi.FaultException;
import org.apache.servicemix.jbi.NoOutMessageAvailableException;
import org.apache.servicemix.jbi.api.EndpointResolver;
import org.apache.servicemix.jbi.api.Message;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.messaging.DefaultMarshaler;
import org.apache.servicemix.jbi.messaging.PojoMarshaler;
import org.apache.servicemix.jbi.resolver.EndpointFilter;
import org.apache.servicemix.jbi.resolver.ExternalInterfaceNameEndpointResolver;
import org.apache.servicemix.jbi.resolver.ExternalServiceNameEndpointResolver;
import org.apache.servicemix.jbi.resolver.InterfaceNameEndpointResolver;
import org.apache.servicemix.jbi.resolver.NullEndpointFilter;
import org.apache.servicemix.jbi.resolver.ServiceAndEndpointNameResolver;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.jbi.resolver.URIResolver;

/**
 * A Facade around the {@link ComponentContext} to provide the {@link ServiceMixClient} API which is useful for
 * working with JBI from inside a POJO based JBI Component which doesn't derive from {@link PojoSupport}
 *
 * @version $Revision$
 */
public class ServiceMixClientFacade implements ServiceMixClient {

    private ComponentContext context;
    private EndpointFilter filter = NullEndpointFilter.getInstance();
    private PojoMarshaler marshaler = new DefaultMarshaler();
    private MessageExchangeFactory exchangeFactory;

    public ServiceMixClientFacade(ComponentContext context) {
        this.context = context;
    }

    /**
     * Provides the JBI container used for message dispatch.
     */
    public ServiceMixClientFacade(JBIContainer container) throws JBIException {
        this(container, new ActivationSpec());
    }

    /**
     * Provides the JBI container and the activation specification, which can be used to register this
     * client at a specific endpoint so that default container routing rules can be configured via dependency injection
     * and the client endpoint metadata can be configured to allow services to talk to this client.
     */
    public ServiceMixClientFacade(JBIContainer container, ActivationSpec activationSpec) throws JBIException {
        activationSpec.setComponent(this);
        container.activateComponent(activationSpec);
    }

    public InOnly createInOnlyExchange() throws MessagingException {
        InOnly exchange = getExchangeFactory().createInOnlyExchange();
        NormalizedMessage in = exchange.createMessage();
        exchange.setInMessage(in);
        return exchange;
    }

    public InOnly createInOnlyExchange(EndpointResolver resolver) throws JBIException {
        InOnly exchange = createInOnlyExchange();
        configureEndpoint(exchange, resolver);
        return exchange;
    }

    public InOut createInOutExchange() throws MessagingException {
        InOut exchange = getExchangeFactory().createInOutExchange();
        NormalizedMessage in = exchange.createMessage();
        exchange.setInMessage(in);
        return exchange;
    }

    public InOut createInOutExchange(EndpointResolver resolver) throws JBIException {
        InOut exchange = createInOutExchange();
        configureEndpoint(exchange, resolver);
        return exchange;
    }

    public InOptionalOut createInOptionalOutExchange() throws MessagingException {
        InOptionalOut exchange = getExchangeFactory().createInOptionalOutExchange();
        NormalizedMessage in = exchange.createMessage();
        exchange.setInMessage(in);
        return exchange;
    }

    public InOptionalOut createInOptionalOutExchange(EndpointResolver resolver) throws JBIException {
        InOptionalOut exchange = createInOptionalOutExchange();
        configureEndpoint(exchange, resolver);
        return exchange;
    }

    public RobustInOnly createRobustInOnlyExchange() throws MessagingException {
        RobustInOnly exchange = getExchangeFactory().createRobustInOnlyExchange();
        NormalizedMessage in = exchange.createMessage();
        exchange.setInMessage(in);
        return exchange;
    }

    public RobustInOnly createRobustInOnlyExchange(EndpointResolver resolver) throws JBIException {
        RobustInOnly exchange = getExchangeFactory().createRobustInOnlyExchange();
        configureEndpoint(exchange, resolver);
        return exchange;
    }

    public Destination createDestination(String uri) throws MessagingException {
        return new DefaultDestination(this, uri);
    }

    public void send(MessageExchange exchange) throws MessagingException {
        getDeliveryChannel().send(exchange);
    }
        
    public void send(Message message) throws MessagingException {
        send(message.getExchange());
    }

    public boolean sendSync(MessageExchange exchange) throws MessagingException {
        return getDeliveryChannel().sendSync(exchange);
    }

    public boolean sendSync(MessageExchange exchange, long timeout) throws MessagingException {
        return getDeliveryChannel().sendSync(exchange, timeout);
    }

    public MessageExchange receive() throws MessagingException {
        return getDeliveryChannel().accept();
    }

    public MessageExchange receive(long timeout) throws MessagingException {
        return getDeliveryChannel().accept(timeout);
    }

    public ComponentContext getContext() {
        return context;
    }

    public DeliveryChannel getDeliveryChannel() throws MessagingException {
        return getContext().getDeliveryChannel();
    }

    /**
     * Provide access to the default message exchange exchangeFactory, lazily creating one.
     */
    public MessageExchangeFactory getExchangeFactory() throws MessagingException {
        if (exchangeFactory == null && context != null) {
            exchangeFactory = getDeliveryChannel().createExchangeFactory();
        }
        return exchangeFactory;
    }

    /**
     * A helper method to indicate that the message exchange is complete
     * which will set the status to {@link ExchangeStatus#DONE} and send the message
     * on the delivery channel.
     *
     * @param exchange
     * @throws MessagingException
     */
    public void done(MessageExchange exchange) throws MessagingException {
        exchange.setStatus(ExchangeStatus.DONE);
        getDeliveryChannel().send(exchange);
    }

    /**
     * A helper method which fails and completes the given exchange with the specified fault
     */
    public void fail(MessageExchange exchange, Fault fault) throws MessagingException {
        exchange.setFault(fault);
        getDeliveryChannel().send(exchange);
    }

    /**
     * A helper method which fails and completes the given exchange with the specified error
     */
    public void fail(MessageExchange exchange, Exception error) throws MessagingException {
        if (error instanceof FaultException) {
            FaultException faultException = (FaultException) error;
            exchange.setFault(faultException.getFault());
        } else {
            exchange.setError(error);
        }
        getDeliveryChannel().send(exchange);
    }

    // Helper methods to make JBI a little more concise to use from a client
    //-------------------------------------------------------------------------
    
    public Object request(Map inMessageProperties, Object content) throws JBIException {
        return request(null, null, inMessageProperties, content);
    }

    public void send(Map inMessageProperties, Object content) throws JBIException {
        send(null, null, inMessageProperties, content);
    }

    public boolean sendSync(Map inMessageProperties, Object content) throws JBIException {
        return sendSync(null, null, inMessageProperties, content);
    }

    public void send(EndpointResolver resolver, Map exchangeProperties, Map inMessageProperties, Object content) throws JBIException {
        InOnly exchange = createInOnlyExchange(resolver);
        populateMessage(exchange, exchangeProperties, inMessageProperties, content);
        send(exchange);
    }

    public boolean sendSync(EndpointResolver resolver, Map exchangeProperties, 
                            Map inMessageProperties, Object content) throws JBIException {
        InOnly exchange = createInOnlyExchange(resolver);
        populateMessage(exchange, exchangeProperties, inMessageProperties, content);
        return sendSync(exchange);
    }

    public Object request(EndpointResolver resolver, Map exchangeProperties, Map inMessageProperties, Object content) throws JBIException {
        InOut exchange = createInOutExchange(resolver);
        populateMessage(exchange, exchangeProperties, inMessageProperties, content);
        boolean answer = sendSync(exchange);
        if (!answer) {
            throw new JBIException("Exchange aborted");
        }
        Exception error = exchange.getError();
        if (error != null) {
            throw new JBIException(error);
        }
        if (exchange.getFault() != null) {
            done(exchange);
            throw FaultException.newInstance(exchange);
        }


        NormalizedMessage outMessage = exchange.getOutMessage();
        if (outMessage == null) {
            throw new NoOutMessageAvailableException(exchange);
        }
        Object result = getMarshaler().unmarshal(exchange, outMessage);
        done(exchange);
        return result;
    }

    public ServiceEndpoint resolveEndpointReference(String uri) {
        DocumentFragment epr = URIResolver.createWSAEPR(uri);
        return getContext().resolveEndpointReference(epr);
    }

    public EndpointResolver createResolverForService(QName service) {
        return new ServiceNameEndpointResolver(service);
    }

    public EndpointResolver createResolverInterface(QName interfaceName) {
        return new InterfaceNameEndpointResolver(interfaceName);
    }

    public EndpointResolver createResolverForExternalService(QName service) {
        return new ExternalServiceNameEndpointResolver(service);
    }

    public EndpointResolver createResolverForExternalInterface(QName interfaceName) {
        return new ExternalInterfaceNameEndpointResolver(interfaceName);
    }

    public EndpointResolver createResolverForExternalInterface(QName service, String endpoint) {
        return new ServiceAndEndpointNameResolver(service, endpoint);
    }
    
    public void close() throws JBIException {
    }


    // Properties
    //-------------------------------------------------------------------------
    public EndpointFilter getFilter() {
        return filter;
    }

    /**
     * Sets the filter used to exclude possible endpoints based on their capabilities
     *
     * @param filter
     */
    public void setFilter(EndpointFilter filter) {
        this.filter = filter;
    }

    public PojoMarshaler getMarshaler() {
        return marshaler;
    }

    /**
     * Sets the marshaler used to convert objects which are not already JAXP {@link Source} instances
     * into the normalized message content.
     *
     * @param marshaler
     */
    public void setMarshaler(PojoMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void configureEndpoint(MessageExchange exchange, EndpointResolver resolver) throws JBIException {
        if (resolver != null) {
            exchange.setEndpoint(resolver.resolveEndpoint(getContext(), exchange, filter));
        }
    }

    protected void populateMessage(MessageExchange exchange, Map exchangeProperties, 
                                   Map inMessageProperties, Object content) throws MessagingException {
        NormalizedMessage in = exchange.getMessage("in");
        populateExchangeProperties(exchange, exchangeProperties);
        populateMessageProperties(in, inMessageProperties);
        getMarshaler().marshal(exchange, in, content);
    }

    protected void populateExchangeProperties(MessageExchange exchange, Map properties) {
        if (properties != null) {
            for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                exchange.setProperty((String) entry.getKey(), entry.getValue());
            }
        }
    }

    protected void populateMessageProperties(NormalizedMessage message, Map properties) {
        if (properties != null) {
            for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                message.setProperty((String) entry.getKey(), entry.getValue());
            }
        }
    }
}
