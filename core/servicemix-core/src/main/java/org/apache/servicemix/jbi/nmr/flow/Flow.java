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
package org.apache.servicemix.jbi.nmr.flow;

import javax.jbi.JBIException;
import javax.jbi.management.LifeCycleMBean;
import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.jbi.nmr.Broker;

/**
 * A Flow provides different dispatch policies within the NMR
 *
 * @version $Revision$
 */
public interface Flow  extends LifeCycleMBean {
   
    /**
     * Initialize the Region
     * @param broker
     * @throws JBIException
     */
    void init(Broker broker) throws JBIException;
    
    /**
     * The description of Flow
     * @return the description
     */
    String getDescription();
    
    /**
     * The unique name of Flow
     * @return the name
     */
    String getName();
    
    /**
     * Distribute an ExchangePacket
     * @param packet
     * @throws JBIException
     */
    void send(MessageExchange me) throws JBIException;
    
    /**
     * suspend the flow to prevent any message exchanges
     */
    void suspend();
    
    
    /**
     * resume message exchange processing
     */
    void resume();
    
    /**
     * Get the broker associated with this flow
     *
     */
    Broker getBroker();
    
    /**
     * Check if the flow can support the requested QoS for this exchange
     * @param me the exchange to check
     * @return true if this flow can handle the given exchange
     */
    boolean canHandle(MessageExchange me);
        
}