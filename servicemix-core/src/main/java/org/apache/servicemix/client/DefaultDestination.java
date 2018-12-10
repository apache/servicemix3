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
package org.apache.servicemix.client;

import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;

/**
 *
 * @version $Revision: $
 */
public class DefaultDestination implements Destination {

    private ServiceMixClient client;

    
    public void close() {
        // close any pending exchanges
        
        // TODO
    }

    public void setMessageListener(MessageListener processor) {
        // registers a processor into the endpoint??
        
        // TODO
        throw new RuntimeException("Unsupported operation");
    }

    public Message createMessage() {
        // TODO
        /*
        Message message = new NormalizedMessageImpl();
        return message;
        */
        return null;
    }

    public Message createMessage(Object body) throws MessagingException {
        // TODO
        /*
        Message message = new NormalizedMessageImpl();
        message.setBody(body);
        return message;
        */
        return null;
    }

    public void send(NormalizedMessage message) throws MessagingException {
        // TODO
        /*
        configureMessage(message);
        InOnly exchange = createInOnlyExchange();
        exchange.setInMessage(message);
        done(exchange);
        */
    }
    
    public Message receive() throws MessagingException {
        // TODO
        /*
        MessageExchange exchange = receiveExchange();
        Message message = (Message) exchange.getMessage("in");
        return message;
        */
        return null;
    }

    public void receive(MessageListener listener) {
        
    }

    public Message waitForResponse(Message requestMessage) {
        return null;
    }

    public void send(MessageExchange exchange) throws MessagingException {
        client.send(exchange);
    }

    public boolean sendSync(MessageExchange exchange) throws MessagingException {
        return client.sendSync(exchange);
    }

    public MessageExchange receiveExchange() throws MessagingException {
        return client.receive();
    }

    public MessageExchange receiveExchange(long timeout) throws MessagingException {
        return client.receive(timeout);
    }

    public InOnly createInOnlyExchange() throws MessagingException {
        return client.createInOnlyExchange();
    }

    public InOut createInOutExchange() throws MessagingException {
        return client.createInOutExchange();
    }

    public InOptionalOut createInOptionalOutExchange() throws MessagingException {
        return client.createInOptionalOutExchange();
    }

    public RobustInOnly createRobustInOnlyExchange() throws MessagingException {
        return client.createRobustInOnlyExchange();
    }


    public void done(Message message) throws MessagingException {
        done(message.getExchange());
    }

    public void done(MessageExchange exchange) throws MessagingException {
        client.done(exchange);
    }

    public void fail(Fault fault) throws MessagingException {
        Message message = (Message) fault;
        client.fail(message.getExchange(), fault);
    }

    public void fail(Message message, Exception fault) throws MessagingException {
        fail(message.getExchange(), fault);
    }
    
    public void fail(MessageExchange exchange, Exception fault) throws MessagingException {
        client.fail(exchange, fault);
    }
}
