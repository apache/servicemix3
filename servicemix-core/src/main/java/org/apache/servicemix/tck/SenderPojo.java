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
import org.apache.servicemix.components.util.PojoSupport;
import org.apache.servicemix.jbi.jaxp.StringSource;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$
 */
public class SenderPojo extends PojoSupport implements ComponentLifeCycle, Sender {

    public static final QName SERVICE = new QName("http://servicemix.org/example/", "sender");
    public static final String ENDPOINT = "sender";

    private static final Log log = LogFactory.getLog(SenderPojo.class);

    private QName interfaceName;
    private int numMessages = 10;
    private ComponentContext context;
    private List messages = new ArrayList();
    boolean done = false;

    public SenderPojo() {
        this(ReceiverPojo.SERVICE);
    }

    public SenderPojo(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void init(ComponentContext context) throws JBIException {
        this.context = context;
        context.activateEndpoint(SERVICE, ENDPOINT);
    }

    public int messagesReceived() {
        return messages.size();
    }

    public void sendMesssages() throws MessagingException {
        sendMessages(numMessages);
    }

    public void sendMessages(int messageCount) throws MessagingException {
    	sendMessages(messageCount, true);
    }
    
    public void sendMessages(int messageCount, boolean sync) throws MessagingException {
        System.out.println("Looking for services for interface: " + interfaceName);

        ServiceEndpoint[] endpoints = context.getEndpointsForService(interfaceName);
        if (endpoints.length > 0) {
            ServiceEndpoint endpoint = endpoints[0];
            System.out.println("Sending to endpoint: " + endpoint);
            
            for (int i = 0; i < messageCount; i++) {
                InOnly exchange = context.getDeliveryChannel().createExchangeFactory().createInOnlyExchange();
                NormalizedMessage message = exchange.createMessage();
                exchange.setEndpoint(endpoint);
                exchange.setInMessage(message);
                // lets set the XML as a byte[], String or DOM etc
                String xml = "<s12:Envelope xmlns:s12='http://www.w3.org/2003/05/soap-envelope'><s12:Body> <foo>Hello!</foo> </s12:Body></s12:Envelope>";
                message.setContent(new StringSource(xml));
                System.out.println("sending message: " + i);
                DeliveryChannel deliveryChannel = context.getDeliveryChannel();
                System.out.println("sync send on deliverychannel: " + deliveryChannel);
                if (sync) {
                	deliveryChannel.sendSync(exchange);
                } else {
                	deliveryChannel.send(exchange);
                }
            }
        }
        else {
            log.warn("No endpoints available for interface: " + interfaceName);
        }
    }


    // Properties
    //-------------------------------------------------------------------------
    public QName getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    public int getNumMessages() {
        return numMessages;
    }

    public void setNumMessages(int numMessages) {
        this.numMessages = numMessages;
    }
}
