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
package org.apache.servicemix;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

/**
 * If a Component implements this interface, MessageExchange will be delivered directly to the listener
 * asynchronously rather than using the usual asynchronous delivery with a thread used up per consuming
 * component.
 *
 * @version $Revision$
 */
public interface MessageExchangeListener {

    /**
     * MessageExchange passed directly to the listener instead of being queued
     *
     * @param exchange
     * @throws MessagingException
     */
    public void onMessageExchange(MessageExchange exchange) throws MessagingException;
}
