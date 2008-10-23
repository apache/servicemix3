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
package org.apache.servicemix.drools.model;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.client.ServiceMixClientFacade;
import org.apache.servicemix.common.EndpointSupport;
import org.apache.servicemix.drools.DroolsComponent;
import org.apache.servicemix.drools.DroolsEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.resolver.URIResolver;
import org.apache.servicemix.jbi.util.MessageUtil;
import org.drools.FactHandle;
import org.drools.WorkingMemory;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;

/**
 * A helper class for use inside a rule to forward a message to an endpoint
 * 
 * @version $Revision: 426415 $
 */
public class JbiHelper extends DefaultAgendaEventListener {

    private DroolsEndpoint endpoint;
    private Exchange exchange;
    private WorkingMemory memory;
    private FactHandle exchangeFactHandle;
    private int rulesFired;
    private boolean exchangeHandled;

    public JbiHelper(DroolsEndpoint endpoint, MessageExchange exchange, WorkingMemory memory) {
        this.endpoint = endpoint;
        this.exchange = new Exchange(exchange, endpoint.getNamespaceContext());
        this.memory = memory;
        
        this.memory.addEventListener(this);
        this.exchangeFactHandle = this.memory.assertObject(this.exchange);
        
        
    }

    public DroolsEndpoint getEndpoint() {
        return endpoint;
    }

    public ComponentContext getContext() {
        return endpoint.getContext();
    }

    public DeliveryChannel getChannel() throws MessagingException {
        return getContext().getDeliveryChannel();
    }

    public ServiceMixClient getClient() {
        return new ServiceMixClientFacade(getContext());
    }

    public Exchange getExchange() {
        return exchange;
    }

    public Log getLogger() {
        return LogFactory.getLog(memory.getRuleBase().getPackages()[0].getName());
    }

    /**
     * Forwards the inbound message to the given
     * 
     * @param uri
     * @param localPart
     */
    /*
     * public void forward(String uri) throws MessagingException { if (exchange
     * instanceof InOnly || exchange instanceof RobustInOnly) { MessageExchange
     * me =
     * getChannel().createExchangeFactory().createExchange(exchange.getPattern
     * ()); URIResolver.configureExchange(me, getContext(), uri);
     * MessageUtil.transferToIn(in, me); getChannel().sendSync(me); } else {
     * throw new
     * MessagingException("Only InOnly and RobustInOnly exchanges can be forwarded"
     * ); } }
     */
    public void route(String uri) throws MessagingException {
        routeTo(null, uri);
    }

    public void routeTo(String content, String uri) throws MessagingException {
        MessageExchange me = this.exchange.getInternalExchange();
        NormalizedMessage in = null;
        if (content == null) {
            in = me.getMessage("in");
        } else {
            in = me.createMessage();
            in.setContent(new StringSource(content));
        }
        MessageExchange newMe = getChannel().createExchangeFactory().createExchange(me.getPattern());
        URIResolver.configureExchange(newMe, getContext(), uri);
        MessageUtil.transferToIn(in, newMe);
        // Set the sender endpoint property
        String key = EndpointSupport.getKey(endpoint);
        newMe.setProperty(JbiConstants.SENDER_ENDPOINT, key);
        newMe.setProperty(JbiConstants.CORRELATION_ID, DroolsEndpoint.getCorrelationId(this.exchange.getInternalExchange()));
        newMe.setProperty(DroolsComponent.DROOLS_CORRELATION_ID, me.getExchangeId());
        getChannel().send(newMe);
    }

    public void routeToDefault(String content) throws MessagingException {
        routeTo(content, endpoint.getDefaultRouteURI());
    }

    /**
     * This method allows for an asynchronous send().  
     * It has no error handling support or support for InOut/InOptionalOut MEPs.
     * 
     * @param content
     * @param uri
     * @throws MessagingException
     */
    @Deprecated
    public void sendTo(String content, String uri) throws MessagingException {

        MessageExchange me = this.exchange.getInternalExchange();

        if ((me instanceof InOnly) || (me instanceof RobustInOnly)) {
            NormalizedMessage in = null;
            if (content == null) {
                in = me.getMessage("in");
            } else {
                in = me.createMessage();
                in.setContent(new StringSource(content));
            }
            MessageExchange newMe = getChannel().createExchangeFactory().createExchange(me.getPattern());
            URIResolver.configureExchange(newMe, getContext(), uri);
            MessageUtil.transferToIn(in, newMe);

            // If i am in route method could send back the done
            me.setStatus(ExchangeStatus.DONE);
            getChannel().send(me);

            // And send forward the new me
            getChannel().send(newMe);
            update();
        } else {
            throw new IllegalStateException("sendTo() method should be used for InOnly or RobustInOnly");
        }

    }

    public void fault(String content) throws Exception {
        fault(new StringSource(content));
    }
    /**
     * Send a JBI Error message (for InOnly) or JBI Fault message (for the other MEPs)
     * 
     * @param content the error content
     * @throws Exception
     */
    public void fault(Source content) throws Exception {
        MessageExchange me = this.exchange.getInternalExchange();
        if (me instanceof InOnly) {
            me.setError(new Exception(new SourceTransformer().toString(content)));
            getChannel().send(me);
        } else {
            Fault fault = me.createFault();
            fault.setContent(content);
            me.setFault(fault);
            getChannel().send(me);
        }
        exchangeHandled = true;
    }

    public void answer(String content) throws Exception {
        answer(new StringSource(content));
    }
    
    /**
     * Answer the exchange with the given response content
     * 
     * @param content the response
     * @throws Exception
     */    
    public void answer(Source content) throws Exception {
        MessageExchange me = this.exchange.getInternalExchange();
        NormalizedMessage out = me.createMessage();
        out.setContent(content);
        me.setMessage(out, "out");
        getChannel().sendSync(me);
        exchangeHandled = true;
        update();
    }

    public void update() {
        this.memory.modifyObject(this.exchangeFactHandle, this.exchange);
    }
    
    /**
     * Get the number of rules that were fired
     * 
     * @return the number of rules
     */
    public int getRulesFired() {
        return rulesFired;
    }
    
    /**
     * Has the MessageExchange been handled by the drools endpoint?
     * 
     * @return
     */
    
    public boolean isExchangeHandled() {
        return exchangeHandled;
    }


    // event handler callbacks
    @Override
    public void activationCreated(ActivationCreatedEvent event) {
        rulesFired++;
    }

}
