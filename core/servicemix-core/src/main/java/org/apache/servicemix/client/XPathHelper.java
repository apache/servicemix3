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
package org.apache.servicemix.client;

import java.util.Map;

import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

/**
 * A helper class for working with XPath and {@link Message} instances.
 * 
 * @version $Revision: $
 */
public class XPathHelper {

    private Source content;
    private XPathFactory xPathFactory;
    private XPath xPath;

    public XPathHelper() {
    }

    public XPathHelper(NormalizedMessage message) {
        this.content = message.getContent();
    }

    public XPathHelper(Source content) {
        this.content = content;
    }

    public XPathHelper(NormalizedMessage message, Map namespaces) {
        this(message);
        setNamespaces(namespaces);
    }

    public XPathHelper(NormalizedMessage message, NamespaceContext namespaces) {
        this(message);
        setNamespaceContext(namespaces);
    }

    public Object evaluate(String expression, QName arg2) throws XPathExpressionException {
        return getXPath().evaluate(expression, getItem(), arg2);
    }

    public String evaluate(String expression) throws XPathExpressionException {
        return getXPath().evaluate(expression, getItem());
    }

    public void reset() {
        if (xPath != null) {
            getXPath().reset();
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public void setMessage(NormalizedMessage message) {
        setContent(message.getContent());
    }

    public void setContent(Source content) {
        this.content = content;
    }

    public final NamespaceContext getNamespaceContext() {
        return getXPath().getNamespaceContext();
    }

    public XPathFunctionResolver getXPathFunctionResolver() {
        return getXPath().getXPathFunctionResolver();
    }

    public XPathVariableResolver getXPathVariableResolver() {
        return getXPath().getXPathVariableResolver();
    }

    public final void setNamespaceContext(NamespaceContext context) {
        getXPath().setNamespaceContext(context);
    }

    public void setXPathFunctionResolver(XPathFunctionResolver resolver) {
        getXPath().setXPathFunctionResolver(resolver);
    }

    public void setXPathVariableResolver(XPathVariableResolver resolver) {
        getXPath().setXPathVariableResolver(resolver);
    }

    public XPathFactory getXPathFactory() {
        if (xPathFactory == null) {
            xPathFactory = XPathFactory.newInstance();
        }
        return xPathFactory;
    }

    public void setXPathFactory(XPathFactory factory) {
        this.xPathFactory = factory;
    }

    public Source getContent() {
        return content;
    }

    public final XPath getXPath() {
        if (xPath == null) {
            xPath = getXPathFactory().newXPath();
        }
        return xPath;
    }

    /**
     * Sets the namespace context to the given map where the keys are namespace
     * prefixes and the values are the URIs
     */
    public final void setNamespaces(Map namespaces) {
        setNamespaceContext(new DefaultNamespaceContext(getNamespaceContext(), namespaces));
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected Object getItem() {
        return content;
    }
}
