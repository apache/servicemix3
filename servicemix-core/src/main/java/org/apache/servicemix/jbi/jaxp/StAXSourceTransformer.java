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
package org.apache.servicemix.jbi.jaxp;

import org.xml.sax.SAXException;

import javanet.staxutils.StAXSource;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;

import java.io.IOException;

/**
 * An enhanced {@link org.apache.servicemix.jbi.jaxp.SourceTransformer} which adds support for converting from and to
 * {@link StAXSource} instances. Since this class introduces a runtime dependency on StAX
 * which some users may not use/require, this class is separated out from the core JAXP transformer.
 *
 * @version $Revision$
 */
public class StAXSourceTransformer extends SourceTransformer {

    private XMLInputFactory inputFactory;

    /**
     * Converts the source instance to a {@link javax.xml.transform.dom.DOMSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    public StAXSource toStaxSource(Source source) throws XMLStreamException {
        if (source instanceof StAXSource) {
            return (StAXSource) source;
        }
        else {
            XMLInputFactory factory = getInputFactory();
            XMLStreamReader reader = factory.createXMLStreamReader(source);
            return new StAXSource(reader);
        }
    }
    
    public XMLStreamReader toXMLStreamReader(Source source) throws XMLStreamException, TransformerException {
        XMLInputFactory factory = getInputFactory();
        try {
        	return factory.createXMLStreamReader(source);
        } catch (XMLStreamException e) {
        	return factory.createXMLStreamReader(toReaderFromSource(source));
        }
    }

    public DOMSource toDOMSource(Source source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DOMSource answer = super.toDOMSource(source);
        if (answer == null && source instanceof StAXSource) {
            answer = toDOMSourceFromStax((StAXSource) source);
        }
        return answer;
    }

    public SAXSource toSAXSource(Source source) throws IOException, SAXException {
        SAXSource answer = super.toSAXSource(source);
        if (answer == null && source instanceof StAXSource) {
            answer = toSAXSourceFromStax((StAXSource) source);
        }
        return answer;
    }

    public DOMSource toDOMSourceFromStax(StAXSource source) throws TransformerException {
        Transformer transformer = createTransfomer();
        DOMResult result = new DOMResult();
        transformer.transform(source, result);
        return new DOMSource(result.getNode(), result.getSystemId());
    }

    public SAXSource toSAXSourceFromStax(StAXSource source) {
        return (SAXSource) source;
    }

    // Properties
    //-------------------------------------------------------------------------
    public XMLInputFactory getInputFactory() {
        if (inputFactory == null) {
            inputFactory = createInputFactory();
        }
        return inputFactory;
    }

    public void setInputFactory(XMLInputFactory inputFactory) {
        this.inputFactory = inputFactory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected XMLInputFactory createInputFactory() {
        XMLInputFactory answer = XMLInputFactory.newInstance();
        return answer;
    }

}
