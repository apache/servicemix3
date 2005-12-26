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

package org.apache.servicemix.jbi.nmr.flow;

import org.apache.servicemix.jbi.nmr.Broker;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;

/**
 * A Flow provides different dispatch policies within the NMR
 *
 * @version $Revision$
 */
public interface Flow  {
   
    /**
     * Initialize the Region
     * @param broker
     * @throws JBIException
     */
    public void init(Broker broker, String subType) throws JBIException;
    
    /**
     * The type of Flow
     * @return the type
     */
    public String getDescription();
    
    /**
     * Distribute an ExchangePacket
     * @param packet
     * @throws JBIException
     */
    public void send(MessageExchange me) throws JBIException;
    
    /**
     * start the flow
     * @throws JBIException
     */
    public void start() throws JBIException;
    
    
    /**
     * stop the flow
     * @throws JBIException
     */
    public void stop() throws JBIException;
    
    /**
     * shutDown the flow
     * @throws JBIException
     */
    public void shutDown() throws JBIException;
    
    
    /**
     * suspend the flow to prevent any message exchanges
     */
    public void suspend();
    
    
    /**
     * resume message exchange processing
     */
    public void resume();
        
}