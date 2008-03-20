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
package org.apache.servicemix.jbi.event;

import java.util.EventObject;

import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;

/**
 * Event sent when an exchange is received or accepted by a component.
 * 
 * @author gnodet
 */
public class ExchangeEvent extends EventObject {

    public static final int EXCHANGE_SENT = 0;
    public static final int EXCHANGE_ACCEPTED = 1;
    
    private static final long serialVersionUID = -8349785806912334977L;

    private MessageExchange exchange;
    private int type;
    
    public ExchangeEvent(MessageExchange exchange, int type) {
        super(exchange);
        this.exchange = exchange;
        this.type = type;
    }
    
    public MessageExchange getExchange() {
        return exchange;
    }
    
    public int getType() {
        return type;
    }

    /**
     * Returns the source context which created the message exchange
     */
    public ComponentContextImpl getExchangeSourceContext() {
        return ((MessageExchangeImpl) exchange).getSourceContext();
    }

}
