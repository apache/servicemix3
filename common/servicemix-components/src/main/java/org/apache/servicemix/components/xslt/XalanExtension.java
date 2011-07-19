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

import org.apache.commons.lang.math.NumberUtils;
import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.components.util.CopyTransformer;
import org.apache.servicemix.components.util.MarshalerSupport;
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.lib.ExsltDynamic;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.DOMBuilder;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXNotSupportedException;

import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

/**
 * An extension to allow <a href="http://xml.apache.org/xalan/">Xalan</a> to perform XPath based routing using
 * ServiceMix.
 *
 * @version $Revision$
 */
public class XalanExtension extends MarshalerSupport {

    /**
     * Forwards the inbound message to the destination copying the content and properties
     */
    public void forward(XSLProcessorContext context, Element element) throws MessagingException {
        ComponentSupport component = getComponent(context, element);
        if (component == null) {
            throw new MessagingException("Could not find a component on which to perform the service invocation!");
        }

        TransformerImpl transformer = context.getTransformer();
        PrefixResolver namespaceContext = transformer.getXPathContext().getNamespaceContext();

        QName service = getQNameAttribute(namespaceContext, element, "service");
        QName interfaceName = getQNameAttribute(namespaceContext, element, "interface");
        QName operation = getQNameAttribute(namespaceContext, element, "operation");

        MessageExchange exchange = getExchange(context, element);
        NormalizedMessage in = getInMessage(context, element);

        // TODO we should allow nested setOutProperty tags

        component.invoke(exchange, in, service, interfaceName, operation);
    }

    /**
     * Invokes the service with the XML content included in the XML element
     */
    public void invoke(XSLProcessorContext context, ElemTemplateElement element) throws MessagingException, ParserConfigurationException, TransformerException {
        ComponentSupport component = getComponent(context, element);
        if (component == null) {
            throw new MessagingException("Could not find a component on which to perform the service invocation!");
        }

        TransformerImpl transformer = context.getTransformer();
        PrefixResolver namespaceContext = transformer.getXPathContext().getNamespaceContext();

        QName service = getQNameAttribute(namespaceContext, element, "service");
        QName interfaceName = getQNameAttribute(namespaceContext, element, "interface");
        QName operation = getQNameAttribute(namespaceContext, element, "operation");

        InOnly outExchange = component.createInOnlyExchange(service, interfaceName, operation);
        NormalizedMessage out = outExchange.createMessage();
        outExchange.setInMessage(out);

        transformer.setParameter("out", out);

        // lets copy the content into the body
        Document document = getTransformer().createDocument();
        DOMBuilder builder = new DOMBuilder(document);
        transformer.executeChildTemplates(element, context.getContextNode(), context.getMode(), builder);
        
        out.setContent(new DOMSource(document));

        // now lets perform the invocation
        component.send(outExchange);
    }

    /**
     * Calls the service with the XML content included in the XML element and outputs the XML content
     */
    public void call(XSLProcessorContext context, ElemTemplateElement element) throws MessagingException, ParserConfigurationException, TransformerException {
        ComponentSupport component = getComponent(context, element);
        if (component == null) {
            throw new MessagingException("Could not find a component on which to perform the service invocation!");
        }

        TransformerImpl transformer = context.getTransformer();
        PrefixResolver namespaceContext = transformer.getXPathContext().getNamespaceContext();

        QName service = getQNameAttribute(namespaceContext, element, "service");
        QName interfaceName = getQNameAttribute(namespaceContext, element, "interface");
        QName operation = getQNameAttribute(namespaceContext, element, "operation");

        InOut outExchange = component.createInOutExchange(service, interfaceName, operation);
        NormalizedMessage out = outExchange.createMessage();
        outExchange.setInMessage(out);


        // lets copy the content into the body
        Document document = getTransformer().createDocument();
        DOMBuilder builder = new DOMBuilder(document);
        transformer.executeChildTemplates(element, context.getContextNode(), context.getMode(), builder);

        out.setContent(new DOMSource(document));

        // now lets perform the invocation
        if (component.getDeliveryChannel().sendSync(outExchange)) {
            NormalizedMessage result = outExchange.getOutMessage();
            String outVarName = getAttribute(element, "outVar", "out");
            transformer.setParameter(outVarName, result);
        }
        else {
            Exception error = outExchange.getError();
            if (error != null) {
                transformer.setParameter("error", error);
            }
            Fault fault = outExchange.getFault();
            if (fault != null) {
                transformer.setParameter("fault", fault);
            }
        }

    }

