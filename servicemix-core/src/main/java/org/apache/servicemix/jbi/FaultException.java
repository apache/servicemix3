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
package org.apache.servicemix.jbi;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

/**
 * An exception which is thrown if a fault occurs in some JBI client invocation.
 *
 * @version $Revision$
 */
public class FaultException extends MessagingException {
    private MessageExchange exchange;
    private Fault fault;

    public static FaultException newInstance(MessageExchange exchange) throws NoFaultAvailableException {
        Fault fault = exchange.getFault();
        if (fault == null) {
            throw new NoFaultAvailableException(exchange);
        }
        else {
            return new FaultException("Fault occurred invoking server: " + fault, exchange, fault);
        }
    }

    public FaultException(String text, MessageExchange exchange, Fault fault) {
        super(text);
        this.exchange = exchange;
        this.fault = fault;
    }

    public MessageExchange getExchange() {
        return exchange;
    }

    public Fault getFault() {
        return fault;
    }

}
