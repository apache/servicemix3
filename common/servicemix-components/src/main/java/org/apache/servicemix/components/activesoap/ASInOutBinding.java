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

import java.io.StringWriter;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.servicemix.components.util.OutBinding;
import org.codehaus.activesoap.RestService;

/**
 * Converts an inbound JBI message into an <a href="http://activesoap.codehaus.org/">ActiveSOAP</a>
 * request-response and outputs the response back into JBI
 *
 * @version $Revision$
 */
public class ASInOutBinding extends OutBinding {

    private RestService service;
    private ASMarshaler marshaler = new ASMarshaler();

    public ASInOutBinding(RestService service) {
        this.service = service;
    }

    /**
     * @deprecated use getMarshaler instead
     */
    public ASMarshaler getMarshaller() {
        return marshaler;
    }

    /**
     * @deprecated use setMarshaler instead
     */
    public void setMarshaller(ASMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    /**
     * @return the marshaler
     */
    public ASMarshaler getMarshaler() {
        return marshaler;
    }

    /**
     * @param marshaler the marshaler to set
     */
    public void setMarshaler(ASMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void process(MessageExchange messageExchange, NormalizedMessage inMessage) throws Exception {
        XMLStreamReader in = marshaler.createStreamReader(inMessage);

        StringWriter buffer = new StringWriter();
        XMLStreamWriter out = marshaler.createStreamWriter(buffer);

        org.codehaus.activesoap.MessageExchange asExchange = service.createMessageExchange(in, out);
        marshaler.fromNMS(asExchange, inMessage);

        service.invoke(asExchange);

        NormalizedMessage outMessage = messageExchange.createMessage();

        marshaler.setContent(outMessage, buffer.toString());
        marshaler.toNMS(outMessage, asExchange);

        answer(messageExchange, outMessage);
    }

}
