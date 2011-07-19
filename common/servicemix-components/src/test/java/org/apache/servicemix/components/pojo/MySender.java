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
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.management.ObjectName;

import org.apache.servicemix.jbi.jaxp.StringSource;

/**
 * @version $Revision$
 */
// START SNIPPET: send
public class MySender implements ComponentLifeCycle {

    private ComponentContext context;
    private ObjectName extensionMBeanName;

    /**
     * Sends a number of messages
     */
    public void sendMessages(int count) throws MessagingException {
        DeliveryChannel deliveryChannel = context.getDeliveryChannel();
        MessageExchangeFactory factory = deliveryChannel.createExchangeFactory();

        for (int i = 0; i < count; i++) {
            InOnly exchange = factory.createInOnlyExchange();
            NormalizedMessage message = exchange.createMessage();
            exchange.setInMessage(message);

            message.setProperty("id", new Integer(i));
            message.setContent(new StringSource("<example id='" + i + "'/>"));

            deliveryChannel.send(exchange);
        }
    }

    // ComponentLifeCycle interface
    //-------------------------------------------------------------------------
    public ObjectName getExtensionMBeanName() {
        return extensionMBeanName;
    }

    public void init(ComponentContext context) throws JBIException {
        this.context = context;
    }

    public void shutDown() throws JBIException {
    }

    public void start() throws JBIException {
    }

    public void stop() throws JBIException {
    }

    // Properties
    //-------------------------------------------------------------------------
    public void setExtensionMBeanName(ObjectName extensionMBeanName) {
        this.extensionMBeanName = extensionMBeanName;
    }

}
// END SNIPPET: send
