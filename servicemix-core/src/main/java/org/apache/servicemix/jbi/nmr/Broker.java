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
package org.apache.servicemix.jbi.nmr;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.jbi.container.JBIContainer;

/**
 * The Broker handles Nomalised Message Routing within ServiceMix
 * 
 * @version $Revision$
 */
public interface Broker extends BrokerMBean {

    public JBIContainer getContainer();
    
    /**
     * initialize the broker
     * 
     * @param container
     * @throws JBIException
     */
    public void init(JBIContainer container) throws JBIException;
    
    /**
     * suspend the flow to prevent any message exchanges
     */
    public void suspend();

    /**
     * resume message exchange processing
     */
    public void resume();

    /**
     * Route an ExchangePacket to a destination
     * 
     * @param exchange
     * @throws JBIException
     */
    public void sendExchangePacket(MessageExchange exchange) throws JBIException;
    
}