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

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.eip.EIPEndpoint;
import org.apache.servicemix.eip.support.ExchangeTarget;
import org.apache.servicemix.eip.support.MessageUtil;

/**
 * A RoutingSlip component can be used to route an incoming In-Out exchange
 * through a series of target services.
 * This component implements the 
 * <a href="http://www.enterpriseintegrationpatterns.com/RoutingTable.html">Routing Slip</a> 
 * pattern, with the limitation that the routing table is static.
 * This component only uses In-Out MEPs and errors or faults sent by targets are reported
 * back to the consumer, thus interrupting the routing process.
 * In addition, this component is fully asynchronous and uses an exchange store to provide
 * full HA and recovery for clustered / persistent flows. 
 *  
 * @author gnodet
 * @version $Revision: 376451 $
 * @org.apache.xbean.XBean element="static-routing-slip"
 *                  description="A static Routing Slip"
 */
public class StaticRoutingSlip extends EIPEndpoint {

    private static final Log log = LogFactory.getLog(Pipeline.class);

    /**
     * List of target components used in the RoutingSlip
     */
    private ExchangeTarget[] targets;
    /**
     * The correlation property used by this component
     */
    private String correlation;
    /**
     * The current index of the target 
     */
    private String index;
    /**
     * The id of the previous target exchange 
     */
    private String previous;
    
    /**
     * @return Returns the targets.
     */
    public ExchangeTarget[] getTargets() {
        return targets;
    }

    /**
     * @param targets The targets to set.
     */
    public void setTargets(ExchangeTarget[] targets) {
        this.targets = targets;
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.eip.EIPEndpoint#validate()
     */
    public void validate() throws DeploymentException {
        super.validate();
        // Check target
        if (targets == null || targets.length == 0) {
            throw new IllegalArgumentException("targets should contain at least one ExchangeTarget");
        }
        // Create correlation properties
        correlation = "RoutingSlip.Correlation." + getService() + "." + getEndpoint();
        index = "RoutingSlip.Index." + getService() + "." + getEndpoint();
        previous = "RoutingSlip.Previous." + getService() + "." + getEndpoint();
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.common.ExchangeProcessor#process(javax.jbi.messaging.MessageExchange)
     */
    public void process(MessageExchange exchange) throws MessagingException {
        try {
            // This exchange comes from the consumer
            if (exchange.getRole() == MessageExchange.Role.PROVIDER) {
                if (exchange.getStatus() == ExchangeStatus.DONE) {
                    String correlationId = (String) exchange.getProperty(correlation);
                    if (correlationId == null) {
                        throw new IllegalStateException(correlation + " property not found");
                    }
                    // Ack last target hit
                    MessageExchange me = (MessageExchange) store.load(correlationId);
                    done(me);
                } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                    String correlationId = (String) exchange.getProperty(correlation);
                    if (correlationId == null) {
                        throw new IllegalStateException(correlation + " property not found");
                    }
                    // Ack last target hit
                    MessageExchange me = (MessageExchange) store.load(correlationId);
                    done(me);
                } else if (exchange instanceof InOut == false) {
                    throw new IllegalStateException("Use an InOut MEP");
                } else {
                    MessageExchange me = exchangeFactory.createInOutExchange();
                    me.setProperty(correlation, exchange.getExchangeId());
                    me.setProperty(index, new Integer(0));
                    targets[0].configureTarget(me, getContext());
                    store.store(exchange.getExchangeId(), exchange);
                    MessageUtil.transferInToIn(exchange, me);
                    send(me);
                }
            // The exchange comes from a target
            } else {
                String correlationId = (String) exchange.getProperty(correlation);
                String previousId = (String) exchange.getProperty(previous);
                Integer prevIndex = (Integer) exchange.getProperty(index);
                if (correlationId == null) {
                    throw new IllegalStateException(correlation + " property not found");
                }
                if (prevIndex == null) {
                    throw new IllegalStateException(previous + " property not found");
                }
                // This should never happen, as we can only send DONE
                if (exchange.getStatus() == ExchangeStatus.DONE) {
                    throw new IllegalStateException("Exchange status is " + ExchangeStatus.DONE);
                // ERROR are sent back to the consumer
                } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                    MessageExchange me = (MessageExchange) store.load(correlationId);
                    fail(me, exchange.getError());
                    // Ack the previous target
                    if (previousId != null) {
                        me = (MessageExchange) store.load(previousId);
                        done(me);
                    }
                // Faults are sent back to the consumer
                } else if (exchange.getFault() != null) {
                    MessageExchange me = (MessageExchange) store.load(correlationId);
                    me.setProperty(correlation, exchange.getExchangeId());
                    store.store(exchange.getExchangeId(), exchange);
                    MessageUtil.transferFaultToFault(exchange, me);
                    send(me);
                    // Ack the previous target
                    if (previousId != null) {
                        me = (MessageExchange) store.load(previousId);
                        done(me);
                    }
                // Out message, give it to next target or back to consumer
                } else if (exchange.getMessage("out") != null) {
                    // This is the answer from the last target
                    if (prevIndex.intValue() == targets.length - 1) {
                        MessageExchange me = (MessageExchange) store.load(correlationId);
                        me.setProperty(correlation, exchange.getExchangeId());
                        store.store(exchange.getExchangeId(), exchange);
                        MessageUtil.transferOutToOut(exchange, me);
                        send(me);
                        if (previousId != null) {
                            me = (MessageExchange) store.load(previousId);
                            done(me);
                        }
                    // We still have a target to hit
                    } else {
                        MessageExchange me = exchangeFactory.createInOutExchange();
                        Integer curIndex = new Integer(prevIndex.intValue() + 1);
                        me.setProperty(correlation, correlationId);
                        me.setProperty(index, curIndex);
                        me.setProperty(previous, exchange.getExchangeId());
                        targets[curIndex.intValue()].configureTarget(me, getContext());
                        store.store(exchange.getExchangeId(), exchange);
                        MessageUtil.transferOutToIn(exchange, me);
                        send(me);
                        if (previousId != null) {
                            me = (MessageExchange) store.load(previousId);
                            done(me);
                        }
                    }
                // This should not happen
                } else {
                    throw new IllegalStateException("Exchange status is " + ExchangeStatus.ACTIVE + " but has no Out nor Fault message");
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
