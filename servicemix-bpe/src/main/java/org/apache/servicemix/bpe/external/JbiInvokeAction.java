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
package org.apache.servicemix.bpe.external;

import java.net.URI;
import java.util.HashMap;
import java.util.Properties;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.bpe.BPEComponent;
import org.apache.servicemix.bpe.BPELifeCycle;
import org.apache.servicemix.common.ExchangeProcessor;
import org.apache.servicemix.jbi.jaxp.BytesSource;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.ode.action.external.ActionSystemException;
import org.apache.ode.action.external.IExternalAction;
import org.apache.ode.action.external.IURIResolver;
import org.apache.ode.client.IFormattableValue;
import org.apache.ode.interaction.XMLInteractionObject;
import org.apache.ode.scope.service.BPRuntimeException;

public class JbiInvokeAction implements IExternalAction, ExchangeProcessor {

    public static final String INTERFACE_NAMESPACE = "interfaceNamespace";
    public static final String INTERFACE_LOCALNAME = "interfaceLocalName";
    public static final String OPERATION_NAMESPACE = "operationNamespace";
    public static final String OPERATION_LOCALNAME = "operationLocalName";
    public static final String SERVICE_NAMESPACE = "serviceNamespace";
    public static final String SERVICE_LOCALNAME = "serviceLocalName";
    public static final String ENDPOINT_NAME = "endpointName";
    public static final String ACTION = "action";
    public static final String ENDPOINT = "endpoint";
    public static final String MEP = "mep";
    
    private static Log log = LogFactory.getLog(JbiInvokeAction.class);
    
    private Properties properties;
    private DeliveryChannel channel;
    private MessageExchangeFactory factory;
    private BPEComponent component;
    private QName interfaceName;
    private QName serviceName;
    private String endpointName;
    private QName operationName;
    private URI mep;
    private SourceTransformer transformer;
    
	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = -8522450752525724302L;
    
    public JbiInvokeAction() {
        transformer = new SourceTransformer();
    }

	public void init(Properties props) throws BPRuntimeException, ActionSystemException {
        if (log.isDebugEnabled()) {
            log.debug("init");
        }
        this.properties = props;
        component = BPEComponent.getInstance();
        if (component == null) {
            throw new BPRuntimeException("BPEComponent has not been created", "");
        }
        try {
            channel = ((BPELifeCycle) component.getLifeCycle()).getContext().getDeliveryChannel();
            factory = channel.createExchangeFactory();
        } catch (MessagingException e) {
            throw new BPRuntimeException("Could not retrieve DeliveryChannel", e);
        } 
        extractInformations();
        if (serviceName == null && interfaceName == null) { 
            throw new BPRuntimeException("Interface, Service or Endpoint should be specified", "");
        }
        if (log.isDebugEnabled()) {
            log.debug("properties: " + props);
        }
	}

	protected void extractInformations() {
        String action = properties.getProperty(ACTION);
        if (action != null) {
            String[] parts = split(action);
            interfaceName = new QName(parts[0], parts[1]);
            operationName = new QName(parts[0], parts[2]);
        } else {
            String interfaceNamespace = properties.getProperty(INTERFACE_NAMESPACE);
            String interfaceLocalName = properties.getProperty(INTERFACE_LOCALNAME);
            if (interfaceLocalName != null) {
                interfaceName = new QName(interfaceNamespace, interfaceLocalName);
            }
            String operationNamespace = properties.getProperty(OPERATION_NAMESPACE);
            String operationLocalName = properties.getProperty(OPERATION_LOCALNAME);
            if (operationLocalName != null) {
                operationName = new QName(operationNamespace, operationLocalName);
            }
        }
        String endpoint = properties.getProperty(ENDPOINT); 
        if (endpoint != null) {
            String[] parts = split(action);
            serviceName = new QName(parts[0], parts[1]);
            endpointName = parts[2];
        } else {
            String serviceNamespace = properties.getProperty(SERVICE_NAMESPACE);
            String serviceLocalName = properties.getProperty(SERVICE_LOCALNAME);
            if (serviceLocalName != null) {
                serviceName = new QName(serviceNamespace, serviceLocalName);
            }
            endpointName = properties.getProperty(ENDPOINT_NAME);
        }
        String mep = properties.getProperty(MEP);
        if (mep == null) {
            mep = "in-out"; 
        }
        this.mep = URI.create("http://www.w3.org/2004/08/wsdl/" + mep);
    }

