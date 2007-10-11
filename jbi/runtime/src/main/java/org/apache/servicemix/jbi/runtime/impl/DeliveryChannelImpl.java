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
package org.apache.servicemix.jbi.runtime.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.api.Channel;
import org.apache.servicemix.api.Exchange;

/**
 * Implementation of the DeliveryChannel.
 *
 */
public class DeliveryChannelImpl implements DeliveryChannel {

    /** Mutable boolean indicating if the channe has been closed */
    private final AtomicBoolean closed;

    /** Holds exchanges to be polled by the component */
    private final BlockingQueue<Exchange> queue;

    /** The underlying Channel */
    private final Channel channel;

    public DeliveryChannelImpl(Channel channel, BlockingQueue<Exchange> queue) {
        this.channel = channel;
        this.queue = queue;
        this.closed = new AtomicBoolean(false);
    }

    public void close() throws MessagingException {
        // TODO process everything
        channel.close();
        closed.set(true);
    }

    public MessageExchangeFactory createExchangeFactory() {
        return new MessageExchangeFactoryImpl(closed);
    }

    public MessageExchangeFactory createExchangeFactory(QName interfaceName) {
        MessageExchangeFactoryImpl factory = new MessageExchangeFactoryImpl(closed);
        factory.setInterfaceName(interfaceName);
        return factory;
    }

    public MessageExchangeFactory createExchangeFactoryForService(QName serviceName) {
        MessageExchangeFactoryImpl factory = new MessageExchangeFactoryImpl(closed);
        factory.setServiceName(serviceName);
        return factory;
    }

    public MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint) {
        MessageExchangeFactoryImpl factory = new MessageExchangeFactoryImpl(closed);
        factory.setEndpoint(endpoint);
        return factory;
    }

    public MessageExchange accept() throws MessagingException {
        try {
            Exchange exchange = queue.take();
            if (exchange == null) {
                return null;
            }
            return new MessageExchangeImpl(exchange);
        } catch (InterruptedException e) {
            throw new MessagingException(e);
        }
    }

    public MessageExchange accept(long timeout) throws MessagingException {
        try {
            Exchange exchange = queue.poll(timeout, TimeUnit.MILLISECONDS);
            if (exchange == null) {
                return null;
            }
            return new MessageExchangeImpl(exchange);
        } catch (InterruptedException e) {
            throw new MessagingException(e);
        }
    }

    public void send(MessageExchange exchange) throws MessagingException {
        assert exchange != null;
        channel.send(((MessageExchangeImpl) exchange).getInternalExchange());
    }

    public boolean sendSync(MessageExchange exchange) throws MessagingException {
        assert exchange != null;
        return channel.sendSync(((MessageExchangeImpl) exchange).getInternalExchange());
    }

    public boolean sendSync(MessageExchange exchange, long timeout) throws MessagingException {
        assert exchange != null;
        return channel.sendSync(((MessageExchangeImpl) exchange).getInternalExchange(), timeout);
    }
}
