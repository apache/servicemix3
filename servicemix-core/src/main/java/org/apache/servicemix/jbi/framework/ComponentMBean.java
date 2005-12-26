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

package org.apache.servicemix.jbi.framework;
import javax.jbi.management.ComponentLifeCycleMBean;

/**
 * Defines basic operations on the Compomnent
 */
public interface ComponentMBean extends ComponentLifeCycleMBean {
    
    
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
     * reset all stats counters
     */
    public void reset();
    
    /**
     * Is MessageExchange sender throttling enabled ?
     * @return true if throttling enabled
     */
    public boolean isExchangeThrottling();
    
    /**
     * Set exchange throttling
     * @param value
     *
     */
    public void setExchangeThrottling(boolean value);
    
    /**
     * Get the throttling timeout
     * @return throttling tomeout (ms)
     */
    public long getThrottlingTimeout();
    
    /**
     * Set the throttling timout 
     * @param value (ms)
     */
    public void setThrottlingTimeout(long value);
    
    /**
     * Get the interval for throttling -
     * number of Exchanges set before the throttling timeout is applied
     * @return interval for throttling
     */
    public int getThrottlingInterval();
    
    /**
     * Set the throttling interval
     * number of Exchanges set before the throttling timeout is applied
     * @param value
     */
    public void setThrottlingInterval(int value);
}
