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
package org.apache.servicemix.expression;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
 
/**
 * An expression strategy for extracting or calculating some value from a message.
 *
 * @version $Revision$
 */
public interface Expression {

    /**
     * Evaluates the expression on the given exchange and message.
     *
     * @param exchange the message exchange
     * @param message the message, typically an inbound message
     * @return the value of the expression
     */
    public Object evaluate(MessageExchange exchange, NormalizedMessage message) throws MessagingException ;
}
