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

import java.io.Writer;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.codehaus.activesoap.MessageExchange;
import org.codehaus.activesoap.util.XMLStreamFactory;

/**
 * @version $Revision$
 */
public class ASMarshaler {

    private XMLStreamFactory streamFactory = new XMLStreamFactory();

    public void setContent(NormalizedMessage message, String xml) throws MessagingException {
        message.setContent(new StringSource(xml));
    }

    public XMLStreamReader createStreamReader(NormalizedMessage message) throws XMLStreamException {
        Source content = message.getContent();
        return streamFactory.getInputFactory().createXMLStreamReader(content);
    }

    public XMLStreamWriter createStreamWriter(Writer writer) throws XMLStreamException {
        return streamFactory.getOutputFactory().createXMLStreamWriter(writer);
    }

    /**
     * Converts from an NMS message to an ActiveSOAP message exchange
     */
    public void fromNMS(MessageExchange asExchange, NormalizedMessage normalizedMessage) {
    }

    /**
     * Converts from an ActiveSOAP message exchange to an NMS message
     */
    public void toNMS(NormalizedMessage normalizedMessage, MessageExchange asExchange) {
    }

    // Properties
    //-------------------------------------------------------------------------
    public XMLStreamFactory getStreamFactory() {
        return streamFactory;
    }

    public void setStreamFactory(XMLStreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

}
