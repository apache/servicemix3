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

import java.util.ArrayList;
import java.util.List;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import junit.framework.Assert;

import org.apache.servicemix.client.Message;
import org.apache.servicemix.client.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple container for performing testing and rendezvous style code.
 * 
 * @version $Revision$
 */
public class MessageList extends Assert implements MessageListener {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(MessageList.class);

    private List messages = new ArrayList();

    private Object semaphore;

    public MessageList() {
        this(new Object());
    }

    public MessageList(Object semaphore) {
        this.semaphore = semaphore;
    }

    /**
     * @return all the messages on the list so far, clearing the buffer
     */
    public List flushMessages() {
        synchronized (semaphore) {
            List answer = new ArrayList(messages);
            messages.clear();
            return answer;
        }
    }

    public synchronized List getMessages() {
        synchronized (semaphore) {
            return new ArrayList(messages);
        }
    }

    public void addMessage(NormalizedMessage message) throws MessagingException {
        synchronized (semaphore) {
            messages.add(message);
            semaphore.notifyAll();
        }
    }

    public void addMessage(String message) throws MessagingException {
        synchronized (semaphore) {
            messages.add(message);
            semaphore.notifyAll();
        }
    }

    public int getMessageCount() {
        synchronized (semaphore) {
            return messages.size();
        }
    }

    public void waitForMessagesToArrive(int messageCount) {
        waitForMessagesToArrive(messageCount, 4000);
    }

    public void waitForMessagesToArrive(int messageCount, long baseTimeout) {
        LOGGER.info("Waiting for message to arrive");

        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < baseTimeout + 100 * messageCount) {
            try {
                if (hasReceivedMessages(messageCount)) {
                    break;
                }
                synchronized (semaphore) {
                    semaphore.wait(4000);
                }
            } catch (InterruptedException e) {
                LOGGER.info("Caught: {}", e);
            }
        }
        long end = System.currentTimeMillis() - start;

        LOGGER.info("End of wait for {} millis", end);
    }

    /**
     * Performs a testing assertion that the correct number of messages have
     * been received
     * 
     * @param messageCount
     */
    public void assertMessagesReceived(int messageCount) {
        waitForMessagesToArrive(messageCount);

        assertEquals("expected number of messages when received: " + getMessages(), messageCount, getMessageCount());
    }

    public boolean hasReceivedMessage() {
        return getMessageCount() > 0;
    }

    public boolean hasReceivedMessages(int messageCount) {
        return getMessageCount() >= messageCount;
    }

    // MessageListener interface
    // -------------------------------------------------------------------------
    public void onMessage(MessageExchange exchange, Message message) throws Exception {
        addMessage(message);
    }

}
