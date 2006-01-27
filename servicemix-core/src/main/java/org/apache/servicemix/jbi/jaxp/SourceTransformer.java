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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xalan.xsltc.trax.DOM2SAX;
import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

/**
 * A helper class to transform from one type of {@link Source} to another
 *
 * @version $Revision$
 */
public class SourceTransformer {
    public static final String CONTENT_DOCUMENT_PROPERTY = "org.apache.servicemix.content.document";

    private static final transient Log log = LogFactory.getLog(SourceTransformer.class);

    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory transformerFactory;

    public SourceTransformer() {
    }

    public SourceTransformer(DocumentBuilderFactory documentBuilderFactory) {
        this.documentBuilderFactory = documentBuilderFactory;
    }


    /**
     * Converts the given input Source into the required result
     */
    public void toResult(Source source, Result result) throws TransformerException {
        Transformer transformer = createTransfomer();
        if (transformer == null) {
            throw new TransformerException("Could not create a transformer - JAXP is misconfigured!");
        }
        if (source != null) {
            transformer.transform(source, result);
        }
        else {
            log.warn("No Source available");
        }
    }


    /**
     * Converts the given input Source into text
     */
    public String toString(Source source) throws TransformerException {
        if (source == null) {
            return null;
        } else if (source instanceof StringSource) {
            return ((StringSource) source).getText();
        } else if (source instanceof BytesSource) {
            return new String(((BytesSource) source).getData());
        } else {
            StringWriter buffer = new StringWriter();
            toResult(source, new StreamResult(buffer));
            return buffer.toString();
        }
    }

    /**
     * Converts the given input Node into text
     */
    public String toString(Node node) throws TransformerException {
        return toString(new DOMSource(node));
    }

    /**
     * Converts the content of the given message to a String
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public String contentToString(NormalizedMessage message) throws MessagingException, TransformerException, ParserConfigurationException, IOException, SAXException {
        return toString(message.getContent());
    }

    /**
     * Converts the source instance to a {@link DOMSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    public DOMSource toDOMSource(Source source) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        if (source instanceof DOMSource) {
            return (DOMSource) source;
        }
        else if (source instanceof SAXSource) {
            return toDOMSourceFromSAX((SAXSource) source);
        }
        else if (source instanceof StreamSource) {
            return toDOMSourceFromStream((StreamSource) source);
        }
        else {
            return null;
        }
    }

    public Source toDOMSource(NormalizedMessage message) throws MessagingException, TransformerException, ParserConfigurationException, IOException, SAXException {
        Node node = toDOMNode(message);
        return new DOMSource(node);
    }

    /**
     * Converts the source instance to a {@link SAXSource} or returns null if the conversion is not
     * supported (making it easy to derive from this class to add new kinds of conversion).
     */
    public SAXSource toSAXSource(Source source) throws IOException, SAXException {
        if (source instanceof SAXSource) {
            return (SAXSource) source;
        }
        else if (source instanceof DOMSource) {
            return toSAXSourceFromDOM((DOMSource) source);
        }
        else if (source instanceof StreamSource) {
            return toSAXSourceFromStream((StreamSource) source);
        }
        else {
            return null;
        }
    }

    public StreamSource toStreamSource(Source source) throws TransformerException {
        if (source instanceof StreamSource) {
            return (StreamSource) source;
        } else if (source instanceof DOMSource) {
            return toStreamSourceFromDOM((DOMSource) source);
        } else if (source instanceof SAXSource) {
            return toStreamSourceFromSAX((SAXSource) source);
        } else {
            return null;
        }
    }

    public StreamSource toStreamSourceFromSAX(SAXSource source) throws TransformerException {
        InputSource inputSource = source.getInputSource();
        if (inputSource != null) {
            if (inputSource.getByteStream() != null) {
                return new StreamSource(inputSource.getByteStream());
            }
            if (inputSource.getCharacterStream() != null) {
                return new StreamSource(inputSource.getCharacterStream());
            }
        }
        String result = toString(source);
        return new StreamSource(new ByteArrayInputStream(result.getBytes()));
    }

	public StreamSource toStreamSourceFromDOM(DOMSource source) throws TransformerException {
        String result = toString(source);
        return new StreamSource(new ByteArrayInputStream(result.getBytes()));
    }

    public SAXSource toSAXSourceFromStream(StreamSource source) {
        InputSource inputSource = new InputSource(source.getInputStream());
        inputSource.setSystemId(source.getSystemId());
        inputSource.setPublicId(source.getPublicId());
        return new SAXSource(inputSource);
    }

