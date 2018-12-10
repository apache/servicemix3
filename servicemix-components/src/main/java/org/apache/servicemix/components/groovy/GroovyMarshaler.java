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
package org.apache.servicemix.components.groovy;

import org.apache.servicemix.jbi.messaging.DefaultMarshaler;
import org.apache.servicemix.jbi.messaging.PojoMarshaler;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.MessagingException;

import groovy.lang.GString;

/**
 * A {@link PojoMarshaler} capable of handling <a href="http://groovy.codehaus.org/">Groovy</a>
 * specific types.
 *
 * @version $Revision$
 */
public class GroovyMarshaler extends DefaultMarshaler {

    public void marshal(MessageExchange exchange, NormalizedMessage message, Object body) throws MessagingException {
        if (body instanceof GString) {
            body = body.toString();
        }

        // TODO handle Node
        super.marshal(exchange, message, body);
    }
}
