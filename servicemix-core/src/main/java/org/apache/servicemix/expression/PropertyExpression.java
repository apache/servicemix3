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
 * A simple expression which returns the value of a property on the message.
 *
 * @version $Revision$
 */
public class PropertyExpression implements Expression {
    private String property;
    private Object defaultValue;

    public PropertyExpression(String property) {
        this.property = property;
    }

    public PropertyExpression(String property, Object defaultValue) {
        this.property = property;
        this.defaultValue = defaultValue;
    }

    public Object evaluate(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
        Object answer = message.getProperty(property);
        if (answer == null) {
            answer = exchange.getProperty(property);
            if (answer == null) {
                answer = defaultValue;
            }
        }
        return answer;
    }
}
