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
package org.apache.servicemix.jbi.framework;

import javax.jbi.management.ComponentLifeCycleMBean;

/**
 * Defines basic operations on the Compomnent
 */
public interface ComponentMBean extends ComponentLifeCycleMBean {
    
    String TYPE_SERVICE_ENGINE = "service-engine";
    String TYPE_BINDING_COMPONENT = "binding-component";
    String TYPE_POJO = "pojo";
    
    /**
     * Get the name of this component
     * @return the name of this component
     */
    String getName();
    
    /**
     * Is MessageExchange sender throttling enabled ?
     * @return true if throttling enabled
     */
    boolean isExchangeThrottling();
    
    /**
     * Set exchange throttling
     * @param value
     *
     */
    void setExchangeThrottling(boolean value);
    
    /**
     * Get the throttling timeout
     * @return throttling timeout (ms)
     */
    long getThrottlingTimeout();
    
    /**
     * Set the throttling timout 
     * @param value (ms)
     */
    void setThrottlingTimeout(long value);
    
    /**
     * Get the interval for throttling -
     * number of Exchanges set before the throttling timeout is applied
     * @return interval for throttling
     */
    int getThrottlingInterval();
    
    /**
     * Set the throttling interval
     * number of Exchanges set before the throttling timeout is applied
     * @param value
     */
    void setThrottlingInterval(int value);
    
    /**
     * @return the component type (service-engine, binding-component)
     */
    String getComponentType();
}
