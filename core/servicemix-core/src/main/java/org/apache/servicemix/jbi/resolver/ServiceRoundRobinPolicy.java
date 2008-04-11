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
package org.apache.servicemix.jbi.resolver;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

/**
 * a simple round robin endpoint resolver policy to have a 
 * basic load balancing
 * 
 * @author lhein
 */
public class ServiceRoundRobinPolicy implements EndpointChooser {

    private Map<QName, Integer> lastIndexMap = new HashMap<QName, Integer>();
    private int lastIndex;

    /*
     * (non-Javadoc)
     * @see org.apache.servicemix.jbi.resolver.EndpointChooser#chooseEndpoint
     * (javax.jbi.servicedesc.ServiceEndpoint[], javax.jbi.component.ComponentContext, 
     * javax.jbi.messaging.MessageExchange)
     */
    public ServiceEndpoint chooseEndpoint(ServiceEndpoint[] endpoints, ComponentContext context,
                                          MessageExchange exchange) {
        // if there are no endpoints, then just return null
        if (endpoints.length == 0) {
            return null;
        }

        if (exchange.getService() == null) {
            return endpoints[0];
        }
        
        // check for saved index for that service
        if (lastIndexMap.containsKey(exchange.getService())) {
            // ok, there is already something
            lastIndex = lastIndexMap.get(exchange.getService());
        } else {
            // fresh value
            lastIndex = 0;
        }

        // check if the index needs a reset
        if (lastIndex >= endpoints.length || lastIndex < 0) {
            // reset the index
            lastIndex = 0;
        }

        // determine the next endpoint to use
        ServiceEndpoint result = endpoints[lastIndex++];

        // save the index
        lastIndexMap.put(exchange.getService(), lastIndex);

        // return the endpoint
        return result;
    }
}
