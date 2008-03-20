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
package org.apache.servicemix.tck;

import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.CopyTransformer;
import org.apache.servicemix.components.util.MessageTransformer;
import org.apache.servicemix.components.util.PojoSupport;
import org.apache.servicemix.jbi.NoInMessageAvailableException;

/**
 * A simple POJO which just implements the {@link ComponentLifeCycle}
 * interface and is not dependent on any ServiceMix code.
 *
 * @version $Revision$
 */
public class ReceiverPojo extends PojoSupport implements ComponentLifeCycle, MessageExchangeListener, Receiver {

    public static final QName SERVICE = new QName("http://servicemix.org/example/", "receiver");
    public static final String ENDPOINT = "receiver";

    private MessageList messageList = new MessageList();
    private MessageTransformer messageTransformer = CopyTransformer.getInstance();

    public ReceiverPojo() {
        this(SERVICE, ENDPOINT);
    }

    public ReceiverPojo(QName service, String endpoint) {
        super(service, endpoint);
    }

    // MessageExchangeListener interface
    //-------------------------------------------------------------------------
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        NormalizedMessage inMessage = getInMessage(exchange);
        // Copy message to avoid possible closed stream exceptions
        // when using StreamSource
        NormalizedMessage copyMessage = exchange.createMessage();
        getMessageTransformer().transform(exchange, inMessage, copyMessage);
        messageList.addMessage(copyMessage);
        done(exchange);
    }

    /**
     * Returns the in message or throws an exception if there is no in message.
     */
    protected NormalizedMessage getInMessage(MessageExchange exchange) throws NoInMessageAvailableException {
        NormalizedMessage message = exchange.getMessage("in");
        if (message == null) {
            throw new NoInMessageAvailableException(exchange);
        }
        return message;
    }

    public MessageTransformer getMessageTransformer() {
        return messageTransformer;
    }
    
    // Receiver interface
    //-------------------------------------------------------------------------
    public MessageList getMessageList() {
        return messageList;
    }
}
