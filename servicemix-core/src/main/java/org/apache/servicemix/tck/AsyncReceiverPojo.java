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
package org.apache.servicemix.tck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

/**
 * A simple POJO which just implements the {@link javax.jbi.component.ComponentLifeCycle}
 * interface and is not dependent on any ServiceMix code.
 *
 * @version $Revision$
 */
public class AsyncReceiverPojo implements ComponentLifeCycle, Receiver, Runnable {

    public static final QName SERVICE = ReceiverPojo.SERVICE;
    public static final String ENDPOINT = ReceiverPojo.ENDPOINT;

    private static final Log log = LogFactory.getLog(AsyncReceiverPojo.class);

    private ComponentContext context;
    private MessageList messageList = new MessageList();
    private Thread runner;
    private boolean running;


    // ComponentLifeCycle interface
    //-------------------------------------------------------------------------
    public void init(ComponentContext context) throws JBIException {
        this.context = context;
        context.activateEndpoint(SERVICE, ENDPOINT);
    }

    public void shutDown() throws JBIException {
    }

    public synchronized void start() throws JBIException {
        if (!running) {
            running = true;
            runner = new Thread(this);
            runner.start();
        }
    }

    public synchronized void stop() throws JBIException {
        running = false;
    }

    public ObjectName getExtensionMBeanName() {
        return null;
    }


    // Receiver interface
    //-------------------------------------------------------------------------
    public MessageList getMessageList() {
        return messageList;
    }

    // Runnable interface
    //-------------------------------------------------------------------------
    public void run() {
        while (running) {
            try {
                DeliveryChannel deliveryChannel = context.getDeliveryChannel();
                log.info("about to do an accept on deliveryChannel: " + deliveryChannel);
                MessageExchange messageExchange = deliveryChannel.accept();
                log.info("received me: " + messageExchange);
                onMessageExchange(messageExchange);
            }
            catch (MessagingException e) {
                log.error("Failed to process inbound messages: " + e, e);
            }
        }
    }

    public void onMessageExchange(MessageExchange exchange) throws MessagingException {
        NormalizedMessage inMessage = exchange.getMessage("in");
        messageList.addMessage(inMessage);
        exchange.setStatus(ExchangeStatus.DONE);
        context.getDeliveryChannel().send(exchange);
    }

	public ComponentContext getContext() {
		return context;
	}
}
