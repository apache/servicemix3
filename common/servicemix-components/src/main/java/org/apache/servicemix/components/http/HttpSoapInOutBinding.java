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
package org.apache.servicemix.components.http;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.components.util.ComponentSupport;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.codehaus.xfire.DefaultXFire;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.attachments.Attachment;
import org.codehaus.xfire.attachments.Attachments;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.soap.handler.ReadHeadersHandler;
import org.codehaus.xfire.transport.http.XFireServletController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HttpSoapInOutBinding extends ComponentSupport implements HttpBinding {

    protected XFire xfire;
    protected XFireServletController controller;
    protected Service service;
    protected boolean defaultInOut = true;
    protected String soapAction = "\"\"";
    protected SourceTransformer transformer;

    public HttpSoapInOutBinding() {

    }
    
    public void init(ComponentContext context) throws JBIException {
        super.init(context);
        xfire = new DefaultXFire();
        ObjectServiceFactory factory = new ObjectServiceFactory(xfire.getTransportManager(),
                                                                new AegisBindingProvider());
        factory.setVoidOneWay(true);
        factory.setStyle(SoapConstants.STYLE_DOCUMENT);
        if (isDefaultInOut()) {
            service = factory.create(InOutService.class);
            service.setInvoker(new BeanInvoker(new InOutService(this)));
        } else {
            service = factory.create(InOnlyService.class);
            service.setInvoker(new BeanInvoker(new InOnlyService(this)));
        }
        xfire.getServiceRegistry().register(service);
        controller = new Controller(xfire);
        transformer = new SourceTransformer();
    }
    
    public void process(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        controller.doService(request, response);
    }

    public void invokeInOnly(Source source, MessageContext context) throws XFireFault {
        if (source == null) { 
            throw new XFireFault("Invalid source.", XFireFault.SENDER);
        }
        try {
        	if (soapAction != null) {
        		XFireServletController.getResponse().setHeader("SOAPAction", soapAction);
        	}
            DeliveryChannel channel = getDeliveryChannel();
            MessageExchangeFactory factory = channel.createExchangeFactory();
            InOnly exchange = factory.createInOnlyExchange();
            populateExchange(exchange, source, context);
            boolean result = channel.sendSync(exchange);
            if (!result) {
                throw new XFireFault("Error sending exchange", XFireFault.SENDER);
            }
        } catch (XFireFault e) {
            throw e;
        } catch (Exception e) {
            throw new XFireFault(e);
        }
    }
    
    public Source invokeInOut(Source source, MessageContext context) throws XFireFault {
        if (source == null) { 
            throw new XFireFault("Invalid source.", XFireFault.SENDER);
        }
        try {
        	if (soapAction != null) {
        		XFireServletController.getResponse().setHeader("SOAPAction", soapAction);
        	}
            DeliveryChannel channel = getDeliveryChannel();
            MessageExchangeFactory factory = channel.createExchangeFactory();
            InOut exchange = factory.createInOutExchange();
            populateExchange(exchange, source, context);
            boolean result = channel.sendSync(exchange);
            if (!result) {
                throw new XFireFault("Error sending exchange", XFireFault.SENDER);
            }
            if (exchange.getStatus() == ExchangeStatus.ERROR) {
                Exception e = exchange.getError();
                if (e == null) {
                    throw new XFireFault("Received error", XFireFault.SENDER);
                } else {
                    throw new XFireFault(e, XFireFault.SENDER);
                }
            }
            NormalizedMessage outMessage = exchange.getOutMessage();
            if (outMessage == null) {
                exchange.setError(new Exception("Expected an out message"));
                channel.sendSync(exchange);
                throw new XFireFault("No response", XFireFault.SENDER);
            }                    
            Source src = exchange.getOutMessage().getContent();
            exchange.setStatus(ExchangeStatus.DONE);
            channel.send(exchange);
            src = transformer.toDOMSource(src);
            return src;
        } catch (XFireFault e) {
            throw e;
        } catch (Exception e) {
            throw new XFireFault(e);
		}
    }
    
    protected void populateExchange(MessageExchange exchange, Source src, MessageContext ctx) throws Exception {
        // TODO: Retrieve properties
        NormalizedMessage inMessage = exchange.createMessage();
        // Add removed namespace declarations from the parents
        Map namespaces = (Map) ctx.getProperty(ReadHeadersHandler.DECLARED_NAMESPACES);
        Node node = transformer.toDOMNode(src);
        Element element;
        if (node instanceof Element) {
        	element = (Element) node;
        } else if (node instanceof Document) {
        	element = ((Document) node).getDocumentElement();
        } else {
        	throw new UnsupportedOperationException("Unable to handle nodes of type " + node.getNodeType());
        }
        // Copy embedded namespaces from the envelope into the body root
        for (Iterator it = namespaces.entrySet().iterator(); it.hasNext();) {
        	Entry entry = (Entry) it.next();
            if (element.getAttributes().getNamedItemNS(
            		XMLConstants.XMLNS_ATTRIBUTE_NS_URI, 
            		(String) entry.getKey()) == null) {
            	element.setAttributeNS(
            			XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
            			XMLConstants.XMLNS_ATTRIBUTE + ":" + (String) entry.getKey(),
            			(String) entry.getValue());
            }
        }
        // Set the source
        inMessage.setContent(new DOMSource(element));
        // Retrieve attachments
        Attachments attachments = (Attachments) ctx.getInMessage().getAttachments();
        if (attachments != null) {
            for (Iterator it = attachments.getParts(); it.hasNext();) {
                Attachment part = (Attachment) it.next();
                inMessage.addAttachment(part.getId(), part.getDataHandler());
            }
        }
        exchange.setMessage(inMessage, "in");
    }
    
    public static class InOnlyService {
        private HttpSoapInOutBinding component;
        public InOnlyService() {}
        public InOnlyService(HttpSoapInOutBinding component) {
            this.component = component;
        }
        public void invokeInOnly(Source source, MessageContext context) throws XFireFault {
            this.component.invokeInOnly(source, context);
        }
    }
    
    public static class InOutService {
        private HttpSoapInOutBinding component;
        public InOutService() {}
        public InOutService(HttpSoapInOutBinding component) {
            this.component = component;
        }
        public Source invokeInOut(Source source, MessageContext context) throws XFireFault {
            return this.component.invokeInOut(source, context);
        }
    }

    
    public class Controller extends XFireServletController {
        public Controller(XFire xfire) {
            super(xfire);
        }
        protected String getService(HttpServletRequest request) {
            return service.getSimpleName();
        }        
    }

    // Properties
    //-------------------------------------------------------------------------
    public boolean isDefaultInOut() {
        return defaultInOut;
    }

    /**
     * Sets whether an InOut (the default) or an InOnly message exchange will be used by default.
     */
    public void setDefaultInOut(boolean defaultInOut) {
        this.defaultInOut = defaultInOut;
    }

	public String getSoapAction() {
		return soapAction;
	}

	public void setSoapAction(String soapAction) {
		this.soapAction = soapAction;
	}

}
