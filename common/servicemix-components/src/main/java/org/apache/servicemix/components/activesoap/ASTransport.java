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
package org.apache.servicemix.components.activesoap;

import org.codehaus.activesoap.transport.Invocation;
import org.codehaus.activesoap.transport.TransportClient;
import org.codehaus.activesoap.util.XMLStreamFactory;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;

/**
 * An <a href="http://activesoap.codehaus.org/">ActiveSOAP</a> transport which uses JBI.
 *
 * @version $Revision$
 */
public class ASTransport implements TransportClient {

    private ASMarshaler marshaler = new ASMarshaler();

    private DeliveryChannel channel;
    private XMLStreamFactory streamFactory;

    public ASTransport(DeliveryChannel channel) {
        this(channel, new XMLStreamFactory());
    }

    public ASTransport(DeliveryChannel channel, XMLStreamFactory streamFactory) {
        this.channel = channel;
        this.streamFactory = streamFactory;
    }

    public Invocation createInvocation() {
        return new ASInvocation(this, streamFactory);
    }

    public void invokeOneWay(Invocation invocation, Reader request) throws Exception {
        throw new UnsupportedOperationException("Should never be invoked directly");
    }

    public Reader invokeRequest(Invocation invocation, Reader request) throws Exception {
        throw new UnsupportedOperationException("Should never be invoked directly");
    }

    public void close() throws Exception {
        channel.close();
    }

    public void invokeOneWay(ASInvocation invocation, String xml) throws Exception {
        MessageExchangeFactory fac = channel.createExchangeFactory();
        InOnly exchange = fac.createInOnlyExchange();
        NormalizedMessage inMessage = exchange.createMessage();
        marshaler.setContent(inMessage, xml);
        exchange.setInMessage(inMessage);
        channel.send(exchange);
    }

    public XMLStreamReader invokeRequest(ASInvocation invocation, String xml) throws Exception {
        MessageExchangeFactory fac = channel.createExchangeFactory();
        InOut exchange = fac.createInOutExchange();
        NormalizedMessage inMessage = exchange.createMessage();
        marshaler.setContent(inMessage, xml);
        exchange.setInMessage(inMessage);
        boolean answer = channel.sendSync(exchange);
        if (answer) {
            NormalizedMessage outMessage = exchange.getOutMessage();
            return marshaler.createStreamReader(outMessage);
        }
        else {
            Fault fault = exchange.getFault();
            return marshaler.createStreamReader(fault);
        }
    }

}
