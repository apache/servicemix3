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

import org.apache.servicemix.MessageExchangeListener;

import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;

/**
 * A {@link ComponentAdaptor} which also supports the direct
 * invocation mechanism via the {@link MessageExchangeListener}.
 *
 * @version $Revision$
 */
public class ComponentAdaptorMEListener extends ComponentAdaptor implements MessageExchangeListener {

    private MessageExchangeListener listener;

    public ComponentAdaptorMEListener(ComponentLifeCycle lifeCycle, MessageExchangeListener listener) {
        super(lifeCycle);
        this.listener = listener;
    }

    public ComponentAdaptorMEListener(ComponentLifeCycle lifeCycle, QName service, String endpoint, MessageExchangeListener listener) {
        super(lifeCycle, service, endpoint);
        this.listener = listener;
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        listener.onMessageExchange(exchange);
    }
}
