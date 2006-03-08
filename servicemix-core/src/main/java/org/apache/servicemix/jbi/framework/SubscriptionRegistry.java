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

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

import org.apache.servicemix.jbi.container.SubscriptionSpec;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Maintains a registry of the applicable subscriptions currently active for the
 * current components
 * 
 * @version $Revision$
 */
public class SubscriptionRegistry {

    private Map subscriptions = new ConcurrentHashMap();
    private Registry registry;
    
    public SubscriptionRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * @param subscription
     * @param endpoint
     */
    public void registerSubscription(SubscriptionSpec subscription, InternalEndpoint endpoint) {
        subscriptions.put(subscription, endpoint);
    }

    /**
     * @param subscription
     * @return the ServiceEndpoint
     */
    public InternalEndpoint deregisterSubscription(SubscriptionSpec subscription) {
        return (InternalEndpoint) subscriptions.remove(subscription);
    }
    
    
    /**
     * @param exchange 
     * @return a List of matching endpoints - can return null if no matches
     */
    public List getMatchingSubscriptionEndpoints(MessageExchangeImpl exchange) {
        List result = null;
        for (Iterator iter = subscriptions.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();

            SubscriptionSpec subscription = (SubscriptionSpec) entry.getKey();
            if (subscription.matches(registry,exchange)) {
                if (result == null) {
                    result = new ArrayList();
                }
                InternalEndpoint endpoint = (InternalEndpoint) entry.getValue();
                result.add(endpoint);
            }
        }
        return result;
    }

}
