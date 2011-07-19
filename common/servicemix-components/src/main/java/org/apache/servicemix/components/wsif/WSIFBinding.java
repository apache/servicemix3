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
package org.apache.servicemix.components.wsif;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;

import org.apache.servicemix.components.util.TransformComponentSupport;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Consumers JBI messages and sends them as a synchronous request/response into WSIF
 * then forwards the response
 *
 * @version $Revision$
 */
public class WSIFBinding extends TransformComponentSupport {

    private static transient Logger logger = LoggerFactory.getLogger(WSIFBinding.class);

    private WSIFMarshaler marshaler = new WSIFMarshaler();
    private WSIFService serviceHelper;
    private Definition definition;
    private Resource definitionResource;
    private WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
    private WSIFOperationMap operationMap;

    protected void init() throws JBIException {
        try {
            if (definition == null) {
                if (definitionResource == null) {
                    throw new JBIException("You must specify a definition or definitionResource property");
                }
                String uri = definitionResource.getURL().toString();
                InputStreamReader reader = new InputStreamReader(definitionResource.getInputStream());
                definition = WSIFUtils.readWSDL(uri, reader);
            }

            if (serviceHelper == null) {
                serviceHelper = factory.getService(definition);
            }

            if (logger.isDebugEnabled()) {
                for (Iterator iter = serviceHelper.getAvailablePortNames(); iter.hasNext();) {
                    logger.debug("Available port name: " + iter.next());
                }
            }

            operationMap = new WSIFOperationMap(serviceHelper);

            Map bindings = definition.getBindings();
            for (Iterator iter = bindings.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                Binding binding = (Binding) entry.getValue();
                operationMap.addBinding(binding);
            }
        }
        catch (IOException e) {
            throw new JBIException(e);
        }
        catch (WSDLException e) {
            throw new JBIException(e);
        }
    }

    /**
     * @deprecated use getMarshaler instead
     */
    public WSIFMarshaler getMarshaller() {
        return marshaler;
    }

    /**
     * @deprecated use setMarshaler instead
     */
    public void setMarshaller(WSIFMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public WSIFMarshaler getMarshaler() {
        return marshaler;
    }

    public void setMarshaler(WSIFMarshaler marshaler) {
        this.marshaler = marshaler;
    }

    public Definition getDefinition() {
        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public Resource getDefinitionResource() {
        return definitionResource;
    }

    public void setDefinitionResource(Resource definitionResource) {
        this.definitionResource = definitionResource;
    }

    public WSIFServiceFactory getFactory() {
        return factory;
    }

    public void setFactory(WSIFServiceFactory factory) {
        this.factory = factory;
    }

    public WSIFService getServiceHelper() {
        return serviceHelper;
    }

    public void setServiceHelper(WSIFService serviceHelper) {
        this.serviceHelper = serviceHelper;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected boolean transform(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws MessagingException {
        try {
            WSIFOperationInfo operationInfo = operationMap.getOperationForExchange(exchange);

            WSIFOperation operation = operationInfo.createWsifOperation();
            WSIFMessage inMessage = operation.createInputMessage();
            Object body = getBody(in);
            marshaler.fromNMS(operationInfo, inMessage, in, body);

            WSIFMessage outMessage = operation.createInputMessage();
            WSIFMessage faultMessage = operation.createInputMessage();
            boolean answer = operation.executeRequestResponseOperation(inMessage, outMessage, faultMessage);
            if (answer) {
                marshaler.toNMS(exchange, out, operationInfo, outMessage);
            }
            else {
                Fault fault = exchange.createFault();
                marshaler.toNMS(exchange, fault, operationInfo, outMessage);
                exchange.setFault(fault);
            }
            return true;
        }
        catch (WSIFException e) {
            throw new MessagingException(e);
        }
    }

}
