/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.components.pojo;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.servicemix.MessageExchangeListener;
import org.servicemix.tck.MessageList;

/**
 * @version $Revision$
 */
// START SNIPPET: receive
public class PojoReceiver implements MessageExchangeListener {
    private MessageList messageList = new MessageList();

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        NormalizedMessage message = exchange.getMessage("in");
        getMessageList().addMessage(message);
    }

    public MessageList getMessageList() {
        return messageList;
    }
}
// END SNIPPET: receive
