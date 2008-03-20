/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.servicemix.ws.notification.invoke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn._2004._06.wsn_ws_basenotification_1_2_draft_01.TopicExpressionType;
import org.servicemix.ws.notification.ActiveMQSubscription;
import org.w3c.dom.Node;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.StringReader;

/**
 * A dispatcher of WS-N notifications which takes a message from JMS and
 * dispatches it into a JBI container.
 * 
 * @version $Revision$
 */
public class JBIInvoker extends InvokerSupport {
    public static final String TOPIC_NAME = "topic";

    private static final transient Log log = LogFactory.getLog(JBIInvoker.class);

    private DeliveryChannel deliveryChannel;
    private MessageExchangeFactory exchangeFactory;

    public JBIInvoker(DeliveryChannel deliveryChannel, ServiceEndpoint endpoint, ActiveMQSubscription subscription) {
        this.deliveryChannel = deliveryChannel;
        this.exchangeFactory = deliveryChannel.createExchangeFactory(endpoint);
    }

    protected void dispatchMessage(TopicExpressionType topic, Message message) throws MessagingException, JMSException {
        InOnly exchange = exchangeFactory.createInOnlyExchange();
        NormalizedMessage in = exchange.createMessage();
        in.setProperty(TOPIC_NAME, topic);
        in.setContent(createContent(topic, message));
        exchange.setInMessage(in);
        appendHeaders(exchange, topic, message);
        deliveryChannel.send(exchange);
    }

    /**
     * Provides a general hook to allow custom headers to be appended to the
     * exchange
     */
    protected void appendHeaders(InOnly exchange, TopicExpressionType topic, Message message) {
    }

    /**
     * Factory method to create the content of the JBI message using the inbound
     * JMS message
     */
    protected Source createContent(TopicExpressionType topic, Message message) throws JMSException {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            return new StreamSource(new StringReader(textMessage.getText()));
        }
        else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte data[] = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(data);
            return new StreamSource(new ByteArrayInputStream(data));
        }
        else if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            return createSource(topic, objectMessage);
        }
        else {
            log.warn("No message body, so cannot create a JAXP Source to dispatch into JBI");
        }
        return null;
    }

    protected Source createSource(TopicExpressionType topic, ObjectMessage message) throws JMSException {
        Serializable object = message.getObject();
        if (object instanceof Node) {
            return new DOMSource((Node) object);
        }
        else {
            log.warn("Unknown type, cannot convert to JAXP Source for object: " + object);
        }
        return null;
    }
}
