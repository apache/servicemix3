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
package org.apache.servicemix.jbi.nmr.flow.st;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
import org.apache.servicemix.jbi.servicedesc.AbstractServiceEndpoint;

/**
 * A simple Straight through flow.
 * 
 * A MessageExchange is routed straight to it's destination with 
 * no staging or buffering. A straight through flow is best suited 
 * for the cases where the ServiceMix JBIContainer is deployed with simple 
 * flows (no state) or embedding, or where latency needs to be as low as possible.
 * 
 * @version $Revision$
 * @org.apache.xbean.XBean element="stFlow"
 */
public class STFlow extends AbstractFlow {
    
    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws MessagingException
     */
    protected void doSend(MessageExchangeImpl me) throws MessagingException {
        if (me.getDestinationId() == null) {
            me.setDestinationId(((AbstractServiceEndpoint) me.getEndpoint()).getComponentNameSpace());
        }
        doRouting(me);
    }
    
    /**
     * The type of Flow
     * @return the type
     */
    public String getDescription() {
        return "st";
    }
    
    /**
     * Check if the flow can support the requested QoS for this exchange
     * @param me the exchange to check
     * @return true if this flow can handle the given exchange
     */
    public boolean canHandle(MessageExchange me) {
        if (isPersistent(me)) {
            return false;
        }
        if (isClustered(me)) {
            return false;
        }
        // We can not handle transactional exchanges:
        //  * asynchronous is a bit weird when the transaction is conveyed
        //  * synchronous could lead to deadlock if the provider uses Push delivery
        if (isTransacted(me)) {
            return false;
        }
        return true;
    }
    
}
