/** 
 * <a href="http://servicemix.org">ServiceMix: The open source ESB</a> 
 * 
 * Copyright 2005 RAJD Consultancy Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/

package org.apache.servicemix.jbi.nmr.flow.st;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;

import javax.jbi.messaging.MessagingException;

/**
 * A simple Straight through flow.
 * 
 * A MessageExchange is routed straight to it's destination with 
 * no staging or buffering. A straight through flow is best suited 
 * for the cases where the ServiceMix JBIContainer is deployed with simple 
 * flows (no state) or embedding, or where latency needs to be as low as possible.
 * 
 * @version $Revision$
 */
public class STFlow extends AbstractFlow {
    
    /**
     * Distribute an ExchangePacket
     * 
     * @param packet
     * @throws MessagingException
     */
    protected void doSend(MessageExchangeImpl me) throws MessagingException {
        doRouting(me);
    }
    
    /**
     * The type of Flow
     * @return the type
     */
    public String getDescription(){
        return "st";
    }
    
}