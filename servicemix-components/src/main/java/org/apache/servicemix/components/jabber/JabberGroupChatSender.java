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

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.TransformerException;

import org.jivesoftware.smack.GroupChat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

/**
 * Sends one way messages to a Jabber {@link GroupChat} and receives inbound messages
 * from the chat
 *
 * @version $Revision$
 */
public class JabberGroupChatSender extends JabberComponentSupport {

    private GroupChat chat;
    private String room;

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (chat == null) {
            if (room == null) {
                throw new IllegalArgumentException("You must specify the room property");
            }
        }
    }

    public void start() throws JBIException {
        super.start();
        if (chat == null) {
            chat = getConnection().createGroupChat(room);
        }
    }

    public void stop() throws JBIException {
        if (chat != null) {
            chat.leave();
            chat = null;
        }
        super.stop();
    }

    // Properties
    //-------------------------------------------------------------------------
    public GroupChat getChat() {
        return chat;
    }

    public void setChat(GroupChat chat) {
        this.chat = chat;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange messageExchange, NormalizedMessage normalizedMessage) throws MessagingException {
        try {
            Message message = chat.createMessage();
            getMarshaler().fromNMS(message, normalizedMessage);
            chat.sendMessage(message);
            done(messageExchange);
        }
        catch (TransformerException e) {
            throw new MessagingException(e);
        }
        catch (XMPPException e) {
            throw new MessagingException(e);
        }
    }

}
