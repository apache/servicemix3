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

import java.io.InputStream;
import java.io.Writer;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.codehaus.xfire.exchange.OutMessage;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

/**
 * @version $Revision$
 */
public class XMarshaler {

    private XMLOutputFactory outputFactory;
    private XMLInputFactory inputFactory;
    
    public XMarshaler()
    {
        outputFactory = new WstxOutputFactory();
        inputFactory = new WstxInputFactory();
    }

    public void setContent(NormalizedMessage message, String xml) throws MessagingException {
        message.setContent(new StringSource(xml));
    }

    public XMLStreamReader createStreamReader(NormalizedMessage message) throws XMLStreamException, TransformerException {
        Source content = message.getContent();
        try {
            return getInputFactory().createXMLStreamReader(content);
        } catch (XMLStreamException e) {
            // Such features can be not supported, depending on the source type
            InputStream is = new SourceTransformer().toStreamSource(content).getInputStream();
            return getInputFactory().createXMLStreamReader(is);
        }
    }


    public XMLStreamWriter createStreamWriter(Writer writer) throws XMLStreamException {
        return getOutputFactory().createXMLStreamWriter(writer);
    }

    /**
     * Converts from an NMS message to an ActiveSOAP message exchange
     */
    public void fromNMS(MessageExchange asExchange, NormalizedMessage normalizedMessage) {
    }

    /**
     * Converts from an ActiveSOAP message exchange to an NMS message
     */
    public void toNMS(NormalizedMessage normalizedMessage, OutMessage outMessage) {
    }

    public XMLInputFactory getInputFactory()
    {
        return inputFactory;
    }

    public XMLOutputFactory getOutputFactory()
    {
        return outputFactory;
    }
}
