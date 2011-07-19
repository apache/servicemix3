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

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;

/**
 * Sends one way messages to a private Jabber {@link Chat}
 *
 * @version $Revision$
 */
public class JabberChatSender extends JabberComponentSupport {

    private Chat chat;
    private String participant;

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (chat == null) {
            if (participant == null) {
                throw new IllegalArgumentException("You must specify the participant property");
            }
        }
    }

    public void start() throws JBIException {
        super.start();
        // now register listener for new packets
        if (this.connection != null && this.connection.isConnected()) {
            // if the user specified a chat room to join we do this here
            if (this.chat != null) {
                this.chat = this.connection.getChatManager().createChat(this.participant, null);
            }
        }
    }

    public void stop() throws JBIException {
        chat = null;
        super.stop();
    }

    // Properties
    //-------------------------------------------------------------------------
    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange messageExchange, NormalizedMessage normalizedMessage) throws Exception {
        Message message = message = new Message(this.participant, Message.Type.normal);
        getMarshaler().fromNMS(message, normalizedMessage);
        chat.sendMessage(message);
        done(messageExchange);
    }

}
