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
package org.apache.servicemix.http.processors;

import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.servicemix.JbiConstants;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.http.HttpEndpoint;
import org.apache.servicemix.http.HttpLifeCycle;
import org.apache.servicemix.http.HttpProcessor;
import org.apache.servicemix.http.ServerManager;
import org.apache.servicemix.soap.SoapFault;
import org.apache.servicemix.soap.SoapHelper;
import org.apache.servicemix.soap.handlers.AddressingInHandler;
import org.apache.servicemix.soap.marshalers.JBIMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMessage;
import org.mortbay.http.HttpContext;

public class ConsumerProcessor implements ExchangeProcessor, HttpProcessor {

    public static final URI IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/in-only");
    public static final URI IN_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-out");
    public static final URI ROBUST_IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-in-only");
    
    protected HttpEndpoint endpoint;
    protected HttpContext httpContext;
    protected ComponentContext context;
    protected DeliveryChannel channel;
    protected SoapMarshaler soapMarshaler;
    protected JBIMarshaler jbiMarshaler;
    protected SoapHelper soapHelper;
	    
    public ConsumerProcessor(HttpEndpoint endpoint) {
        this.endpoint = endpoint;
        this.soapMarshaler = new SoapMarshaler(endpoint.isSoap());
        this.soapHelper = new SoapHelper(endpoint);
        this.soapHelper.addPolicy(new AddressingInHandler());
        this.jbiMarshaler = new JBIMarshaler();
    }
    
    public void process(MessageExchange exchange) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void start() throws Exception {
        String url = endpoint.getLocationURI();
        httpContext = getServerManager().createContext(url, this);
        httpContext.start();
        context = endpoint.getServiceUnit().getComponent().getComponentContext();
        channel = context.getDeliveryChannel();
    }

    public void stop() throws Exception {
    	httpContext.stop();
        getServerManager().remove(httpContext);
    }
    
    public void process(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	QName envelopeName = null;
    	try {
    		SoapMessage message = soapMarshaler.createReader().read(request.getInputStream(), 
    																request.getHeader("Content-Type"));
    		MessageExchange exchange = soapHelper.createExchange(message);
            NormalizedMessage inMessage = exchange.getMessage("in");
            inMessage.setProperty(JbiConstants.PROTOCOL_HEADERS, getHeaders(request));
            boolean result = channel.sendSync(exchange);
            if (!result) {
                throw new Exception("Error sending exchange: aborted");
            }
            if (exchange.getStatus() == ExchangeStatus.ERROR) {
                exchange.setStatus(ExchangeStatus.DONE);
                channel.send(exchange);
                if (exchange.getError() != null) {
                    throw new Exception(exchange.getError());
                } else if (exchange.getFault() != null) {
                    throw new SoapFault(SoapFault.RECEIVER, null, null, null, exchange.getFault().getContent());
                } else {
                    throw new Exception("Unkown Error");
                }
            } else if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
                NormalizedMessage outMsg = exchange.getMessage("out");
                if (outMsg != null) {
                	SoapMessage out = new SoapMessage();
                	jbiMarshaler.fromNMS(out, outMsg);
                	soapMarshaler.createWriter(out).write(response.getOutputStream());
                }
                exchange.setStatus(ExchangeStatus.DONE);
                channel.send(exchange);
            }
    	} catch (SoapFault fault) {
    		if (SoapFault.SENDER.equals(fault.getCode())) {
    			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    		} else {
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    		}
    		SoapMessage soapFault = new SoapMessage();
    		soapFault.setFault(fault);
    		soapFault.setEnvelopeName(envelopeName);
    		soapMarshaler.createWriter(soapFault).write(response.getOutputStream());
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    protected Map getHeaders(HttpServletRequest request) {
		Map headers = new HashMap();
        Enumeration enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = request.getHeader(name);
            headers.put(name, value);
        }
		return headers;
	}
	
    protected ServerManager getServerManager() {
        HttpLifeCycle lf =  (HttpLifeCycle) endpoint.getServiceUnit().getComponent().getLifeCycle();
        return lf.getServer();
    }

}
