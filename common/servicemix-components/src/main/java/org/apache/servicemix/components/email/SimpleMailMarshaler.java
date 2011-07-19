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

import java.util.Date;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.internet.AddressException;
import javax.xml.transform.TransformerException;

import org.springframework.mail.SimpleMailMessage;

/**
 * The default marshaler from the {@link NormalizedMessage} to a {@link SimpleMailMessage} using
 * expressions for each field required on the email.
 *
 * @version $Revision$
 */
public class SimpleMailMarshaler extends MailMarshalerSupport{

    /**
     * Populates the mime email message with values extracted from the message exchange using expressions.
     *
     * @param mailMessage       the mime email
     * @param exchange          the JBI message exchange
     * @param normalizedMessage the normalized message from JBI
     * @throws javax.mail.MessagingException if the message could not be constructed or there was an error creating an address
     */
    public void prepareMessage(SimpleMailMessage mailMessage, MessageExchange exchange, NormalizedMessage normalizedMessage) throws javax.mail.MessagingException {
        try {
            Object to = getTo(exchange, normalizedMessage);
            if (to != null) {
                if (to instanceof String) {
                    mailMessage.setTo((String) to);
                }
                else {
                    mailMessage.setTo((String[]) to);
                }
            }
            Object cc = getCc(exchange, normalizedMessage);
            if (cc != null) {
                if (cc instanceof String) {
                    mailMessage.setCc((String) cc);
                }
                else {
                    mailMessage.setCc((String[]) cc);
                }
            }
            Object bcc = getBcc(exchange, normalizedMessage);
            if (bcc != null) {
                if (bcc instanceof String) {
                    mailMessage.setBcc((String) bcc);
                }
                else {
                    mailMessage.setBcc((String[]) bcc);
                }
            }
            String from = getFrom(exchange, normalizedMessage);
            if (from != null) {
                mailMessage.setFrom(from);
            }
            String replyTo = getReplyTo(exchange, normalizedMessage);
            if (replyTo != null) {
                mailMessage.setReplyTo(replyTo);
            }

            String text = getText(exchange, normalizedMessage);
            if (text != null) {
                mailMessage.setText(text);
            }
            String subject = getSubject(exchange, normalizedMessage);
            if (subject != null) {
                mailMessage.setSubject(subject);
            }
            Date sentDate = getSentDate(exchange, normalizedMessage);
            if (sentDate != null) {
                mailMessage.setSentDate(sentDate);
            }
        }
        catch (MessagingException e) {
            throw new javax.mail.MessagingException(e.getMessage(), e);
        }
        catch (TransformerException e) {
            throw new javax.mail.MessagingException(e.getMessage(), e);
        }
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected String getFrom(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asString(getFrom().evaluate(exchange, normalizedMessage));
    }

    protected String getReplyTo(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException {
        return asString(getReplyTo().evaluate(exchange, normalizedMessage));
    }

    protected Object getTo(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asStringOrStringArray(getTo().evaluate(exchange, normalizedMessage));
    }

    protected Object getCc(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asStringOrStringArray(getCc().evaluate(exchange, normalizedMessage));
    }

    protected Object getBcc(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asStringOrStringArray(getBcc().evaluate(exchange, normalizedMessage));
    }

}
