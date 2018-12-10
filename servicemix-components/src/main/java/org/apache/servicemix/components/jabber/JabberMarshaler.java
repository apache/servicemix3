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
package org.apache.servicemix.components.jabber;

import org.apache.servicemix.jbi.jaxp.SourceMarshaler;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import java.util.Date;
import java.util.Iterator;

/**
 * Marshals Jabber messages into and out of NMS messages
 *
 * @version $Revision$
 */
public class JabberMarshaler {
    private SourceMarshaler sourceMarshaler;

    public JabberMarshaler() {
        this(new SourceMarshaler());
    }

    public JabberMarshaler(SourceMarshaler sourceMarshaler) {
        this.sourceMarshaler = sourceMarshaler;
    }

    /**
     * Marshals the Jabber message into an NMS message
     *
     * @throws MessagingException
     */
    public void toNMS(NormalizedMessage normalizedMessage, Packet packet) throws MessagingException {
        addNmsProperties(normalizedMessage, packet);
        if (packet instanceof Message) {
            Message message = (Message) packet;
            Source source = sourceMarshaler.asSource(message.getBody());
            normalizedMessage.setContent(source);
        }

        // lets add the packet to the NMS
        normalizedMessage.setProperty("org.apache.servicemix.jabber.packet", packet);
    }

    /**
     * Marshals from the Jabber message to the normalized message
     *
     * @param message
     * @param normalizedMessage
     * @throws TransformerException
     */
    public void fromNMS(Message message, NormalizedMessage normalizedMessage) throws TransformerException {
        // lets create a text message
        String xml = messageAsString(normalizedMessage);
        message.setBody(xml);
        addJabberProperties(message, normalizedMessage);
    }

    // Properties
    //-------------------------------------------------------------------------
    public SourceMarshaler getSourceMarshaller() {
        return sourceMarshaler;
    }

    public void setSourceMarshaller(SourceMarshaler sourceMarshaler) {
        this.sourceMarshaler = sourceMarshaler;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Converts the inbound message to a String that can be sent
     */
    protected String messageAsString(NormalizedMessage normalizedMessage) throws TransformerException {
        return sourceMarshaler.asString(normalizedMessage.getContent());
    }

    /**
     * Appends properties on the NMS to the JMS Message
     */
    protected void addJabberProperties(Message message, NormalizedMessage normalizedMessage) {
        for (Iterator iter = normalizedMessage.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = normalizedMessage.getProperty(name);
            if (shouldIncludeHeader(normalizedMessage, name, value)) {
                message.setProperty(name, value);
            }
        }
    }

    protected void addNmsProperties(NormalizedMessage normalizedMessage, Packet message) {
        Iterator iter = message.getPropertyNames();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            Object value = message.getProperty(name);
            normalizedMessage.setProperty(name, value);
        }
    }

    /**
     * Decides whether or not the given header should be included in the JMS message.
     * By default this includes all suitable typed values
     */
    protected boolean shouldIncludeHeader(NormalizedMessage normalizedMessage, String name, Object value) {
        return value instanceof String || value instanceof Number || value instanceof Date;
    }

}
