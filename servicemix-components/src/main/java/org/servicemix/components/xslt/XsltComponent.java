/** 
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **/
package org.servicemix.components.xslt;

import org.servicemix.MessageExchangeListener;
import org.servicemix.components.util.TransformComponentSupport;
import org.servicemix.jbi.jaxp.BytesSource;
import org.servicemix.jbi.jaxp.StringSource;
import org.springframework.core.io.Resource;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Iterator;

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

    public Source getXsltSource() throws IOException {
        if (xsltSource == null) {
            // lets create a new one each time
            // as we can only read a stream once
            return createXsltSource();
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
        catch (TransformerException e) {
            e.printStackTrace();
            throw new MessagingException("Failed to transform: " + e, e);
        }
        catch (IOException e) {
            throw new MessagingException("Failed to load transform: " + e, e);
        }
    }

    protected void transformContent(Transformer transformer, MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws TransformerException, MessagingException {
        if (isUseStringBuffer()) {
            StringWriter buffer = new StringWriter();
            Result result = new StreamResult(buffer);
            transformer.transform(in.getContent(), result);
            out.setContent(new StringSource(buffer.toString()));
        }
        else {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            Result result = new StreamResult(buffer);
            transformer.transform(in.getContent(), result);
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

    protected Source createXsltSource() throws IOException {
        if (xsltResource != null) {
            URL url = xsltResource.getURL();
            if (url == null) {
                return new StreamSource(xsltResource.getInputStream());
            }
            else {
                return new StreamSource(xsltResource.getInputStream(), url.toExternalForm());
            }
        }
        return null;
    }

    public Templates getTemplates() throws IOException, TransformerConfigurationException {
        if (templates == null) {
            templates = createTemplates();
        }
        return templates;
    }

    /**
     * Factory method to create a new transformer instance
     */
    protected Templates createTemplates() throws TransformerConfigurationException, IOException {
        Source source = getXsltSource();
        return getTransformerFactory().newTemplates(source);
    }

    /**
     * Factory method to create a new transformer instance
     */
    protected Transformer createTransformer(MessageExchange exchange, NormalizedMessage in)
            throws TransformerConfigurationException, IOException {
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
        transformer.setParameter("exchange", exchange);
        transformer.setParameter("in", in);
        transformer.setParameter("component", this);
    }

}
