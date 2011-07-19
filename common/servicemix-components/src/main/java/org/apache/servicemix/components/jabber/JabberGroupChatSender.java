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
package org.apache.servicemix.components.jabber;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

/**
 * Sends one way messages to a Jabber {@link MultiUserChat} and receives inbound messages
 * from the chat
 *
 * @version $Revision$
 */
public class JabberGroupChatSender extends JabberComponentSupport {

    private MultiUserChat chat;
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
        try {
            if (chat == null) {
                this.chat = new MultiUserChat(this.connection, this.room);
                this.chat.join(this.user);
            }
        } catch (Exception ex) {
            logger.error("Unable to LOGGER into chatroom " + room, ex);
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
    public MultiUserChat getChat() {
        return chat;
    }

    public void setChat(MultiUserChat chat) {
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
    protected void process(MessageExchange messageExchange, NormalizedMessage normalizedMessage) throws Exception {
        Message message = this.chat.createMessage();
        message.setTo(this.room);
        getMarshaler().fromNMS(message, normalizedMessage);
        chat.sendMessage(message);
        done(messageExchange);
    }

}
