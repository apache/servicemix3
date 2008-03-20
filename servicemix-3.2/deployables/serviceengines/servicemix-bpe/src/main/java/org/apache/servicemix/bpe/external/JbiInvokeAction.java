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
package org.apache.servicemix.bpe.external;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpe.action.external.ActionSystemException;
import org.apache.ode.bpe.action.external.IExternalAction;
import org.apache.ode.bpe.action.external.IURIResolver;
import org.apache.ode.bpe.client.IFormattableValue;
import org.apache.ode.bpe.interaction.spiimpl.document.DocumentFormattableValue;
import org.apache.ode.bpe.scope.service.BPRuntimeException;
import org.apache.servicemix.bpe.BPEComponent;
import org.apache.servicemix.bpe.BPEEndpoint;
import org.apache.servicemix.bpe.BPELifeCycle;
import org.apache.servicemix.bpe.BPEServiceUnit;
import org.apache.servicemix.jbi.jaxp.BytesSource;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;

public class JbiInvokeAction implements IExternalAction {

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

    private static final Log LOG = LogFactory.getLog(JbiInvokeAction.class);

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -8522450752525724302L;

    private Properties properties;
    private QName interfaceName;
    private QName serviceName;
    private String endpointName;
    private QName operationName;
    private String inputPartName = BPEComponent.PART_PAYLOAD;
    private String outputPartName = BPEComponent.PART_PAYLOAD;
    private URI mep;
    private SourceTransformer transformer;
    private Operation wsdlOperation;

    public JbiInvokeAction() {
        transformer = new SourceTransformer();
    }

    public void init(Properties props) throws BPRuntimeException, ActionSystemException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("init");
        }
        this.properties = props;
        extractInformations();
        if (serviceName == null && interfaceName == null) {
            throw new BPRuntimeException("Interface, Service or Endpoint should be specified", "");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("properties: " + props);
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
        String mepStr = properties.getProperty(MEP);
        if (mepStr == null) {
            BPEEndpoint ep = BPEEndpoint.getCurrent();
            Definition def = ((BPEServiceUnit) ep.getServiceUnit()).getDefinition();
            PortType pt = def.getPortType(interfaceName);
            Operation oper = pt != null ? pt.getOperation(operationName.getLocalPart(), null, null) : null;
            if (oper != null) {
                boolean output = oper.getOutput() != null && oper.getOutput().getMessage() != null
                        && oper.getOutput().getMessage().getParts().size() > 0;
                boolean faults = oper.getFaults().size() > 0;
                if (output) {
                    mepStr = "in-out";
                } else if (faults) {
                    mepStr = "robust-in-only";
                } else {
                    mepStr = "in-only";
                }
                if (oper.getInput() != null && oper.getInput().getMessage() != null) {
                    Map parts = oper.getInput().getMessage().getParts();
                    inputPartName = (String) parts.keySet().iterator().next();
                }
                if (oper.getOutput() != null && oper.getOutput().getMessage() != null) {
                    Map parts = oper.getOutput().getMessage().getParts();
                    outputPartName = (String) parts.keySet().iterator().next();
                }
                wsdlOperation = oper;
            }
        }
        if (mepStr == null) {
            mepStr = "in-out";
        }
        this.mep = URI.create("http://www.w3.org/2004/08/wsdl/" + mepStr);
    }

    public void execute(HashMap input, HashMap output, 
                        IURIResolver resolver) throws BPRuntimeException, ActionSystemException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute");
        }
        Object payload = input.get(inputPartName);
        Source inputSource = getSourceFromPayload(payload);
        // Create and send exchange
        try {
            BPEEndpoint endpoint = BPEEndpoint.getCurrent();
            BPEComponent component = (BPEComponent) endpoint.getServiceUnit().getComponent();
            DeliveryChannel channel = ((BPELifeCycle) component.getLifeCycle()).getContext().getDeliveryChannel();
            MessageExchangeFactory factory = channel.createExchangeFactory();
            // TODO: need to configure mep
            MessageExchange me = factory.createExchange(this.mep);
            me.setInterfaceName(interfaceName);
            me.setService(serviceName);
            if (endpointName != null) {
                ServiceEndpoint ep = component.getComponentContext().getEndpoint(serviceName, endpointName);
                me.setEndpoint(ep);
            }
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
                if (me.getFault() != null) {
                    Document fault;
                    try {
                        fault = transformer.toDOMDocument(me.getFault());
                        me.setStatus(ExchangeStatus.DONE);
                    } catch (Exception e) {
                        me.setError(e);
                        throw new ActionSystemException(e);
                    } finally {
                        channel.send(me);
                    }
                    Element e = fault.getDocumentElement();
                    // Try to determine fault name
                    String faultName = e.getLocalName();
                    String partName = BPEComponent.PART_PAYLOAD;
                    QName elemName = new QName(e.getNamespaceURI(), e.getLocalName());
                    if (wsdlOperation != null) {
                        for (Iterator itFault = wsdlOperation.getFaults().values().iterator(); itFault.hasNext();) {
                            Fault f = (Fault) itFault.next();
                            Part p = (Part) f.getMessage().getParts().values().iterator().next();
                            if (elemName.equals(p.getTypeName())) {
                                faultName = f.getName();
                                partName = p.getName();
                            }
                        }
                    }
                    BPRuntimeException bpre = new BPRuntimeException(faultName, "");
                    bpre.setNameSpace(e.getNamespaceURI());
                    /* We must use a type that implements BPE's IFormattableValue interface
                     * since otherwise the value will get wrapped in a CannedFormattableValue
                     * which has undesireable side effects.  
                     */
                    DocumentFormattableValue documentFormattableValue = new DocumentFormattableValue(fault);
                    bpre.addPartMessage(partName, documentFormattableValue);
                    throw bpre;
                } else {
                    try {
                        nm = me.getMessage("out");
                        if (nm != null) {
                            /* We must use a type that implements BPE's IFormattableValue interface
                             * since otherwise the value will get wrapped in a CannedFormattableValue
                             * which has undesireable side effects.  
                             */
                            Document out = transformer.toDOMDocument(nm);
                            DocumentFormattableValue documentFormattableValue = new DocumentFormattableValue(out);
                            output.put(outputPartName, documentFormattableValue);
                        }
                        me.setStatus(ExchangeStatus.DONE);
                    } catch (Exception e) {
                        me.setError(e);
                        throw new ActionSystemException(e);
                    } finally {
                        channel.send(me);
                    }
                }
            } else if (me.getStatus() == ExchangeStatus.ERROR) {
                // Extract error
                Exception error = me.getError();
                throw new BPRuntimeException("Unknown", error);
            }
        } catch (MessagingException e) {
            throw new ActionSystemException(e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request: " + payload);
            LOG.debug("Response: " + output.get(outputPartName));
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("release");
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
        String nsUri = uri.substring(0, idx2);
        return new String[] {nsUri, svcName, epName };
    }
}
