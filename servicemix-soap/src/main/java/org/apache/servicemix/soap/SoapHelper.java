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
package org.apache.servicemix.soap;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.servicemix.soap.marshalers.JBIMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMessage;
import org.w3c.dom.Document;

/**
 * Helper class for working with soap endpoints
 *
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 * @since 3.0
 */
public class SoapHelper {

    public static final URI IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/in-only");
    public static final URI IN_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-out");
    public static final URI ROBUST_IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-in-only");
    
	private SoapEndpoint endpoint;
    private List policies;
    private JBIMarshaler jbiMarshaler;
	
	public SoapHelper() {
		this.policies = new ArrayList();
        this.jbiMarshaler = new JBIMarshaler();
	}
	
	public SoapHelper(SoapEndpoint endpoint) {
		this();
		this.endpoint = endpoint;
	}
	
    public void addPolicy(Handler policy) {
    	policies.add(policy);
    }

	public Context createContext(SoapMessage message) {
		Context context = new Context();
		context.setProperty(Context.SOAP_MESSAGE, message);
		context.setProperty(Context.OPERATION, message.getBodyName());
		context.setProperty(Context.INTERFACE, endpoint.getInterfaceName());
		context.setProperty(Context.SERVICE, endpoint.getService());
		context.setProperty(Context.ENDPOINT, endpoint.getEndpoint());
		return context;
	}
	
	public MessageExchange createExchange(SoapMessage message) throws Exception {
		Context context = createContext(message);
		analyzeHeaders(context);
		URI mep = findMep(context);
		if (mep == null) {
			mep = endpoint.getDefaultMep();
		}
        MessageExchange exchange = createExchange(mep);
        exchange.setService((QName) context.getProperty(Context.SERVICE));
        exchange.setInterfaceName((QName) context.getProperty(Context.INTERFACE));
        exchange.setOperation((QName) context.getProperty(Context.OPERATION));
        if (context.getProperty(Context.ENDPOINT) != null) {
    		ComponentContext componentContext = endpoint.getServiceUnit().getComponent().getComponentContext();
    		QName serviceName = (QName) context.getProperty(Context.SERVICE);
    		String endpointName = (String) context.getProperty(Context.ENDPOINT);
    		ServiceEndpoint se = componentContext.getEndpoint(serviceName, endpointName);
    		if (se != null) {
    			exchange.setEndpoint(se);
    		}
        }
        NormalizedMessage inMessage = exchange.createMessage();
        jbiMarshaler.toNMS(inMessage, message);
        exchange.setMessage(inMessage, "in");
        return exchange;
	}
	
    public void analyzeHeaders(Context context) throws Exception {
    	for (Iterator it = policies.iterator(); it.hasNext();) {
    		Handler policy = (Handler) it.next();
    		policy.process(context);
    	}
    }
	
    public MessageExchange createExchange(URI mep) throws MessagingException {
        ComponentContext context = endpoint.getServiceUnit().getComponent().getComponentContext();
        DeliveryChannel channel = context.getDeliveryChannel();
        MessageExchangeFactory factory = channel.createExchangeFactory();
        MessageExchange exchange = factory.createExchange(mep);
        return exchange;
    }
    
    public URI findMep(Context context) throws Exception {
		QName interfaceName = (QName) context.getProperty(Context.INTERFACE);
		QName serviceName = (QName) context.getProperty(Context.SERVICE);
		QName operationName = (QName) context.getProperty(Context.OPERATION);
		String endpointName = (String) context.getProperty(Context.ENDPOINT);
		ComponentContext componentContext = endpoint.getServiceUnit().getComponent().getComponentContext();
		// Find target endpoint
		ServiceEndpoint se = null;
		if (serviceName != null && endpointName != null) {
			se = componentContext.getEndpoint(serviceName, endpointName);
		}
		if (se == null && interfaceName != null) {
			ServiceEndpoint[] ses = componentContext.getEndpoints(interfaceName);
			if (ses != null && ses.length > 0) {
				se = ses[0];
			}
		}
		// Find WSDL description
        Definition definition = null;
		if (se == null) {
			// Get this endpoint definition
			definition = endpoint.getDefinition();
			if (definition == null && endpoint.getDescription() != null) {
				// Eventually parse the definition
	            WSDLFactory factory = WSDLFactory.newInstance();
	            WSDLReader reader = factory.newWSDLReader();
	            definition = reader.readWSDL(null, endpoint.getDescription());
            	endpoint.setDefinition(definition);
			}
		} else {
			// Find endpoint description from the component context
			Document description = componentContext.getEndpointDescriptor(se);
			if (description != null) {
				// Parse WSDL
				WSDLFactory factory = WSDLFactory.newInstance();
				WSDLReader reader = factory.newWSDLReader();
				definition = reader.readWSDL(null, description);
			}
		}
		
		// Find operation within description
        URI mep = null;
        if (interfaceName != null && operationName != null && definition != null) {
        	PortType portType = definition.getPortType(interfaceName);
        	if (portType != null) {
	        	Operation oper = portType.getOperation(operationName.getLocalPart(), null, null);
	        	if (oper != null) {
	        		boolean output = oper.getOutput() != null && 
	        		                 oper.getOutput().getMessage() != null &&
	        		                 oper.getOutput().getMessage().getParts().size() > 0;
	        		boolean faults = oper.getFaults().size() > 0;
	        		if (output) {
	        			mep = IN_OUT;
	        		} else if (faults) {
	        			mep = ROBUST_IN_ONLY;
	        		} else {
	        			mep = IN_ONLY;
	        		}
	        	}
        	}
        }
        return mep;
    }
    
	
}
