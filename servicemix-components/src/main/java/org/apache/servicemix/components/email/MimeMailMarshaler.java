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
package org.apache.servicemix.components.email;

import java.io.IOException;
import java.util.Date;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * The default marshaller from the {@link NormalizedMessage} to a Mime email using
 * expressions for each field required on the email.
 *
 * @version $Revision$
 */
public class MimeMailMarshaler extends MailMarshalerSupport {


    /**
     * Populates the MessageExchange with values extracted from the mail message using expressions.
     *
     * @param exchange          the JBI message exchange
     * @param normalizedMessage the normalized message from JBI
     * @param mimeMessage       the mime email
     * @throws javax.mail.MessagingException if the message could not be constructed or there was an error creating an address
     */
	public void prepareExchange(MessageExchange exchange, NormalizedMessage normalizedMessage, MimeMessage mimeMessage) throws javax.mail.MessagingException {
		String from 	= InternetAddress.toString(mimeMessage.getFrom());
		String to 		= InternetAddress.toString(mimeMessage.getRecipients(Message.RecipientType.TO));
		String cc 		= InternetAddress.toString(mimeMessage.getRecipients(Message.RecipientType.CC));
		String replyTo 	= InternetAddress.toString(mimeMessage.getReplyTo());
		String sentDate = getDateFormat().format(mimeMessage.getSentDate());
		String text;
		try {
			//TODO: Add Message Attachments and allow multipart messages.
			text 	= asString(mimeMessage.getContent());
		} catch (IOException e) {
			throw new javax.mail.MessagingException("Error while fetching content",e);
		}
		
		normalizedMessage.setProperty("org.apache.servicemix.email.from",from);
		normalizedMessage.setProperty("org.apache.servicemix.email.to",to);
		normalizedMessage.setProperty("org.apache.servicemix.email.cc",cc);
		normalizedMessage.setProperty("org.apache.servicemix.email.text",text);
		normalizedMessage.setProperty("org.apache.servicemix.email.replyTo",replyTo);
		normalizedMessage.setProperty("org.apache.servicemix.email.sentDate",sentDate);
		
		//TODO: Change this to something that makes more sense
		try {
			normalizedMessage.setContent(new StringSource(text));
		} catch (MessagingException e) {
			throw new javax.mail.MessagingException("Error while setting content on normalized message",e);
		}
	}
	
    /**
     * Populates the mime email message with values extracted from the message exchange using expressions.
     *
     * @param mimeMessage       the mime email
     * @param exchange          the JBI message exchange
     * @param normalizedMessage the normalized message from JBI
     * @throws javax.mail.MessagingException if the message could not be constructed or there was an error creating an address
     */
    public void prepareMessage(MimeMessage mimeMessage, MessageExchange exchange, NormalizedMessage normalizedMessage) throws javax.mail.MessagingException {
        try {
            Address to = getTo(exchange, normalizedMessage);
            if (to != null) {
                mimeMessage.setRecipient(Message.RecipientType.TO, to);
            }
            Address cc = getCc(exchange, normalizedMessage);
            if (cc != null) {
                mimeMessage.setRecipient(Message.RecipientType.CC, cc);
            }
            Address bcc = getBcc(exchange, normalizedMessage);
            if (bcc != null) {
                mimeMessage.setRecipient(Message.RecipientType.BCC, bcc);
            }
            Address from = getFrom(exchange, normalizedMessage);
            if (from != null) {
                mimeMessage.setFrom(from);
            }
            String text = getText(exchange, normalizedMessage);
            String html = getHtml(exchange, normalizedMessage);
            if ((text != null) && (html == null)) {
                mimeMessage.setText(text);
            }
            else if ((text != null) && (html != null)) {
                MimeMultipart content = new MimeMultipart("alternative");
                MimeBodyPart textBodyPart = new MimeBodyPart();
                MimeBodyPart htmlBodyPart = new MimeBodyPart();
                textBodyPart.setText(text);
                htmlBodyPart.setContent(html, "text/html");
                content.addBodyPart(textBodyPart);
                content.addBodyPart(htmlBodyPart);
                
                mimeMessage.setContent(content);
            }
            String subject = getSubject(exchange, normalizedMessage);
            if (subject != null) {
                mimeMessage.setSubject(subject);
            }
            Date sentDate = getSentDate(exchange, normalizedMessage);
            if (sentDate != null) {
                mimeMessage.setSentDate(sentDate);
            }
            Address[] replyTo = getReplyTo(exchange, normalizedMessage);
            if (replyTo != null) {
                mimeMessage.setReplyTo(replyTo);
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
    protected Address getFrom(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddress(getFrom().evaluate(exchange, normalizedMessage));
    }

    protected Address getTo(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddress(getTo().evaluate(exchange, normalizedMessage));
    }

    protected Address getCc(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddress(getCc().evaluate(exchange, normalizedMessage));
    }

    protected Address getBcc(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddress(getBcc().evaluate(exchange, normalizedMessage));
    }

    protected Address[] getReplyTo(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddressArray(getReplyTo().evaluate(exchange, normalizedMessage));
    }

}
