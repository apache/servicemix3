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
package org.apache.servicemix.tck;

import java.util.Iterator;
import java.util.Map;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;

import junit.framework.Assert;

import org.apache.servicemix.jbi.event.ExchangeEvent;
import org.apache.servicemix.jbi.event.ExchangeListener;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;

public class ExchangeCompletedListener extends Assert implements ExchangeListener {

    private Map exchanges = new ConcurrentHashMap();
    
    public void exchangeSent(ExchangeEvent event) {
        exchanges.put(event.getExchange().getExchangeId(), event.getExchange());
    }
    
    public void assertExchangeCompleted() throws Exception {
        Thread.sleep(50);
        for (Iterator it = exchanges.values().iterator(); it.hasNext();) {
            MessageExchange me = (MessageExchange) it.next();
            assertTrue("Exchange is ACTIVE", me.getStatus() != ExchangeStatus.ACTIVE);
        }
    }

}
