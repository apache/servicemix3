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
package org.apache.servicemix.components.email;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import junit.framework.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * A stub {@link JavaMailSender} useful for testing.
 *
 * @version $Revision$
 */
public class StubJavaMailSender extends Assert implements JavaMailSender {

    private static transient Logger logger = LoggerFactory.getLogger(StubJavaMailSender.class);

    private List messages = new ArrayList();
    private Object semaphore = new Object();

    public void send(MimeMessage mimeMessage) throws MailException {
        addMessage(mimeMessage);
    }

    public void send(MimeMessagePreparator preparator) throws MailException {
        try {
            MimeMessage message = createMimeMessage();
            preparator.prepare(message);
            send(message);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MimeMessage createMimeMessage() {
        return new MimeMessage((Session) null);
    }

    public MimeMessage createMimeMessage(InputStream inputStream) throws MailException {
        return createMimeMessage();
    }

    public void send(MimeMessage[] messages) throws MailException {
        for (int i = 0; i < messages.length; i++) {
            MimeMessage message = messages[i];
            send(message);
        }
    }

    public void send(MimeMessagePreparator[] preparators) throws MailException {
        for (int i = 0; i < preparators.length; i++) {
            MimeMessagePreparator preparator = preparators[i];
            send(preparator);
        }
    }

    public void send(SimpleMailMessage simpleMailMessage) throws MailException {
        addMessage(simpleMailMessage);
    }

    public void send(SimpleMailMessage[] simpleMailMessages) throws MailException {
        for (int i = 0; i < simpleMailMessages.length; i++) {
            SimpleMailMessage simpleMailMessage = simpleMailMessages[i];
            send(simpleMailMessage);
        }
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

    public void addMessage(Object message) {
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
        logger.info("Waiting for message to arrive");

        long start = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i++) {
            try {
                if (hasReceivedMessages(messageCount)) {
                    break;
                }
                synchronized (semaphore) {
                    semaphore.wait(4000);
                }
            }
            catch (InterruptedException e) {
                logger.info("Caught: ", e);
            }
        }
        long end = System.currentTimeMillis() - start;

        logger.info("End of wait for {} millis", end);
    }

    /**
     * Performs a testing assertion that the correct number of messages have been received
     *
     * @param messageCount
     */
    public void assertMessagesReceived(int messageCount) {
        waitForMessagesToArrive(messageCount);

        assertEquals("expected number of messages", messageCount, getMessageCount());
        logger.info("Received messages: {}", getMessages());
    }

    public boolean hasReceivedMessage() {
        return getMessageCount() == 0;
    }

    public boolean hasReceivedMessages(int messageCount) {
        return getMessageCount() >= messageCount;
    }

}
