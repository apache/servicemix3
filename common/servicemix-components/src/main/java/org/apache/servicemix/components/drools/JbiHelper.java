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

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.drools.WorkingMemory;

/**
 * A helper class for use inside a rule to forward a message to an endpoint
 *
 * @version $Revision$
 */
public class JbiHelper {

    private DroolsComponent component;
    private MessageExchange exchange;
    private NormalizedMessage in;
    private WorkingMemory memory;

    public JbiHelper(DroolsComponent component, 
                     MessageExchange exchange, 
                     NormalizedMessage in,
                     WorkingMemory memory) {
        this.component = component;
        this.exchange = exchange;
        this.in = in;
        this.memory = memory;
    }

    /**
     * Forwards the inbound message to the given
     *
     * @param uri
     * @param localPart
     */
    public void forwardToService(String uri, String localPart) throws MessagingException {
        QName service = new QName(uri, localPart);
        component.forwardToService(exchange, in, service);
    }

    public void forwardToService(QName name, QName operation, QName interfaceName) throws MessagingException {
        component.forwardToService(exchange, in, name);
    }

    public void invoke(QName service, QName operation, QName interfaceName) throws MessagingException {
        component.invoke(exchange, in, service, interfaceName, operation);
    }

    public void route(QName service, QName operation, QName interfaceName) throws MessagingException {
        component.route(exchange, in, service, interfaceName, operation);
    }

    public DeliveryChannel getDeliveryChannel() throws MessagingException {
        return getComponent().getContext().getDeliveryChannel();
    }

    public DroolsComponent getComponent() {
        return component;
    }

    public MessageExchange getExchange() {
        return exchange;
    }

    public NormalizedMessage getIn() {
        return in;
    }

}
