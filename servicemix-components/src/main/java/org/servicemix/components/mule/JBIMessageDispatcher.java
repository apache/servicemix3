/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2005 Exist Software Engineering. All rights reserved.
 */
package org.servicemix.components.mule;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.servicemix.jbi.FaultException;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.ObjectName;

/**
 * A dispatcher of Mule events into the JBI container
 *
 * @version $Revision$
 */
public class JBIMessageDispatcher extends AbstractMessageDispatcher implements ComponentLifeCycle {

    private ObjectName extensionMBeanName;
    private JBIConnector jbiConnector;
    private MuleMarshaler marshaler = new MuleMarshaler();
    private ComponentContext context;
    private DeliveryChannel deliveryChannel;
    private MessageExchangeFactory exchangeFactory;

    public JBIMessageDispatcher(JBIConnector connector) {
        super(connector);
        this.jbiConnector = connector;
    }

    public JBIConnector getJbiConnector() {
        return jbiConnector;
    }

    public void setJbiConnector(JBIConnector jbiConnector) {
        this.jbiConnector = jbiConnector;
    }

    // Mule methods
    //-------------------------------------------------------------------------
    public void doDispose() {
    }

    public void doDispatch(UMOEvent event) throws Exception {
        InOnly exchange = getExchangeFactory().createInOnlyExchange();
        NormalizedMessage message = exchange.createMessage();
        exchange.setInMessage(message);
        getMarshaler().populateNormalizedMessage(message, event);
        done(exchange);
    }

    public UMOMessage doSend(UMOEvent event) throws Exception {
        doDispatch(event);
        return event.getMessage();
    }

    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }

    public Object getDelegateSession() throws UMOException {
        return null;
    }


    // JBI methods
    //-------------------------------------------------------------------------
    public ObjectName getExtensionMBeanName() {
        return extensionMBeanName;
    }

    public void init(ComponentContext context) throws JBIException {
        this.context = context;
        this.deliveryChannel = context.getDeliveryChannel();
        this.exchangeFactory = deliveryChannel.createExchangeFactory();
    }

    public void shutDown() throws JBIException {
    }

    public void start() throws JBIException {
    }

    public void stop() throws JBIException {
    }


    // Properties
    //-------------------------------------------------------------------------
    public void setExtensionMBeanName(ObjectName extensionMBeanName) {
        this.extensionMBeanName = extensionMBeanName;
    }

    public MuleMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(MuleMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public ComponentContext getContext() {
        return context;
    }

    public DeliveryChannel getDeliveryChannel() {
        return deliveryChannel;
    }

    public MessageExchangeFactory getExchangeFactory() {
        return exchangeFactory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void done(MessageExchange exchange) throws MessagingException {
        exchange.setStatus(ExchangeStatus.DONE);
        getDeliveryChannel().send(exchange);
    }

    protected void fail(MessageExchange exchange, Exception error) throws MessagingException {
        exchange.setError(error);
        if (error instanceof FaultException) {
            FaultException faultException = (FaultException) error;
            exchange.setFault(faultException.getFault());
        }
        exchange.setStatus(ExchangeStatus.ERROR);
        getDeliveryChannel().send(exchange);
    }

}