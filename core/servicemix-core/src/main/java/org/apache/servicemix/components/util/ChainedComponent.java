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

import java.util.Iterator;
import java.util.Set;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.FaultException;

/**
 * This class allows a series of componeents to be chained together. It will
 * invoke the first service, then take the output of that first service as the
 * input to the next service, and return the overall results when finished.
 * 
 * All properties and attachments are maintained.
 * 
 * @author birchfieldj
 * @deprecated use the StaticRoutingSlip pattern from the EIP component instead
 * 
 */
public class ChainedComponent extends TransformComponentSupport {

    private QName[] services = new QName[0];

    protected boolean transform(MessageExchange exchange, 
                                NormalizedMessage in,
                                NormalizedMessage out) throws MessagingException {
        NormalizedMessage curIn = in;
        MessageExchange curExchange = exchange;
        for (int i = 0; i < services.length; i++) {
            InOut mexchange = this.getDeliveryChannel()
                    .createExchangeFactoryForService(services[i])
                    .createInOutExchange();
            copyProperties(curExchange, mexchange);
            curIn = invokeService(mexchange, curIn, services[i]);
            curExchange = mexchange;
        }
        getMessageTransformer().transform(exchange, curIn, out);
        copyProperties(curExchange, exchange);
        return true;
    }

    /**
     * Invokes the service with the given message, and returns the output
     * 
     * @param exchange
     * @param in
     * @param service
     * @return the out message of the invoked service
     * @throws MessagingException
     */
    private NormalizedMessage invokeService(InOut exchange,
                                            NormalizedMessage in, 
                                            QName service) throws MessagingException {
        NormalizedMessage msg = exchange.createMessage();
        getMessageTransformer().transform(exchange, in, msg);
        exchange.setMessage(msg, "in");
        boolean result = this.getDeliveryChannel().sendSync(exchange);
        if (result) {
            if (exchange.getStatus() == ExchangeStatus.ERROR) {
                exchange.setStatus(ExchangeStatus.DONE);
                getDeliveryChannel().send(exchange);
                if (exchange.getError() != null) {
                    throw new MessagingException("Received error", exchange.getError());
                } else if (exchange.getFault() != null) {
                    throw new FaultException("Received fault", exchange, exchange.getFault());
                } else {
                    throw new MessagingException("Received unknown error");
                }
            } else {
                NormalizedMessage out = exchange.getOutMessage();
                exchange.setStatus(ExchangeStatus.DONE);
                getDeliveryChannel().send(exchange);
                return out; 
            }
        }
        throw new MessagingException("Could not invoke service: " + service);
    }

    /**
     * 
     * @param in
     *            echange to copy from
     * @param out
     *            excahnge to copy to
     */
    private void copyProperties(MessageExchange in, MessageExchange out) {
        Set propertyNames = in.getPropertyNames();
        for (Iterator iter = propertyNames.iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            out.setProperty(name, in.getProperty(name));
        }
    }

    /**
     * Allows the services to be set
     * 
     * @param services
     *            a collection of QNAmes representing the services to be
     *            invoked.
     */
    public void setServices(QName[] services) {
        this.services = services;
    }

}
