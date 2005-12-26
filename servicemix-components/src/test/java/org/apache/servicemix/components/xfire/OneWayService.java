package org.apache.servicemix.components.xfire;

import org.apache.servicemix.tck.MessageList;

import javax.jbi.messaging.MessagingException;

public class OneWayService {

    private MessageList messageList = new MessageList();
    
    public void receive(String msg) throws MessagingException {
        messageList.addMessage(msg);
    }

    public MessageList getMessageList() {
        return messageList;
    }
}
