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
package org.apache.servicemix.jbi.framework;


/**
 * Defines basic operations on the Compomnent
 */
public interface ComponentStatsMBean {
    
    
    /**
     * Get the Inbound MessageExchange count
     * 
     * @return inbound count
     */
    public long getInboundExchangeCount();

    /**
     * Get the Inbound MessageExchange rate (number/sec)
     * 
     * @return the inbound exchange rate
     */
    public double getInboundExchangeRate();

    /**
     * Get the Outbound MessageExchange count
     * 
     * @return outbound count
     */
    public long getOutboundExchangeCount();

    /**
     * Get the Outbound MessageExchange rate (number/sec)
     * 
     * @return the outbound exchange rate
     */
    public double getOutboundExchangeRate();
    
    /**
     * @return size of the inbound Queue
     */
    public int getInboundQueueSize();

    /**
     * reset all stats counters
     */
    public void reset();
}
