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
package org.apache.servicemix.expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenException;
import org.jaxen.NamespaceContext;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import java.io.IOException;


/**
 * Evalutes an XPath expression on the given message using <a href="http://jaxen.org/"/>Jaxen</a>
 *
 * @version $Revision$
 */
public class JaxenXPathExpression implements Expression, InitializingBean {
    private static final transient Log log = LogFactory.getLog(JaxenXPathExpression.class);
    
    private String xpath;
    private SourceTransformer transformer = new SourceTransformer();
    private JaxenVariableContext variableContext = new JaxenVariableContext();
    private XPath xpathObject;
    private NamespaceContext namespaceContext;
    private FunctionContext functionContext;

    public JaxenXPathExpression() {
    }

    /**
     * A helper constructor to make a fully created expression. This constructor will
     * call the {@link #afterPropertiesSet()} method to ensure this POJO is properly constructed.
     */
    public JaxenXPathExpression(String xpath) throws Exception {
        this.xpath = xpath;
        afterPropertiesSet();
    }

    public void afterPropertiesSet() throws Exception {
        if (xpathObject == null) {
            if (xpath == null) {
                throw new IllegalArgumentException("You must specify the xpath property");
            }
            xpathObject = createXPath(xpath);
            xpathObject.setVariableContext(variableContext);
            if (namespaceContext != null) {
                xpathObject.setNamespaceContext(namespaceContext);
            }
            if (functionContext != null) {
                xpathObject.setFunctionContext(functionContext);
            }
        }
    }

    public Object evaluate(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
        try {
            Object object = getXMLNode(exchange, message);
            if (object == null) {
                return null;
            }
            synchronized (this) {
                variableContext.setExchange(exchange);
                variableContext.setMessage(message);
                return evaluateXPath(object);
            }
        }
        catch (TransformerException e) {
            throw new MessagingException(e);
        }
        catch (JaxenException e) {
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

    public boolean matches(MessageExchange exchange, NormalizedMessage message) throws MessagingException {
        try {
            Object object = getXMLNode(exchange, message);
            if (object == null) {
                return false;
            }
            synchronized (this) {
                variableContext.setExchange(exchange);
                variableContext.setMessage(message);
                return evaluateXPathAsBoolean(object);
            }
        }
        catch (TransformerException e) {
            throw new MessagingException(e);
        }
        catch (JaxenException e) {
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
    public XPath getXpathObject() {
        return xpathObject;
    }

    public void setXpathObject(XPath xpathObject) {
        this.xpathObject = xpathObject;
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public SourceTransformer getTransformer() {
        return transformer;
    }

    public void setTransformer(SourceTransformer transformer) {
        this.transformer = transformer;
    }

    public JaxenVariableContext getVariableContext() {
        return variableContext;
    }

    public void setVariableContext(JaxenVariableContext variableContext) {
        this.variableContext = variableContext;
    }

    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    public FunctionContext getFunctionContext() {
        return functionContext;
    }

    public void setFunctionContext(FunctionContext functionContext) {
        this.functionContext = functionContext;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected XPath createXPath(String xpath) throws JaxenException {
        return new DOMXPath(xpath);
    }

    protected Object evaluateXPath(Object object) throws JaxenException {
        return xpathObject.evaluate(object);
    }

    protected boolean evaluateXPathAsBoolean(Object object) throws JaxenException {
        return xpathObject.booleanValueOf(object);
    }


    protected Object getXMLNode(MessageExchange exchange, NormalizedMessage message) throws TransformerException, MessagingException, ParserConfigurationException, IOException, SAXException {
        Node node = null;
        if (message != null) {
            node = transformer.toDOMNode(message);
        }
        else {
            log.warn("Null message for exchange: " + exchange);
        }
        if (node == null) {
            // lets make an empty document to avoid Jaxen throwing a NullPointerException
            node = transformer.createDocument();
        }
        return node;
    }
}
