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
package org.apache.servicemix.components.drools;

import java.net.URL;

import javax.jbi.JBIException;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.components.util.OutBinding;
import org.drools.FactException;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.io.RuleBaseLoader;
import org.springframework.core.io.Resource;

/**
 * A component which implements a rules based routing using <a href="http://drools.org/">Drools</a> to decide
 * where to route the message.
 *
 * @version $Revision$
 */
public class DroolsComponent extends OutBinding {

    private RuleBase ruleBase;
    private Resource ruleBaseResource;
    private URL ruleBaseURL;
    private ThreadLocal routed = new ThreadLocal();

    public RuleBase getRuleBase() {
        return ruleBase;
    }

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    public Resource getRuleBaseResource() {
        return ruleBaseResource;
    }

    public void setRuleBaseResource(Resource ruleBaseResource) {
        this.ruleBaseResource = ruleBaseResource;
    }

    public URL getRuleBaseURL() {
        return ruleBaseURL;
    }

    public void setRuleBaseURL(URL ruleBaseURL) {
        this.ruleBaseURL = ruleBaseURL;
    }

    // Helper methods for the rule base
    //-------------------------------------------------------------------------
    public void forwardToService(MessageExchange exchange, NormalizedMessage in, QName name) throws MessagingException {
        DeliveryChannel channel = getDeliveryChannel();
        MessageExchangeFactory factory = channel.createExchangeFactoryForService(name);
        InOnly outExchange = factory.createInOnlyExchange();
        String processCorrelationId = (String)exchange.getProperty(JbiConstants.CORRELATION_ID);
        if (processCorrelationId != null) {
            outExchange.setProperty(JbiConstants.CORRELATION_ID, processCorrelationId);
        }
        forwardToExchange(exchange, outExchange, in);
    }

    public void forwardToInterface(QName name, MessageExchange exchange, NormalizedMessage in) throws MessagingException {
        DeliveryChannel channel = getDeliveryChannel();
        MessageExchangeFactory factory = channel.createExchangeFactory(name);
        InOnly outExchange = factory.createInOnlyExchange();
        String processCorrelationId = (String)exchange.getProperty(JbiConstants.CORRELATION_ID);
        if (processCorrelationId != null) {
            outExchange.setProperty(JbiConstants.CORRELATION_ID, processCorrelationId);
        }
        forwardToExchange(exchange, outExchange, in);
    }

    public void route(MessageExchange exchange, NormalizedMessage in, QName service, QName interfaceName, QName operation) throws MessagingException {
        if (routed.get() != null) {
            throw new IllegalStateException("Drools component has already routed this exchange");
        }
        routed.set(Boolean.TRUE);
        DeliveryChannel channel = getDeliveryChannel();
        MessageExchangeFactory factory = channel.createExchangeFactory();
        MessageExchange me = factory.createExchange(exchange.getPattern());
        me.setInterfaceName(interfaceName);
        me.setService(service);
        me.setOperation(operation);
        NormalizedMessage nm = me.createMessage(); 
        me.setMessage(nm, "in");
        getMessageTransformer().transform(exchange, in, nm);
        channel.sendSync(me); 
        if (me.getStatus() == ExchangeStatus.ERROR) {
            fail(exchange, me.getError());
        } else if (me.getStatus() == ExchangeStatus.DONE) {
            done(exchange);
        } else {
            NormalizedMessage out = me.getMessage("out");
            if (out != null) {
                nm = exchange.createMessage();
                exchange.setMessage(nm, "out");
                getMessageTransformer().transform(exchange, out, nm);
            } else {
                Fault f = me.getFault();
                Fault of = exchange.createFault();
                exchange.setFault(of);
                getMessageTransformer().transform(exchange, f, of);
            }
            channel.send(exchange);
            done(me);
        }
        
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void init() throws JBIException {
        super.init();
        try {
            if (ruleBase == null) {
                if (ruleBaseResource != null) {
                    ruleBase = RuleBaseLoader.loadFromInputStream(ruleBaseResource.getInputStream());
                }
                else if (ruleBaseURL != null) {
                    ruleBase = RuleBaseLoader.loadFromUrl(ruleBaseURL);
                }
                else {
                    throw new IllegalArgumentException("You must specify a ruleBase, ruleBaseResource or ruleBaseURL property");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new JBIException(e);
        }
    }

    protected void process(MessageExchange exchange, NormalizedMessage in) throws Exception {
        WorkingMemory memory = ruleBase.newWorkingMemory();
        populateWorkingMemory(memory, exchange, in);
        routed.set(null);
        memory.fireAllRules();
        if (routed.get() == null) {
            if (exchange instanceof InOut) {
                fail(exchange, new Exception("Drools component has not routed the exchange"));
            } else {
                done(exchange);
            }
        }
    }

    protected void populateWorkingMemory(WorkingMemory memory, MessageExchange exchange, NormalizedMessage in) throws MessagingException, FactException {
        memory.setApplicationData("context", getContext());
        memory.setApplicationData("deliveryChannel", getDeliveryChannel());
        memory.setApplicationData("jbi", new JbiHelper(this, exchange, in, memory));
        memory.assertObject(in);
        memory.assertObject(exchange);
    }

}
