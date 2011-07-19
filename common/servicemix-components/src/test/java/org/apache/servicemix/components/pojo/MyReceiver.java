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
package org.apache.servicemix.components.pojo;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.tck.MessageList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Revision$
 */

// START SNIPPET: receive 
public class MyReceiver implements ComponentLifeCycle, MessageExchangeListener {

	private static final Logger logger = LoggerFactory.getLogger(MyReceiver.class);
	
    private ComponentContext context;
    private ObjectName extensionMBeanName;
    private MessageList messageList = new MessageList();

    // ComponentLifeCycle interface
    //-------------------------------------------------------------------------
    public ObjectName getExtensionMBeanName() {
        return extensionMBeanName;
    }

    public void init(ComponentContext context) throws JBIException {
        this.context = context;

        // Lets activate myself
        context.activateEndpoint(new QName("http://servicemix.org/cheese/", "receiver"), "receiver");
    }

    public void shutDown() throws JBIException {
    }

    public void start() throws JBIException {
    }

    public void stop() throws JBIException {
    }

    // MessageExchangeListener interface
    //-------------------------------------------------------------------------
    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        logger.info("Received message {}", exchange);
        NormalizedMessage message = exchange.getMessage("in");
        getMessageList().addMessage(message);
        exchange.setStatus(ExchangeStatus.DONE);
        context.getDeliveryChannel().send(exchange);
    }

    // Properties
    //-------------------------------------------------------------------------
    public void setExtensionMBeanName(ObjectName extensionMBeanName) {
        this.extensionMBeanName = extensionMBeanName;
    }

    public MessageList getMessageList() {
        return messageList;
    }

    public void setMessageList(MessageList messageList) {
        this.messageList = messageList;
    }

}
// END SNIPPET: receive
