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
package org.apache.servicemix.components.wsif;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.util.DOMUtil;
import org.apache.servicemix.jbi.util.LazyDOMSource;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * A class which marshalls a WSIF messages into and out of NMS
 *
 * @version $Revision$
 */
public class WSIFMarshaler {

    public static final String WSDL_WRAPPER_NAMESPACE = "http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper";

    private SourceTransformer transformer = new SourceTransformer();

    public void toNMS(final MessageExchange exchange, final NormalizedMessage nmsMessage, final WSIFOperationInfo operationInfo, final WSIFMessage wsifMessage) throws WSIFException, MessagingException {
        addNmsProperties(nmsMessage, wsifMessage);
        for (Iterator iter = wsifMessage.getPartNames(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = wsifMessage.getObjectPart(name);
            nmsMessage.setProperty(name, value);
        }
        nmsMessage.setContent(new LazyDOMSource() {
            protected Node loadNode() {
                return createResultDocument(exchange, nmsMessage, operationInfo, wsifMessage);
            }
        });
    }

    public void fromNMS(WSIFOperationInfo operationInfo, WSIFMessage wsifMessage, NormalizedMessage nmsMessage, Object body) throws WSIFException, MessagingException {
        addWSIFProperties(wsifMessage, nmsMessage);
        try {
            Element element = transformer.toDOMElement(nmsMessage);
            Map parts = wsifMessage.getMessageDefinition().getParts();
            for (Iterator iter = parts.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                Part part = (Part) entry.getValue();

                Object value = getPartValue(name, part, nmsMessage, element);
                wsifMessage.setObjectPart(name, value);
            }
        }
        catch (TransformerException e) {
            throw new MessagingException(e);
        } 
        catch (ParserConfigurationException e) {
            throw new MessagingException(e);
        } 
        catch (IOException e) {
            throw new MessagingException(e);
        } 
        catch (SAXException e) {
            throw new MessagingException(e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
    public SourceTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(SourceTransformer transformer) {
        this.transformer = transformer;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void addNmsProperties(NormalizedMessage nmsMessage, WSIFMessage wsifMessage) throws WSIFException {
        Iterator iter = wsifMessage.getPartNames();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            Object value = wsifMessage.getObjectPart(name);
            nmsMessage.setProperty(name, value);
        }
    }

    protected void addWSIFProperties(WSIFMessage wsifMessage, NormalizedMessage nmsMessage) throws WSIFException {
        for (Iterator iter = nmsMessage.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = nmsMessage.getProperty(name);
            if (shouldIncludeHeader(nmsMessage, name, value)) {
                wsifMessage.setObjectPart(name, value);
            }
        }
    }


    /**
     * Decides whether or not the given header should be included in the WSIF message.
     * By default this includes all values
     */
    protected boolean shouldIncludeHeader(NormalizedMessage normalizedMessage, String name, Object value) {
        return true;
    }

    protected Object getPartValue(String name, Part part, NormalizedMessage nmsMessage, Element body) throws MessagingException, TransformerException, ParserConfigurationException, IOException, SAXException {
        if (isSimpleType(part)) {
            // lets extract the text content   
            return DOMUtil.getElementText(body).trim();
        }
        // TODO we should be extracting each part of the XML using XPath
        return transformer.toDOMNode(nmsMessage);

    }

    /**
     * Returns true if the given part is a string type
     */
    protected boolean isSimpleType(Part part) {
       QName typeName = part.getTypeName();
       if (typeName != null) {
          return "http://www.w3.org/2001/XMLSchema".equals(typeName.getNamespaceURI());
       }
       return false;
    }

    protected Node createResultDocument(MessageExchange exchange, NormalizedMessage normalizedMessage, WSIFOperationInfo operationInfo, WSIFMessage wsifMessage) {
        try {
            Document document = transformer.createDocument();
            Element root = document.createElementNS(WSDL_WRAPPER_NAMESPACE, "jbi:message");
            document.appendChild(root);
            root.setAttribute("xmlns:jbi", WSDL_WRAPPER_NAMESPACE);
            QName operation = exchange.getOperation();
            String operationName = "unknown";
            if (operation != null) {
                operationName = operation.getLocalPart();
                String uri = operation.getNamespaceURI();
                if (uri != null && uri.length() > 0) {
                    root.setAttribute("xmlns:op", uri);
                }
            }
            root.setAttribute("version", "1.0");
            root.setAttribute("type", "op:" + operationName);
            root.setAttribute("name", operationName);


            for (Iterator iter = wsifMessage.getPartNames(); iter.hasNext();) {
                String name = (String) iter.next();
                Object value = normalizedMessage.getProperty(name);

                Element element = document.createElementNS(WSDL_WRAPPER_NAMESPACE, "jbi:part");
                root.appendChild(element);
                addPartValue(element, name, value);
            }

            return document;
        }
        catch (Exception e) {
            throw new FailedToCreateDOMException(e);
        }
    }

    protected void addPartValue(Element element, String name, Object value) {
        if (value instanceof Document) {
            Document doc = (Document) value;
            Element root = doc.getDocumentElement();
            doc.removeChild(root);
            element.appendChild(root);
        }
        else if (value != null) {
            String text = value.toString();
            element.appendChild(element.getOwnerDocument().createTextNode(text));
        }
    }

}
