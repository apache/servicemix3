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
package org.apache.servicemix.jbi.event;

import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;

import java.util.EventObject;

import javax.jbi.messaging.MessageExchange;

public class ExchangeEvent extends EventObject {

    private static final long serialVersionUID = -8349785806912334977L;

    public ExchangeEvent(MessageExchange exchange) {
        super(exchange);
    }
    
    public MessageExchange getExchange() {
        return (MessageExchange) getSource();
    }

    /**
     * Returns the source context which created the message exchange
     */
    public ComponentContextImpl getExchangeSourceContext() {
        return getExchangeImpl().getSourceContext();
    }
    
    protected MessageExchangeImpl getExchangeImpl() {
        return (MessageExchangeImpl) getSource();
    }
}
