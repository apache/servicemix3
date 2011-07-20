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
package org.apache.servicemix.jbi.framework.support;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import com.ibm.wsdl.Constants;

import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieve interface implemented by the given endpoint using the WSDL 1 description.
 */
public class WSDL1Processor implements EndpointProcessor {

    public static final String WSDL1_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(WSDL1Processor.class);
    
    private Registry registry;
    
    public void init(Registry reg) {
        this.registry = reg;
    }

    /**
     * Retrieve interface implemented by the given endpoint using the WSDL 1 description.
     * 
     * @param serviceEndpoint the endpoint being checked
     */
    public void process(InternalEndpoint serviceEndpoint) {
        try {
            Document document = registry.getEndpointDescriptor(serviceEndpoint);
            if (document == null || document.getDocumentElement() == null) {
                LOGGER.debug("Endpoint {} has no service description", serviceEndpoint);
                return;
            }
            if (!WSDL1_NAMESPACE.equals(document.getDocumentElement().getNamespaceURI())) {
                LOGGER.debug("Endpoint {} has a non WSDL1 service description", serviceEndpoint);
                return;
            }
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature(Constants.FEATURE_VERBOSE, false);
            Definition definition = reader.readWSDL(null, document);
            // Check if the wsdl is only a port type
            // In these cases, only the port type is used, as the service name and endpoint name
            // are provided on the jbi endpoint
            if (definition.getPortTypes().keySet().size() == 1
                    && definition.getServices().keySet().size() == 0) {
                PortType portType = (PortType) definition.getPortTypes().values().iterator().next();
                QName interfaceName = portType.getQName();
                LOGGER.debug("Endpoint {} implements interface {}", serviceEndpoint, interfaceName);
                serviceEndpoint.addInterface(interfaceName);
            } else {
                Service service = definition.getService(serviceEndpoint.getServiceName());
                if (service == null) {
                    LOGGER.info("Endpoint {} has a service description, but no matching service found in {}",
                            serviceEndpoint, definition.getServices().keySet());
                    return;
                }
                Port port = service.getPort(serviceEndpoint.getEndpointName());
                if (port == null) {
                    LOGGER.info("Endpoint {} has a service description, but no matching endpoint found in {}",
                            serviceEndpoint, service.getPorts().keySet());
                    return;
                }
                if (port.getBinding() == null) {
                    LOGGER.info("Endpoint {} has a service description, but no binding found", serviceEndpoint);
                    return;
                }
                if (port.getBinding().getPortType() == null) {
                    LOGGER.info("Endpoint {} has a service description, but no port type found", serviceEndpoint);
                    return;
                }
                QName interfaceName = port.getBinding().getPortType().getQName();
                LOGGER.debug("Endpoint {} implements interface {}", serviceEndpoint, interfaceName);
                serviceEndpoint.addInterface(interfaceName);
            }
        } catch (Exception e) {
            LOGGER.warn("Error retrieving interfaces from service description: {}", e.getMessage());
            LOGGER.debug("Error retrieving interfaces from service description", e);
        }
    }

}
