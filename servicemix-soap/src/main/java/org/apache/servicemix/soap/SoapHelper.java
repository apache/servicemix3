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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.soap.marshalers.JBIMarshaler;
import org.apache.servicemix.soap.marshalers.SoapMarshaler;
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

    private static final Log logger = LogFactory.getLog(SoapHelper.class);

    public static final URI IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/in-only");
    public static final URI IN_OUT = URI.create("http://www.w3.org/2004/08/wsdl/in-out");
    public static final URI ROBUST_IN_ONLY = URI.create("http://www.w3.org/2004/08/wsdl/robust-in-only");

    private SoapEndpoint endpoint;
    private List policies;
    private JBIMarshaler jbiMarshaler;
    private SoapMarshaler soapMarshaler;
    private Map definitions;

    public SoapHelper(SoapEndpoint endpoint) {
        this.policies = endpoint.getPolicies();
        if (this.policies == null) {
            this.policies = Collections.EMPTY_LIST;
        }
        this.definitions = new HashMap();
        this.jbiMarshaler = new JBIMarshaler();
        this.endpoint = endpoint;
        boolean requireDom = false;
        for (Iterator iter = policies.iterator(); iter.hasNext();) {
            Handler handler = (Handler) iter.next();
            requireDom |= handler.requireDOM();
        }
        this.soapMarshaler = new SoapMarshaler(endpoint.isSoap(), requireDom);
        if (endpoint.isSoap() && "1.1".equals(endpoint.getSoapVersion())) {
            this.soapMarshaler.setSoapUri(SoapMarshaler.SOAP_11_URI);
        }
    }
    
    public SoapMarshaler getSoapMarshaler() {
        return this.soapMarshaler;
    }
    
    public JBIMarshaler getJBIMarshaler() {
        return this.jbiMarshaler;
    }

    public MessageExchange onReceive(Context context) throws Exception {
        if (policies != null) {
            for (Iterator it = policies.iterator(); it.hasNext();) {
                Handler policy = (Handler) it.next();
                policy.onReceive(context);
            }
        }
        URI mep = findMep(context);
        if (mep == null) {
            mep = endpoint.getDefaultMep();
        }
        MessageExchange exchange = createExchange(mep);
        // If WS-A has not set informations, use the default ones
        if (context.getProperty(Context.SERVICE) == null && context.getProperty(Context.INTERFACE) == null) {
            if (endpoint.getDefaultOperation() != null) {
                context.setProperty(Context.OPERATION, endpoint.getDefaultOperation());
            } else {
                context.setProperty(Context.OPERATION, context.getInMessage().getBodyName());
            }
            // If no target endpoint / service / interface is defined
            // we assume we use the same informations has defined on the
            // external endpoint
            if (endpoint.getTargetInterfaceName() == null && endpoint.getTargetService() == null
                            && endpoint.getTargetEndpoint() == null) {
                context.setProperty(Context.INTERFACE, endpoint.getInterfaceName());
                context.setProperty(Context.SERVICE, endpoint.getService());
                context.setProperty(Context.ENDPOINT, endpoint.getEndpoint());
            } else {
                context.setProperty(Context.INTERFACE, endpoint.getTargetInterfaceName());
                context.setProperty(Context.SERVICE, endpoint.getTargetService());
                context.setProperty(Context.ENDPOINT, endpoint.getTargetEndpoint());
            }
        }
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
        jbiMarshaler.toNMS(inMessage, context.getInMessage());
        exchange.setMessage(inMessage, "in");
        return exchange;
    }

    public SoapMessage onReply(Context context, NormalizedMessage outMsg) throws Exception {
        SoapMessage out = new SoapMessage();
        if (context.getInMessage() != null) {
            out.setEnvelopeName(context.getInMessage().getEnvelopeName());
        }
        jbiMarshaler.fromNMS(out, outMsg);
        context.setOutMessage(out);
        if (policies != null) {
            for (Iterator it = policies.iterator(); it.hasNext();) {
                Handler policy = (Handler) it.next();
                policy.onReply(context);
            }
        }
        return out;
    }

    public SoapMessage onFault(Context context, SoapFault fault) throws Exception {
        SoapMessage soapFault = new SoapMessage();
        soapFault.setFault(fault);
        if (context == null) {
            context = new Context();
        }
        if (context.getInMessage() != null) {
            soapFault.setEnvelopeName(context.getInMessage().getEnvelopeName());
        }
        context.setFaultMessage(soapFault);
        if (policies != null) {
            for (Iterator it = policies.iterator(); it.hasNext();) {
                Handler policy = (Handler) it.next();
                policy.onFault(context);
            }
        }
        return soapFault;
    }

    public Context createContext(SoapMessage message) {
        Context context = new Context();
        context.setInMessage(message);
        return context;
    }

    protected MessageExchange createExchange(URI mep) throws MessagingException {
        ComponentContext context = endpoint.getServiceUnit().getComponent().getComponentContext();
        DeliveryChannel channel = context.getDeliveryChannel();
        MessageExchangeFactory factory = channel.createExchangeFactory();
        MessageExchange exchange = factory.createExchange(mep);
        return exchange;
    }

    protected URI findMep(Context context) throws Exception {
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
        } else {
            // Find endpoint description from the component context
            String key = se.getServiceName() + se.getEndpointName();
            synchronized (definitions) {
                definition = (Definition) definitions.get(key);
                if (definition == null) {
                    WSDLFactory factory = WSDLFactory.newInstance();
                    Document description = componentContext.getEndpointDescriptor(se);
                    if (description != null) {
                        // Parse WSDL
                        WSDLReader reader = factory.newWSDLReader();
                        try {
                            definition = reader.readWSDL(null, description);
                        } catch (WSDLException e) {
                            logger.info("Could not read wsdl from endpoint descriptor: " + e.getMessage());
                            if (logger.isDebugEnabled()) {
                                logger.debug("Could not read wsdl from endpoint descriptor", e);
                            }
                        }
                    }
                    if (definition == null) {
                        definition = factory.newDefinition();
                    }
                    definitions.put(key, definition);
                }
            }
        }

        // Find operation within description
        URI mep = null;
        if (interfaceName != null && operationName != null && definition != null) {
            PortType portType = definition.getPortType(interfaceName);
            if (portType != null) {
                Operation oper = portType.getOperation(operationName.getLocalPart(), null, null);
                if (oper != null) {
                    boolean output = oper.getOutput() != null && oper.getOutput().getMessage() != null
                                    && oper.getOutput().getMessage().getParts().size() > 0;
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
