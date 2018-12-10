/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.eip.patterns;

import java.net.URI;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.RobustInOnly;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.eip.EIPEndpoint;
import org.apache.servicemix.eip.support.ExchangeTarget;
import org.apache.servicemix.eip.support.MessageUtil;
import org.apache.servicemix.jbi.FaultException;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;

/**
 * The Pipeline component is a bridge between an In-Only (or Robust-In-Only) MEP and
 * an In-Out MEP.
 * When the Pipeline receives an In-Only MEP, it will send the input in an In-Out MEP 
 * to the tranformer destination and forward the response in an In-Only MEP to the target 
 * destination.
 * In addition, this component is fully asynchronous and uses an exchange store to provide
 * full HA and recovery for clustered / persistent flows. 
 *  
 * @author gnodet
 * @version $Revision: 376451 $
 * @org.apache.xbean.XBean element="pipeline"
 *                  description="A Pipeline"
 */
public class Pipeline extends EIPEndpoint {

    private static final Log log = LogFactory.getLog(Pipeline.class);

    private static final String TRANSFORMER = "Pipeline.Transformer";
    
    private static final String CONSUMER_MEP = "Pipeline.ConsumerMEP";

    /**
     * The adress of the in-out endpoint acting as a transformer
     */
    private ExchangeTarget transformer;

    /**
     * The address of the target endpoint
     */
    private ExchangeTarget target;

    /**
     * The correlation property used by this component
     */
    private String correlationConsumer;

    /**
     * The correlation property used by this component
     */
    private String correlationTransformer;

    /**
     * The correlation property used by this component
     */
    private String correlationTarget;

    /**
     * @return Returns the target.
     */
    public ExchangeTarget getTarget() {
        return target;
    }

    /**
     * @param target The target to set.
     */
    public void setTarget(ExchangeTarget target) {
        this.target = target;
    }

    /**
     * @return Returns the transformer.
     */
    public ExchangeTarget getTransformer() {
        return transformer;
    }

