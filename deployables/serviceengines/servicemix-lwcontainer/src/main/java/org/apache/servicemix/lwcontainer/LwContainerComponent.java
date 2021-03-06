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
package org.apache.servicemix.lwcontainer;

import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.common.BaseComponent;
import org.apache.servicemix.common.BaseServiceUnitManager;
import org.apache.servicemix.common.Deployer;
import org.apache.servicemix.common.Endpoint;

public class LwContainerComponent extends BaseComponent {

    /* (non-Javadoc)
     * @see org.servicemix.common.BaseComponent#createServiceUnitManager()
     */
    public BaseServiceUnitManager createServiceUnitManager() {
        Deployer[] deployers = new Deployer[] {new LwContainerXBeanDeployer(this) };
        return new BaseServiceUnitManager(this, deployers);
    }
    
    public void prepareShutdown(Endpoint endpoint) throws InterruptedException {
        lifeCycle.prepareShutdown(endpoint);
    }
    
    public void handleExchange(Endpoint endpoint, MessageExchange exchange, boolean add) {
        lifeCycle.handleExchange(endpoint, exchange, add);
    }

}
