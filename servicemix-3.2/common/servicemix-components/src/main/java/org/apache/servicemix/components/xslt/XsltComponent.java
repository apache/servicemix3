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
package org.apache.servicemix.components.xslt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.servicemix.MessageExchangeListener;
import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.servicemix.jbi.jaxp.BytesSource;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * An <a href="http://www.w3.org/TR/xslt">XSLT</a> based JBI component using <a
 * href="http://java.sun.com/xml/jaxp/">JAXP</a> to perform the XSLT
 * transformation.
 * 
 * @version $Revision$
 */
public class XsltComponent extends TransformComponentSupport implements MessageExchangeListener {

    private TransformerFactory transformerFactory;
    private Source xsltSource;
    private Resource xsltResource;
    private Templates templates;
    private boolean disableOutput;
    private boolean useStringBuffer = true;
    private boolean forceDocIfDom = true;
    private Map xsltParameters;

    /**
     * @return the forceDocIfDom
     */
    public boolean isForceDocIfDom() {
        return forceDocIfDom;
    }

    /**
     * @param forceDocIfDom the forceDocIfDom to set
     */
    public void setForceDocIfDom(boolean forceDocIfDom) {
        this.forceDocIfDom = forceDocIfDom;
    }

    // Properties
    // -------------------------------------------------------------------------
    public TransformerFactory getTransformerFactory() {
        if (transformerFactory == null) {
            transformerFactory = TransformerFactory.newInstance();
        }
        return transformerFactory;
    }

    public void setTransformerFactory(TransformerFactory transformerFactory) {
        this.transformerFactory = transformerFactory;
    }

    public Source getXsltSource() throws Exception {
        if (xsltSource == null) {
            // lets create a new one each time
            // as we can only read a stream once
            xsltSource = createXsltSource();
        }
        return xsltSource;
    }

    public void setXsltSource(Source xsltSource) {
        this.xsltSource = xsltSource;
    }

    public Resource getXsltResource() {
        return xsltResource;
    }

    public void setXsltResource(Resource xsltResource) {
        this.xsltResource = xsltResource;
    }

    public boolean isDisableOutput() {
        return disableOutput;
    }

    public void setDisableOutput(boolean disableOutput) {
        this.disableOutput = disableOutput;
    }

    public boolean isUseStringBuffer() {
        return useStringBuffer;
    }

    public void setUseStringBuffer(boolean useStringBuffer) {
        this.useStringBuffer = useStringBuffer;
    }

    /**
     * @return the xsltParameters
     */
    public Map getXsltParameters() {
        return xsltParameters;
    }

    /**
     * @param xsltParameters the xsltParameters to set
     */
    public void setXsltParameters(Map xsltParameters) {
        this.xsltParameters = xsltParameters;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out)
            throws MessagingException {
        try {
            Transformer transformer = createTransformer(exchange, in);
            configureTransformer(transformer, exchange, in);
            copyPropertiesAndAttachments(exchange, in, out);
            transformContent(transformer, exchange, in, out);
            return shouldOutputResult(transformer);
        }
        catch (Exception e) {
            throw new MessagingException("Failed to transform: " + e, e);
        }
    }

    protected void transformContent(Transformer transformer, MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws TransformerException, MessagingException, ParserConfigurationException {
        Source src = in.getContent();
        if (forceDocIfDom && src instanceof DOMSource) {
            Node n = ((DOMSource) src).getNode();
            if (n instanceof Document == false) {
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.appendChild(doc.importNode(n, true));
                src = new DOMSource(doc);
            }
        }
        if (isUseStringBuffer()) {
            StringWriter buffer = new StringWriter();
            Result result = new StreamResult(buffer);
            transformer.transform(src, result);
            out.setContent(new StringSource(buffer.toString()));
        }
        else {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Result result = new StreamResult(buffer);
            transformer.transform(src, result);
            out.setContent(new BytesSource(buffer.toByteArray()));
        }
    }

    /**
     * Should we disable output of the result of the XSLT?
     */
    protected boolean shouldOutputResult(Transformer transformer) {
        if (disableOutput) {
            return false;
        }
        return true;
        /**
         * String value = transformer.getOutputProperty("disableOutput"); return
         * value == null || !value.equals("true");
         */
    }

    protected Source createXsltSource() throws Exception {
        if (xsltResource != null) {
            return new DOMSource(parse(xsltResource));
        }
        return null;
    }

    protected Document parse(Resource res) throws Exception {
        URL url = null;
        try {
            res.getURL();
        } catch (IOException e) {
            // Ignore
        }
        DocumentBuilder builder = new SourceTransformer().createDocumentBuilder();
        return builder.parse(res.getInputStream(), url != null ? url.toExternalForm() : null);
    }

    
    public Templates getTemplates() throws Exception {
        if (templates == null) {
            templates = createTemplates();
        }
        return templates;
    }

    /**
     * Factory method to create a new transformer instance
     */
    protected Templates createTemplates() throws Exception {
        Source source = getXsltSource();
        return getTransformerFactory().newTemplates(source);
    }

    /**
     * Factory method to create a new transformer instance
     */
    protected Transformer createTransformer(MessageExchange exchange, NormalizedMessage in)
            throws Exception {
        Source source = getXsltSource();
        if (source == null) {
            return getTransformerFactory().newTransformer();
        }
        else {
            return getTemplates().newTransformer();
        }
    }

    /**
     * A hook to allow the transformer to be configured from the current
     * exchange and inbound message
     */
    protected void configureTransformer(Transformer transformer, MessageExchange exchange, NormalizedMessage in) {
        for (Iterator iter = exchange.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = exchange.getProperty(name);
            transformer.setParameter(name, value);
        }
        for (Iterator iter = in.getPropertyNames().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            Object value = in.getProperty(name);
            transformer.setParameter(name, value);
        }
        if (xsltParameters != null) {
            for (Iterator iter = xsltParameters.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                Object value = xsltParameters.get(name);
                transformer.setParameter(name, value);
            }
        }
        transformer.setParameter("exchange", exchange);
        transformer.setParameter("in", in);
        transformer.setParameter("component", this);
    }

}
