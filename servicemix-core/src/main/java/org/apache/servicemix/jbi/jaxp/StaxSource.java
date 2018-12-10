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

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class StaxSource extends SAXSource implements XMLReader {

    private XMLStreamReader streamReader;

    private ContentHandler contentHandler;

    private char[] chars = new char[1024];

    public StaxSource(XMLStreamReader streamReader) {
        this.streamReader = streamReader;
        setInputSource(new InputSource());
    }

    public XMLReader getXMLReader() {
        return this;
    }

    protected void parse() throws SAXException {
        try {
            contentHandler.startDocument();
            while (true) {
                switch (streamReader.getEventType()) {
                case XMLStreamConstants.ATTRIBUTE:
                case XMLStreamConstants.CDATA:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (!streamReader.isWhiteSpace()) {
                        for (int textLength = streamReader.getTextLength(); textLength > 0; textLength -= chars.length) {
                            int l = Math.min(textLength, chars.length);
                            streamReader.getTextCharacters(0, chars, 0, l);
                            contentHandler.characters(chars, 0, l);
                        }
                    }
                    break;
                case XMLStreamConstants.COMMENT:
                case XMLStreamConstants.DTD:
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    contentHandler.endDocument();
                    return;
                case XMLStreamConstants.END_ELEMENT: {
                    String uri = streamReader.getNamespaceURI();
                    String localName = streamReader.getLocalName();
                    String prefix = streamReader.getPrefix();
                    String qname = prefix != null && prefix.length() > 0 ? prefix + ":" + localName : localName;
                    contentHandler.endElement(uri, localName, qname);
                    for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
                        //contentHandler.endPrefixMapping(streamReader.getNamespaceURI(i));
                    }
                    break;
                }
                case XMLStreamConstants.ENTITY_DECLARATION:
                case XMLStreamConstants.ENTITY_REFERENCE:
                case XMLStreamConstants.NAMESPACE:
                case XMLStreamConstants.NOTATION_DECLARATION:
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                case XMLStreamConstants.SPACE:
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.START_ELEMENT: {
                    for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
                        //contentHandler.startPrefixMapping(streamReader.getNamespacePrefix(i),
                        //                                  streamReader.getNamespaceURI(i));
                    }
                    String uri = streamReader.getNamespaceURI();
                    String localName = streamReader.getLocalName();
                    String prefix = streamReader.getPrefix();
                    String qname = prefix != null && prefix.length() > 0 ? prefix + ":" + localName : localName;
                    contentHandler.startElement(uri, localName, qname, getAttributes());
                    break;
                }
                }
                streamReader.next();
            }
        } catch (XMLStreamException e) {
            if (e.getLocation() != null) {
                throw new SAXParseException(e.getMessage(), null, null, e.getLocation().getLineNumber(), e.getLocation()
                        .getColumnNumber(), e);
            } else {
                throw new SAXParseException(e.getMessage(), null, null, -1, -1, e);
            }
        }
    }

    protected String getQualifiedName() {
        String prefix = streamReader.getPrefix();
        if (prefix != null && prefix.length() > 0) {
            return prefix + ":" + streamReader.getLocalName();
        } else {
            return streamReader.getLocalName();
        }
    }

    protected Attributes getAttributes() {
        AttributesImpl attrs = new AttributesImpl();
        // Adding namespace declaration as attributes is necessary because
        // the xalan implementation that ships with SUN JDK 1.4 is bugged
        // and does not handle the startPrefixMapping method
        for (int i = 0; i < streamReader.getNamespaceCount(); i++) {
            String prefix = streamReader.getNamespacePrefix(i);
            String uri = streamReader.getNamespaceURI(i);
            if (uri == null) {
                uri = "";
            }
            // Default namespace
            if (prefix == null || prefix.length() == 0) {
                attrs.addAttribute(null, 
                                   null, 
                                   XMLConstants.XMLNS_ATTRIBUTE, 
                                   "CDATA", 
                                   uri);
            } else {
                attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
                                   prefix, 
                                   XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix, 
                                   "CDATA", 
                                   uri);
            }
        }
        for (int i = 0; i < streamReader.getAttributeCount(); i++) {
            String uri = streamReader.getAttributeNamespace(i);
            String localName = streamReader.getAttributeLocalName(i);
            String prefix = streamReader.getAttributePrefix(i);
            String qName;
            if (prefix != null && prefix.length() > 0) {
                qName = prefix + ':' + localName;
            } else {
                qName = localName;
            }
            String type = streamReader.getAttributeType(i);
            String value = streamReader.getAttributeValue(i);
            if (value == null) {
                value = "";
            }
            attrs.addAttribute(uri, localName, qName, type, value);
        }
        return attrs;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void parse(InputSource input) throws SAXException {
        StaxSource.this.parse();
    }

    public void parse(String systemId) throws SAXException {
        StaxSource.this.parse();
    }

}