    /**
     * @param transformer The transformer to set.
     */
    public void setTransformer(ExchangeTarget transformer) {
        this.transformer = transformer;
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.eip.EIPEndpoint#validate()
     */
    public void validate() throws DeploymentException {
        super.validate();
        // Check target
        if (target == null) {
            throw new IllegalArgumentException("target should be set to a valid ExchangeTarget");
        }
        // Check transformer
        if (transformer == null) {
            throw new IllegalArgumentException("transformer should be set to a valid ExchangeTarget");
        }
        // Create correlation properties
        correlationConsumer = "Pipeline.Consumer." + getService() + "." + getEndpoint();
        correlationTransformer = "Pipeline.Transformer." + getService() + "." + getEndpoint();
        correlationTarget = "Pipeline.Target." + getService() + "." + getEndpoint();
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.common.ExchangeProcessor#process(javax.jbi.messaging.MessageExchange)
     */
    public void process(MessageExchange exchange) throws MessagingException {
        try {
            // The exchange comes from the consumer
            if (exchange.getRole() == MessageExchange.Role.PROVIDER) {
                // A DONE status from the consumer can only be received
                // when a fault has been sent
                if (exchange.getStatus() == ExchangeStatus.DONE) {
                    String transformerId = (String) exchange.getProperty(correlationTransformer);
                    String targetId = (String) exchange.getProperty(correlationTarget);
                    if (transformerId == null && targetId == null) {
                        throw new IllegalStateException("Exchange status is " + ExchangeStatus.DONE + " but has no correlation set");
                    }
                    // Load the exchange
                    MessageExchange me = (MessageExchange) store.load(targetId != null ? targetId : transformerId);
                    done(me);
                // Errors must be sent back to the target or transformer
                } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                    String transformerId = (String) exchange.getProperty(correlationTransformer);
                    String targetId = (String) exchange.getProperty(correlationTarget);
                    if (transformerId == null && targetId == null) {
                        throw new IllegalStateException("Exchange status is " + ExchangeStatus.DONE + " but has no correlation set");
                    }
                    // Load the exchange
                    MessageExchange me = (MessageExchange) store.load(targetId != null ? targetId : transformerId);
                    fail(me, exchange.getError());
                // This is a new exchange
                } else if (exchange.getProperty(correlationTransformer) == null) {
                    if (exchange instanceof InOnly == false && exchange instanceof RobustInOnly == false) {
                        fail(exchange, new UnsupportedOperationException("Use an InOnly or RobustInOnly MEP"));
                        return;
                    }
                    // Create exchange for target
                    MessageExchange tme = exchangeFactory.createInOutExchange();
                    transformer.configureTarget(tme, getContext());
                    // Set correlations
                    exchange.setProperty(correlationTransformer, tme.getExchangeId());
                    tme.setProperty(correlationConsumer, exchange.getExchangeId());
                    tme.setProperty(TRANSFORMER, Boolean.TRUE);
                    tme.setProperty(CONSUMER_MEP, exchange.getPattern());
                    // Put exchange to store
                    store.store(exchange.getExchangeId(), exchange);
                    // Send in to listener and target
                    MessageUtil.transferInToIn(exchange, tme);
                    send(tme);
                } else {
                    throw new IllegalStateException("Exchange status is " + ExchangeStatus.ACTIVE + " but has no correlation set");
                }
            // If the exchange comes from the transformer
            } else if (Boolean.TRUE.equals(exchange.getProperty(TRANSFORMER))) {
                // Retrieve the correlation id
                String consumerId = (String) exchange.getProperty(correlationConsumer);
                if (consumerId == null) {
                    throw new IllegalStateException(correlationConsumer + " property not found");
                }
                // This should not happen beacause the MEP is an In-Out
                // and the DONE status is always sent by the consumer (us)
                if (exchange.getStatus() == ExchangeStatus.DONE) {
                    throw new IllegalStateException("Received a DONE status from the transformer");
                // Errors must be sent back to the consumer
                } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                    MessageExchange me = (MessageExchange) store.load(consumerId);
                    fail(me, exchange.getError());
                // Faults must be sent back to the consumer
                } else if (exchange.getFault() != null) {
                    MessageExchange me = (MessageExchange) store.load(consumerId);
                    if (me instanceof InOnly) {
                        // Do not use the fault has it may contain streams
                        // So just transform it to a string and send an error
                        String fault = new SourceTransformer().contentToString(exchange.getFault());
                        fail(me, new FaultException(fault, null, null));
                        done(exchange);
                    } else {
                        store.store(exchange.getExchangeId(), exchange);
                        MessageUtil.transferFaultToFault(exchange, me);
                        send(me);
                    }
                // This is the answer from the transformer
                } else if (exchange.getMessage("out") != null) {
                    // Retrieve the consumer MEP
                    URI mep = (URI) exchange.getProperty(CONSUMER_MEP);
                    if (mep == null) {
                        throw new IllegalStateException("Exchange does not carry the consumer MEP");
                    }
                    MessageExchange me = exchangeFactory.createExchange(mep);
                    target.configureTarget(me, getContext());
                    me.setProperty(correlationConsumer, consumerId);
                    me.setProperty(correlationTransformer, exchange.getExchangeId());
                    store.store(exchange.getExchangeId(), exchange);
                    MessageUtil.transferOutToIn(exchange, me);
                    send(me);
                // This should not happen
                } else {
                    throw new IllegalStateException("Exchange status is " + ExchangeStatus.ACTIVE + " but has no Out nor Fault message");
                }
            // The exchange comes from the target
            } else {
                // Retrieve the correlation id for the consumer
                String consumerId = (String) exchange.getProperty(correlationConsumer);
                if (consumerId == null) {
                    throw new IllegalStateException(correlationConsumer + " property not found");
                }
                // Retrieve the correlation id for the transformer
                String transformerId = (String) exchange.getProperty(correlationTransformer);
                if (transformerId == null) {
                    throw new IllegalStateException(correlationTransformer + " property not found");
                }
                // This should be the last message received
                if (exchange.getStatus() == ExchangeStatus.DONE) {
                    // Need to ack the transformer
                    MessageExchange tme = (MessageExchange) store.load(transformerId);
                    done(tme);
                    // Need to ack the consumer
                    MessageExchange cme = (MessageExchange) store.load(consumerId);
                    done(cme);
                // Errors should be sent back to the consumer
                } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                    // Need to ack the transformer
                    MessageExchange tme = (MessageExchange) store.load(transformerId);
                    done(tme);
                    // Send error to consumer
                    MessageExchange cme = (MessageExchange) store.load(consumerId);
                    fail(cme, exchange.getError());
                // If we have a robust-in-only MEP, we can receive a fault
                } else if (exchange.getFault() != null) {
                    // Need to ack the transformer
                    MessageExchange tme = (MessageExchange) store.load(transformerId);
                    done(tme);
                    // Send fault back to consumer
                    store.store(exchange.getExchangeId(), exchange);
                    MessageExchange cme = (MessageExchange) store.load(consumerId);
                    cme.setProperty(correlationTarget, exchange.getExchangeId());
                    MessageUtil.transferFaultToFault(exchange, cme);
                    send(cme);
                // This should not happen
                } else {
                    throw new IllegalStateException("Exchange from target has a " + ExchangeStatus.ACTIVE + " status but has no Fault message");
                }
            }
        // If an error occurs, log it and report the error back to the sender
        // if the exchange is still ACTIVE 
        } catch (Exception e) {
            log.error("An exception occured while processing exchange", e);
            if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                fail(exchange, e);
            }
        }
    }

}
