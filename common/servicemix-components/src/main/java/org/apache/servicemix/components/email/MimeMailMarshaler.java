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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default marshaler from the {@link NormalizedMessage} to a Mime email using
 * expressions for each field required on the email.
 *
 * @version $Revision$
 */
public class MimeMailMarshaler extends MailMarshalerSupport {

    private static Logger logger = LoggerFactory.getLogger(MimeMailPoller.class);

    /**
     * Populates the MessageExchange with values extracted from the mail message using expressions.
     *
     * @param exchange          the JBI message exchange
     * @param normalizedMessage the normalized message from JBI
     * @param mimeMessage       the mime email
     * @throws javax.mail.MessagingException if the message could not be constructed or there was an error creating an address
     */
	public void prepareExchange(MessageExchange exchange, NormalizedMessage normalizedMessage, MimeMessage mimeMessage) throws javax.mail.MessagingException {
		String from     = InternetAddress.toString(mimeMessage.getFrom());
		String to 		= InternetAddress.toString(mimeMessage.getRecipients(Message.RecipientType.TO));
		String cc 		= InternetAddress.toString(mimeMessage.getRecipients(Message.RecipientType.CC));
		String replyTo 	= InternetAddress.toString(mimeMessage.getReplyTo());
		String sentDate = getDateFormat().format(mimeMessage.getSentDate());
		String text = null;
        String html = null;
        MimeMultipart mp = null;
        Object content = null;
        Object subContent = null;
        MimeMultipart subMP = null;

		try {
            content = mimeMessage.getContent();
            if (content instanceof String)
                // simple mail
                text = asString(content);
            else if (content instanceof MimeMultipart) {
                // mail with attachment
                mp = (MimeMultipart)content;
                int nbMP = mp.getCount();
                for (int i=0; i < nbMP; i++) {
                    Part part = mp.getBodyPart(i);
                    String disposition = part.getDisposition();
                    if ((disposition != null) &&
                        ((disposition.equals(Part.ATTACHMENT) ||
                         (disposition.equals(Part.INLINE))))) {
                        //Parts marked with a disposition of Part.ATTACHMENT from part.getDisposition() are clearly attachments
                        DataHandler att = part.getDataHandler();
                        normalizedMessage.addAttachment(att.getName(), att);
                    } else {
                        MimeBodyPart mbp = (MimeBodyPart)part;
                        if (mbp.isMimeType("text/plain")) {
                          // Handle plain
                            text = (String)mbp.getContent();
                        } else if (mbp.isMimeType("text/html")) {
                          // Handle html contents
                            html = (String)mbp.getContent();
                        } else if (mbp.isMimeType("multipart/related")){
                            // Special case for multipart/related message type
                            subContent = mbp.getContent();
                            if (subContent instanceof MimeMultipart) {
                                subMP = (MimeMultipart)subContent;
                                int nbsubMP = subMP.getCount();
                                for (int j=0; j < nbsubMP; j++) {
                                    MimeBodyPart subMBP = (MimeBodyPart)part;
                                    // add a property into the normalize message
                                    normalizedMessage.setProperty("org.apache.servicemix.email.alternativeContent" + j, subMBP.getContent());
                                }
                            }
                        } else // strange mail...LOGGER a warning
                            logger.warn("Some mail contents can not be traited and is not include into message");
                    }
                }
            } else { // strange mail...LOGGER a warning
                logger.warn("Some mail contents can not be traited and is not include into message");
            }
		} catch (MessagingException e) {
			throw new javax.mail.MessagingException("Error while setting content on normalized message",e);
		} catch (IOException e) {
            throw new javax.mail.MessagingException("Error while fetching content",e);
        }

        normalizedMessage.setProperty("org.apache.servicemix.email.from", from);
        normalizedMessage.setProperty("org.apache.servicemix.email.to", to);
        normalizedMessage.setProperty("org.apache.servicemix.email.cc", cc);
        normalizedMessage.setProperty("org.apache.servicemix.email.text", text);
        normalizedMessage.setProperty("org.apache.servicemix.email.replyTo", replyTo);
        normalizedMessage.setProperty("org.apache.servicemix.email.sentDate", sentDate);
        normalizedMessage.setProperty("org.apache.servicemix.email.html", html);

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
            Address[] to = getTo(exchange, normalizedMessage);
            if (to != null) {
                mimeMessage.setRecipients(Message.RecipientType.TO, to);
            }
            Address[] cc = getCc(exchange, normalizedMessage);
            if (cc != null) {
                mimeMessage.setRecipients(Message.RecipientType.CC, cc);
            }
            Address[] bcc = getBcc(exchange, normalizedMessage);
            if (bcc != null) {
                mimeMessage.setRecipients(Message.RecipientType.BCC, bcc);
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

            // add attachment from message and properties
            HashMap attachments = this.getAttachments(exchange, normalizedMessage);
            if (attachments != null) {
                Set attNames = attachments.keySet();
                Iterator itAttNames = attNames.iterator();
                if (itAttNames.hasNext()) { // there is at least one attachment
                    // Create the message part
                    BodyPart messageBodyPart = new MimeBodyPart();
                    // Fill the message
                    messageBodyPart.setText(text);
                    // Create a Multipart
                    Multipart multipart = new MimeMultipart();
                    // Add part one
                    multipart.addBodyPart(messageBodyPart);
                    while (itAttNames.hasNext()) {
                        String oneAttachmentName = (String)itAttNames.next();
                        // Create another body part
                        messageBodyPart = new MimeBodyPart();
                        // Set the data handler to the attachment
                        messageBodyPart.setDataHandler(new DataHandler((DataSource)attachments.get(oneAttachmentName)));
                        // Set the filename
                        messageBodyPart.setFileName(oneAttachmentName);
                        // Set Disposition
                        messageBodyPart.setDisposition(Part.ATTACHMENT);
                        // Add part to multipart
                        multipart.addBodyPart(messageBodyPart);
                    }
                    // Put parts in message
                    mimeMessage.setContent(multipart);
                 }
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

    protected Address[] getTo(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddressArray(getTo().evaluate(exchange, normalizedMessage));
    }

    protected Address[] getCc(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddressArray(getCc().evaluate(exchange, normalizedMessage));
    }

    protected Address[] getBcc(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddressArray(getBcc().evaluate(exchange, normalizedMessage));
    }

    protected Address[] getReplyTo(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, AddressException {
        return asAddressArray(getReplyTo().evaluate(exchange, normalizedMessage));
    }

    protected HashMap getAttachments(MessageExchange exchange, NormalizedMessage normalizedMessage) {
        HashMap attachments = new HashMap();
        String filePath = "";
        String oneAttachmentName = "";
        try {
            // get attachment from property org.apache.servicemix.email.attachment
            String listAttachment = (String)getAttachments().evaluate(exchange, normalizedMessage);
            if (StringUtils.isNotBlank(listAttachment)) {
                StringTokenizer st = new StringTokenizer(listAttachment, ",");
                if (st != null) {
                    while (st.hasMoreTokens()) {
                        filePath = st.nextToken();
                        File file = new File(filePath);
                        attachments.put(file.getName(), new FileDataSource(file));
                    }
                }
            }
        } catch (MessagingException e) {
            logger.warn("file {} not found for attachment: ", filePath, e);
        }
        // get attachment from Normalize Message
        Set attNames = normalizedMessage.getAttachmentNames();
        Iterator itAttNames = attNames.iterator();
        while (itAttNames.hasNext()) {
            oneAttachmentName = (String)itAttNames.next();
            DataSource oneAttchmentInputString = normalizedMessage.getAttachment(oneAttachmentName).getDataSource();
            attachments.put(oneAttachmentName, oneAttchmentInputString);
        }

        return attachments;
    }

}
