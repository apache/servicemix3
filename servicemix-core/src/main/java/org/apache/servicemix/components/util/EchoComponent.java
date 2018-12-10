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
package org.apache.servicemix.components.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.MessageExchangeListener;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

/**
 * A simple, yet useful component for testing synchronous flows. Echos back Exchanges
 * 
 * @version $Revision$
 */
public class EchoComponent extends TransformComponentSupport implements MessageExchangeListener {
    private static final Log log = LogFactory.getLog(EchoComponent.class);
    
    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        getMessageTransformer().transform(exchange, in, out);
        log.info("Echoed back message: " + out);
        return true;
    }
}
