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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

/**
 * An exception thrown if a component cannot find an expected output message.
 *
 * @version $Revision$
 */
public class NoOutMessageAvailableException extends MessagingException {
    private MessageExchange messageExchange;

    public NoOutMessageAvailableException(MessageExchange messageExchange) {
        super("No out message available for message exchange: " + messageExchange);
        this.messageExchange = messageExchange;
    }

    /**
     * The message exchange on which the problem occurred
     */
    public MessageExchange getMessageExchange() {
        return messageExchange;
    }
}