    public Reader toReaderFromSource(Source src) throws TransformerException {
        StreamSource stSrc = toStreamSource(src);
        Reader r = stSrc.getReader();
        if (r == null) {
            r = new InputStreamReader(stSrc.getInputStream());
        }
        return r;
    }

    public DOMSource toDOMSourceFromStream(StreamSource source) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilder builder = createDocumentBuilder();
        String systemId = source.getSystemId();
        Document document = null;
        InputStream inputStream = source.getInputStream();
        if (inputStream != null) {
            document = builder.parse(inputStream, systemId);
        }
        else {
            Reader reader = source.getReader();
            if (reader != null) {
                document = builder.parse(new InputSource(reader));
            }
            else {
                throw new IOException("No input stream or reader available");
            }
        }
        return new DOMSource(document, systemId);
    }

    public SAXSource toSAXSourceFromDOM(DOMSource source) {
        DOM2SAX converter = new DOM2SAX(source.getNode());
        String systemId = converter.getSystemId();
        return new SAXSource(converter, new InputSource(systemId));
    }

    public DOMSource toDOMSourceFromSAX(SAXSource source) throws IOException, SAXException, ParserConfigurationException {
        return new DOMSource(toDOMNodeFromSAX(source));
    }

    public Node toDOMNodeFromSAX(SAXSource source) throws ParserConfigurationException, IOException, SAXException {
        SAX2DOM converter = new SAX2DOM(createDocument());
        XMLReader xmlReader = source.getXMLReader();
        if (xmlReader == null) {
            xmlReader = createXMLReader();
        }
        xmlReader.setContentHandler(converter);
        xmlReader.parse(source.getInputSource());
        return converter.getDOM();
    }

    private XMLReader createXMLReader() throws SAXException {
        // In JDK 1.4, the xml reader factory does not look for META-INF services
        // If the org.xml.sax.driver system property is not defined, and exception will be thrown.
        // In these cases, default to xerces parser
        try {
            return XMLReaderFactory.createXMLReader();
        } catch (Exception e) {
            return XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        }
    }


    /**
     * Converts the given TRaX Source into a W3C DOM node
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public Node toDOMNode(Source source) throws TransformerException, ParserConfigurationException, IOException, SAXException {
        return toDOMSource(source).getNode();
    }

    /**
     * Avoids multple parsing to DOM by caching the DOM representation in the message
     * as a property so future calls will avoid the reparse - and avoid issues with
     * stream based Source instances.
     *
     * @param message the normalized message
     * @return the W3C DOM node for this message
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public Node toDOMNode(NormalizedMessage message) throws MessagingException, TransformerException, ParserConfigurationException, IOException, SAXException {
        Object value = message.getProperty(CONTENT_DOCUMENT_PROPERTY);
        if (value != null) {
            if (value instanceof Node) {
                return (Node) value;
            }
            else {
                throw new MessagingException("Invalid property type: Expected W3C DOM node but found: " + value.getClass().getName() + " with value: " + value);
            }
        }
        else {
            Source content = message.getContent();
            if (content != null) {
                Node node = toDOMNode(content);
                message.setProperty(CONTENT_DOCUMENT_PROPERTY, node);
                return node;
            }
            else {
                return null;
            }
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public DocumentBuilderFactory getDocumentBuilderFactory() {
        if (documentBuilderFactory == null) {
            documentBuilderFactory = createDocumentBuilderFactory();
        }
        return documentBuilderFactory;
    }

    public void setDocumentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
        this.documentBuilderFactory = documentBuilderFactory;
    }


    // Helper methods
    //-------------------------------------------------------------------------
    public DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        return factory;
    }


    public DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = getDocumentBuilderFactory();
        return factory.newDocumentBuilder();
    }

    public Document createDocument() throws ParserConfigurationException {
        DocumentBuilder builder = createDocumentBuilder();
        return builder.newDocument();
    }

    public TransformerFactory getTransformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = createTransformerFactory();
        }
        return transformerFactory;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    public Transformer createTransfomer() throws TransformerConfigurationException {
        TransformerFactory factory = getTransformerFactory();
        return factory.newTransformer();
    }

    public TransformerFactory createTransformerFactory() {
        TransformerFactory answer = TransformerFactory.newInstance();
        return answer;
    }

}
