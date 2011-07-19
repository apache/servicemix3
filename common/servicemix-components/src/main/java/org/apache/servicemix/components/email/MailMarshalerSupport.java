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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.components.util.MarshalerSupport;
import org.apache.servicemix.expression.Expression;
import org.apache.servicemix.expression.PropertyExpression;

/**
 * A useful base class for mail marshalers.
 *
 * @version $Revision$
 */
public abstract class MailMarshalerSupport extends MarshalerSupport {

    private DateFormat dateFormat = DateFormat.getInstance();

    private Expression to = new PropertyExpression("org.apache.servicemix.email.to");
    private Expression cc = new PropertyExpression("org.apache.servicemix.email.cc");
    private Expression bcc = new PropertyExpression("org.apache.servicemix.email.bcc");
    private Expression from = new PropertyExpression("org.apache.servicemix.email.from", "noone@servicemix.org");
    private Expression text = new PropertyExpression("org.apache.servicemix.email.text");
    private Expression html = new PropertyExpression("org.apache.servicemix.email.html");
    private Expression subject = new PropertyExpression("org.apache.servicemix.email.subject", "Message from ServiceMix");
    private Expression replyTo = new PropertyExpression("org.apache.servicemix.email.replyTo");
    private Expression sentDate = new PropertyExpression("org.apache.servicemix.email.sentDate");
    private Expression attachments = new PropertyExpression("org.apache.servicemix.email.attachments");

    public Expression getTo() {
        return to;
    }

    public void setTo(Expression to) {
        this.to = to;
    }

    public Expression getCc() {
        return cc;
    }

    public void setCc(Expression cc) {
        this.cc = cc;
    }

    public Expression getBcc() {
        return bcc;
    }

    public void setBcc(Expression bcc) {
        this.bcc = bcc;
    }

    public Expression getFrom() {
        return from;
    }

    public void setFrom(Expression from) {
        this.from = from;
    }

    public Expression getText() {
        return text;
    }

    public void setText(Expression text) {
        this.text = text;
    }

    public Expression getHtml() {
        return html;
    }

    public void setHtml(Expression html) {
        this.html = html;
    }

    public Expression getSubject() {
        return subject;
    }

    public void setSubject(Expression subject) {
        this.subject = subject;
    }

    public Expression getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Expression replyTo) {
        this.replyTo = replyTo;
    }

    public Expression getSentDate() {
        return sentDate;
    }

    public void setSentDate(Expression sentDate) {
        this.sentDate = sentDate;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Expression getAttachments() {
        return attachments;
    }

    public void setAttachments(Expression attachments) {
        this.attachments = attachments;
    }
    // Implementation methods
    //-------------------------------------------------------------------------
    protected Date getSentDate(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException {
        return asDate(sentDate.evaluate(exchange, normalizedMessage));
    }

    protected String getSubject(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException {
        return asString(subject.evaluate(exchange, normalizedMessage));
    }

    protected String getText(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, TransformerException {
        String text = asString(this.text.evaluate(exchange, normalizedMessage));
        if (text == null) {
            // lets just turn the XML body to text
            Source content = normalizedMessage.getContent();
            if (content != null) {
                text = getTransformer().toString(content);
            }
        }
        return text;
    }
    
    protected String getHtml(MessageExchange exchange, NormalizedMessage normalizedMessage) throws MessagingException, TransformerException {
        return (this.html != null) ? asString(this.html.evaluate(exchange, normalizedMessage)) : null;
    }

    protected Address asAddress(Object value) throws AddressException {
        if (value instanceof Address) {
            return (Address) value;
        }
        if (value instanceof String) {
            return new InternetAddress((String) value);
        }
        if (value != null) {
            throw new IllegalArgumentException("Expression does not evaluate to an Address. Is of type: " + value.getClass().getName() + " with value: " + value);
        }
        return null;
    }

    protected Date asDate(Object value) {
        if (value instanceof Date) {
            return (Date) value;
        }
        if (value instanceof String) {
            String text = (String) value;
            try {
                return dateFormat.parse(text);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("Invalid date format for: " + text + ". Reason: " + e);
            }
        }
        if (value != null) {
            throw new IllegalArgumentException("Expression does not evaluate to a Date. Is of type: " + value.getClass().getName() + " with value: " + value);

        }
        return null;
    }

    protected Address[] asAddressArray(Object value) throws AddressException {
    	if (value instanceof String) {
    		Address[] addresses = InternetAddress.parse((String)value);
    		return addresses;
    	}
        if (value instanceof Address[]) {
            return (Address[]) value;
        }
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            Address[] answer = new Address[collection.size()];
            int i = 0;
            for (Iterator iter = collection.iterator(); iter.hasNext();) {
                answer[i++] = asAddress(iter.next());
            }
            return answer;
        }
        if (value != null) {
            throw new IllegalArgumentException("Expression does not evaluate to an Address[]. Is of type: " + value.getClass().getName() + " with value: " + value);
        }
        return null;
    }

    protected Object asStringOrStringArray(Object value) {
        if (value instanceof String) {
            return value;
        }
        if (value instanceof String[]) {
            return value;
        }
        if (value instanceof Collection) {
            Collection collection = (Collection) value;
            String[] answer = new String[collection.size()];
            int i = 0;
            for (Iterator iter = collection.iterator(); iter.hasNext();) {
                answer[i++] = asString(iter.next());
            }
            return answer;
        }
        if (value != null) {
            throw new IllegalArgumentException("Expression does not evaluate to a String[]. Is of type: " + value.getClass().getName() + " with value: " + value);
        }
        return null;
    }

}