    public void execute(HashMap input, HashMap output, IURIResolver resolver)
			throws BPRuntimeException, ActionSystemException {
        if (log.isDebugEnabled()) {
            log.debug("execute");
        }
        Object payload = input.get(BPEComponent.PART_PAYLOAD);
        Source inputSource = getSourceFromPayload(payload);
        // Create and send exchange
        try {
            // TODO: need to configure mep
            MessageExchange me = factory.createExchange(this.mep);
            me.setInterfaceName(interfaceName);
            me.setService(serviceName);
            // TODO: set endpoint
            me.setOperation(operationName);
            NormalizedMessage nm = me.createMessage();
            me.setMessage(nm, "in");
            nm.setContent(inputSource);
            boolean res = channel.sendSync(me);
            if (!res) {
                throw new ActionSystemException("Timeout on sending message");
            }
            if (me.getStatus() == ExchangeStatus.ACTIVE) {
                nm = me.getMessage("out");
                if (nm != null) {
                    try {
                        XMLInteractionObject result = new XMLInteractionObject();
                        result.setDocument((Document) transformer.toDOMNode(nm));
                        output.put(BPEComponent.PART_PAYLOAD, result);
                    } catch (Exception e) {
                        throw new ActionSystemException(e);
                    }
                }
                me.setStatus(ExchangeStatus.DONE);
                channel.send(me);
            } else if (me.getStatus() == ExchangeStatus.ERROR) {
                // Extract fault or error
                if (me.getFault() != null) {
                    Document fault;
                    try {
                        fault = (Document) transformer.toDOMNode(me.getFault());
                    } catch (Exception e) {
                        throw new ActionSystemException(e);
                    }
                    me.setStatus(ExchangeStatus.DONE);
                    channel.send(me);
                    Element e = fault.getDocumentElement();
                    BPRuntimeException bpre = new BPRuntimeException(e.getLocalName(), "");
                    bpre.setNameSpace(e.getNamespaceURI());
                    XMLInteractionObject interaction = new XMLInteractionObject();
                    interaction.setDocument(fault);
                    bpre.addPartMessage("payload", interaction);
                    throw bpre;
                    /*
                    try {
                        XMLInteractionObject result = new XMLInteractionObject();
                        result.setDocument((Document) transformer.toDOMNode(me.getFault()));
                        output.put(BPEComponent.PART_PAYLOAD, result);
                    } catch (Exception e) {
                        throw new ActionSystemException(e);
                    }
                    me.setStatus(ExchangeStatus.DONE);
                    channel.send(me);
                    */
                } else {
                    Exception error = me.getError();
                    me.setStatus(ExchangeStatus.DONE);
                    channel.send(me);
                    throw new BPRuntimeException("Unknown", error);
                }
            }
        } catch (MessagingException e) {
            throw new ActionSystemException(e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Request: " + payload);
            log.debug("Response: " + output.get("payload"));
        }
	}

    protected Source getSourceFromPayload(Object payload) {
        Source inputSource;
        if (payload instanceof IFormattableValue) {
            IFormattableValue value = (IFormattableValue) payload;
            if (value.supportsGetValueAs(Document.class)) {
                Document doc = (Document) value.getValueAs(Document.class);
                inputSource = new DOMSource(doc);
            } else if (value.supportsGetValueAs(byte[].class)) {
                byte[] data = (byte[]) value.getValueAs(byte[].class);
                inputSource = new BytesSource(data);
            } else if (value.supportsGetValueAs(String.class)) {
                String data = (String) value.getValueAs(String.class);
                inputSource = new StringSource(data);
            } else {
                throw new UnsupportedOperationException("Unable to retrieve value");
            }
        } else if (payload instanceof Document) {
            inputSource = new DOMSource((Document) payload);
        } else if (payload instanceof byte[]) {
            inputSource = new BytesSource((byte[]) payload);
        } else if (payload instanceof String) {
            inputSource = new StringSource((String) payload);
        } else {
            throw new UnsupportedOperationException("Unable to retrieve value");
        }
        return inputSource;
    }
    
	public void release() {
        if (log.isDebugEnabled()) {
            log.debug("release");
        }
	}

    public void process(MessageExchange exchange) throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void start() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void stop() throws Exception {
        // TODO Auto-generated method stub
        
    }

    protected String[] split(String uri) {
        char sep;
        if (uri.indexOf('/') > 0) {
            sep = '/';
        } else {
            sep = ':';
        }
        int idx1 = uri.lastIndexOf(sep);
        int idx2 = uri.lastIndexOf(sep, idx1 - 1);
        String epName = uri.substring(idx1 + 1);
        String svcName = uri.substring(idx2 + 1, idx1);
        String nsUri   = uri.substring(0, idx2);
        return new String[] { nsUri, svcName, epName };
    }
}