    /**
     * Copies the properties from the input to the output message
     */
    public void copyProperties(XSLProcessorContext context, Element element) throws MessagingException {
        NormalizedMessage in = getInMessage(context, element);
        NormalizedMessage out = getOutMessage(context, element);
        CopyTransformer.copyProperties(in, out);
    }

    /**
     * Sets a named property on the output message
     */
    public void setOutProperty(XSLProcessorContext context, ElemTemplateElement element) throws MessagingException, SAXNotSupportedException {
        NormalizedMessage out = getOutMessage(context, element);
        String name = element.getAttribute("name");
        if (name == null) {
            throw new IllegalArgumentException("Must specify a 'name' attribute to set a property on the output message");
        }
        String xpath = element.getAttribute("select");
        if (xpath == null) {
            throw new IllegalArgumentException("Must specify a 'select' attribute to set a property on the output message");
        }
        XObject answer = ExsltDynamic.evaluate(context.getTransformer().getXPathContext().getExpressionContext(), xpath);
        Object value;
        try {
            if (answer.getType() == XObject.CLASS_NUMBER) {
                value = NumberUtils.createNumber(answer.str());
            } else if (answer.getType() == XObject.CLASS_BOOLEAN) {
                value = new Boolean(answer.bool());
            } else {
                // XObject guarantees we are never null.
                value = answer.str();
            }
        } catch (TransformerException e) {
            value = answer.str();
        } catch (NumberFormatException e) {
            value = answer.str();
        }
        out.setProperty(name, value);
    }

    // Extension XPath functions
    //-------------------------------------------------------------------------

    // Implementation methods
    //-------------------------------------------------------------------------
    protected ComponentSupport getComponent(XSLProcessorContext context, Element element) {
        return (ComponentSupport) getParameter(context, "component");
    }

    protected MessageExchange getExchange(XSLProcessorContext context, Element element) {
        return (MessageExchange) getParameter(context, "exchange");
    }

    protected NormalizedMessage getInMessage(XSLProcessorContext context, Element element) {
        return (NormalizedMessage) getParameter(context, "in");
    }

    protected NormalizedMessage getOutMessage(XSLProcessorContext context, Element element) {
        return (NormalizedMessage) getParameter(context, "out");
    }

    protected Object getParameter(XSLProcessorContext context, String name) {
        return context.getTransformer().getParameter(name);
    }

    protected QName getQNameAttribute(PrefixResolver namespaceContext, Element element, String name) {
        String qualifiedName = element.getAttribute(name);
        if (qualifiedName != null) {
            int index = qualifiedName.indexOf(':');
            if (index >= 0) {
                String prefix = qualifiedName.substring(0, index);
                String localName = qualifiedName.substring(index + 1);
                String uri = namespaceContext.getNamespaceForPrefix(prefix);
                return new QName(uri, localName, prefix);
            }
            else {
                String uri = namespaceContext.getNamespaceForPrefix("");
                if (uri != null) {
                    return new QName(uri, qualifiedName);
                }
                return new QName(qualifiedName);
            }
        }
        return null;
    }

    protected String getAttribute(Element element, String attribute, String defaultValue) {
        String answer = element.getAttribute(attribute);
        if (answer == null || answer.length() == 0) {
            answer = defaultValue;
        }
        return answer;
    }

}
