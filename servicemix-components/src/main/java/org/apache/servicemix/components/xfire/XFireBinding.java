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
package org.apache.servicemix.components.xfire;

import java.io.ByteArrayOutputStream;

import javax.jbi.JBIException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.stream.XMLStreamReader;

import org.apache.servicemix.components.util.OutBinding;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.exchange.InMessage;
import org.codehaus.xfire.transport.Channel;
import org.codehaus.xfire.transport.Transport;
import org.codehaus.xfire.transport.local.LocalTransport;

public class XFireBinding extends OutBinding {
    private XMarshaler marshaler;

    private XFire xfire;

    public XFireBinding()
    {
        super();

        this.marshaler = new XMarshaler();
    }
    
    protected void process(MessageExchange messageExchange, NormalizedMessage nm) throws Exception {
        
        XMLStreamReader reader = marshaler.createStreamReader(nm);
        if (reader == null) {
            throw new JBIException("Could not get source as XMLStreamReader.");
        }
        
        InMessage in = new InMessage(reader, "");
        MessageContext context = new MessageContext();
        context.setXFire(xfire);
        context.setService(xfire.getServiceRegistry().getService(getService().getLocalPart()));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        context.setProperty(Channel.BACKCHANNEL_URI, buffer);
        
        Transport transport =  xfire.getTransportManager().getTransport(LocalTransport.BINDING_ID);
        Channel channel = transport.createChannel();
        channel.receive(context, in);

        NormalizedMessage outMessage = messageExchange.createMessage();

        marshaler.setContent(outMessage, buffer.toString());
        marshaler.toNMS(outMessage, context.getOutMessage());

        answer(messageExchange, outMessage);
    }

    public XFire getXfire() {
        return xfire;
    }

    public void setXfire(XFire xfire) {
        this.xfire = xfire;
    }
}
