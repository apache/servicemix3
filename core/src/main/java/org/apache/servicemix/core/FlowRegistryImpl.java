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
package org.apache.servicemix.core;

import org.apache.servicemix.api.ServiceMixException;
import org.apache.servicemix.api.internal.*;

/**
 * The default implementation of FlowRegistry.
 * 
 * @version $Revision: $
 * @since 4.0
 */
public class FlowRegistryImpl extends ServiceRegistryImpl<Flow> implements FlowRegistry {

    public boolean canDispatch(InternalExchange exchange, InternalEndpoint endpoint) {
        for (Flow flow : getServices()) {
            if (flow.canDispatch(exchange, endpoint)) {
                return true;
            }
        }
        return false;
    }

    public void dispatch(InternalExchange exchange) {
        InternalReference target = (InternalReference) exchange.getTarget();
        for (InternalEndpoint endpoint : target.choose()) {
            for (Flow flow : getServices()) {
                if (flow.canDispatch(exchange, endpoint)) {
                    flow.dispatch(exchange);
                    return;
                }
            }
            throw new ServiceMixException("Could not dispatch exchange. No flow can handle it.");
        }
    }
}
