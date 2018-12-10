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

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOptionalOut;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;

/**
 * Represents a JBI endpoint you can communicate with
 * 
 * @version $Revision: $
 */
public interface Destination {

    /**
     * Indicates that the endpoint is no longer required
     */
    void close();

    // Simple one-way messaging API
    // -------------------------------------------------------------------------

    /**
     * Registers an asynchronous listener to messages so that they can be
     * processed as they become available
     */
    public void setMessageListener(MessageListener processor);

    /**
     * Creates a message ready to be sent to the endpoint via
     * {@link #sendMessage(NormalizedMessage)}
     * 
     * @return
     */
    Message createMessage();

    /**
     * Creates a message ready to be sent to the endpoint via
     * {@link #sendMessage(NormalizedMessage)}. The message has the given body
     * attached
     * 
     * @return
     * @throws MessagingException
     */
    Message createMessage(Object body) throws MessagingException;

    /**
     * Sends the message to the endpoint
     * 
     * @throws MessagingException
     */
    public void send(NormalizedMessage message) throws MessagingException;

    // Methods which can block for a long time
    // -------------------------------------------------------------------------

    /**
     * Receives a message from the endpoint.
     * 
     * Depending on the implementation this method could work with a declarative
     * transaction model to know when you have completed processing the message
     * correctly, otherwise for RobustInOnly then the message is acknowedged
     * immediately.
     * 
     * @throws MessagingException
     */
    Message receive() throws MessagingException;

    /**
     * Pulls a single message from the endpoint and processes it ensuring that
     * the message exchange is complete as soon as this method is called.
     * 
     * This method implicitly defines a unit of work around the message exchange
     */
    public void receive(MessageListener listener);

    /**
     * Performs a request response with the endpoint blocking until a response
     * is available.
     */
    Message waitForResponse(Message requestMessage);

    // Send and receive of message exchanges
    // -------------------------------------------------------------------------

    /**
     * Sends the message exchange to the endpoint.
     * 
     * @param exchange
     * @throws MessagingException
     */
    void send(MessageExchange exchange) throws MessagingException;

    /**
     * Sends the message exchange to the endpoint, blocking until the send has
     * completed.
     * 
     * @param exchange
     * @throws MessagingException
     * @return true if the exchange has been processed and returned by the
     *         servicing component, false otherwise.
     */
    boolean sendSync(MessageExchange exchange) throws MessagingException;

    /**
     * Receives an inbound message exchange, blocking forever until one is
     * available.
     * 
     * @return the received message exchange
     * @throws MessagingException
     */
    MessageExchange receiveExchange() throws MessagingException;

    /**
     * Receives an inbound message exchange, blocking until the given timeout
     * period.
     * 
     * @param timeout
     *            the maximum amount of time to wait for a message
     * @return the received message exchange or null if the timeout occurred.
     * @throws MessagingException
     */
    MessageExchange receiveExchange(long timeout) throws MessagingException;

    // Factory methods to make MessageExchange instances
    // -------------------------------------------------------------------------

    /**
     * Creates an {@link InOnly} (one way) message exchange.
     * 
     * @return the newly created message exchange
     * @throws MessagingException
     */
    InOnly createInOnlyExchange() throws MessagingException;

    /**
     * Creates an {@link InOut} (request-reply) message exchange.
     * 
     * @return the newly created message exchange
     * @throws MessagingException
     */
    InOut createInOutExchange() throws MessagingException;

    /**
     * Creates an {@link InOptionalOut} (optional request-reply) message
     * exchange.
     * 
     * @return the newly created message exchange
     * @throws MessagingException
     */
    InOptionalOut createInOptionalOutExchange() throws MessagingException;

    /**
     * Creates an {@link RobustInOnly} (one way) message exchange.
     * 
     * @return the newly created message exchange
     * @throws MessagingException
     */
    RobustInOnly createRobustInOnlyExchange() throws MessagingException;

    // API to explicitly acknowledge an inbound message as being complete
    // -------------------------------------------------------------------------
    
    /**
     * Marks this exchange as being complete; typically used for inbound
     * messages
     * 
     * @throws MessagingException
     */
    public void done(MessageExchange message) throws MessagingException;

    /**
     * Marks this exchange as being complete; typically used for inbound
     * messages
     * 
     * @throws MessagingException
     */
    public void done(Message message) throws MessagingException;

    /**
     * Marks this exchange as being failed with a fault
     * 
     * @throws MessagingException
     */
    public void fail(Fault fault) throws MessagingException;

    /**
     * Marks this exchange as being failed with an error
     * 
     * @throws MessagingException
     */
    public void fail(Message message, Exception fault) throws MessagingException;
    
    /**
     * Marks this exchange as being failed with an error
     * 
     * @throws MessagingException
     */
    public void fail(MessageExchange exchange, Exception fault) throws MessagingException;
}
