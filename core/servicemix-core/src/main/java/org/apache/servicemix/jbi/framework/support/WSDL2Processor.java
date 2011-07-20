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

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.apache.servicemix.jbi.framework.Registry;
import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;

import org.apache.woden.WSDLReader;
import org.apache.woden.internal.DOMWSDLReader;
import org.apache.woden.internal.DOMWSDLSource;
import org.apache.woden.types.NCName;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Interface;
import org.apache.woden.wsdl20.Service;
import org.apache.woden.wsdl20.xml.DescriptionElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Retrieve interface implemented by the given endpoint using the WSDL 2 description.
 */
public class WSDL2Processor implements EndpointProcessor {

    public static final String WSDL2_NAMESPACE = "http://www.w3.org/2006/01/wsdl";

    private static final transient Logger LOGGER = LoggerFactory.getLogger(WSDL2Processor.class);
    
    private Registry registry;
    
    public void init(Registry reg) {
        this.registry = reg;
    }

    /**
     * Retrieve interface implemented by the given endpoint using the WSDL 2 description.
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
            if (!WSDL2_NAMESPACE.equals(document.getDocumentElement().getNamespaceURI())) {
                LOGGER.debug("Endpoint {} has a non WSDL2 service description", serviceEndpoint);
                return;
            }
            WSDLReader reader = new DOMWSDLReader();
            DOMWSDLSource source = (DOMWSDLSource) reader.createWSDLSource();
            source.setSource(document);
            DescriptionElement descElement = reader.readWSDL(source);
            Description desc = descElement.toComponent();
            // Check if the wsdl is only a port type
            // In these cases, only the port type is used, as the service name and endpoint name
            // are provided on the jbi endpoint
            if (desc.getInterfaces().length == 1 && desc.getServices().length == 0) {
                Interface itf = desc.getInterfaces()[0];
                QName interfaceName = itf.getName();
                LOGGER.debug("Endpoint {} implements interface {}", serviceEndpoint, interfaceName);
                serviceEndpoint.addInterface(interfaceName);
            } else {
                Service service = desc.getService(serviceEndpoint.getServiceName());
                if (service == null) {
                    LOGGER.info("Endpoint {} has a service description, but no matching service found in {}",
                                    serviceEndpoint, desc.getServices());
                    return;
                }
                Endpoint endpoint = service.getEndpoint(new NCName(serviceEndpoint.getEndpointName()));
                if (endpoint == null) {
                    LOGGER.info("Endpoint {} has a service description, but no matching endpoint found in {}",
                                    serviceEndpoint, service.getEndpoints());
                    return;
                }
                if (endpoint.getBinding() == null) {
                    LOGGER.info("Endpoint {} has a service description, but no binding found", serviceEndpoint);
                    return;
                }
                if (endpoint.getBinding().getInterface() == null) {
                    LOGGER.info("Endpoint {} has a service description, but no port type found", serviceEndpoint);
                    return;
                }
                QName interfaceName = endpoint.getBinding().getInterface().getName();
                LOGGER.debug("Endpoint {} implements interface {}", serviceEndpoint, interfaceName);
                serviceEndpoint.addInterface(interfaceName);
            }
        } catch (Exception e) {
            LOGGER.warn("Error retrieving interfaces from service description: {}", e.getMessage());
            LOGGER.debug("Error retrieving interfaces from service description", e);
        }
    }
}
