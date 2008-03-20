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
package org.apache.servicemix.jbi;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

/**
 * An exception thrown when a synchronous exchange has timed out.
 *
 * @version $Revision$
 */
public class ExchangeTimeoutException extends MessagingException {
    
    private final MessageExchange exchange;

    public ExchangeTimeoutException(MessageExchange exchange) {
        super("Exchange has timed out: " + exchange);
        this.exchange = exchange;
    }

    /**
     * Returns th exchange
     */
    public MessageExchange getExchange() {
        return exchange;
    }
}
