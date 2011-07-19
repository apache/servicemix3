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
package org.apache.servicemix.jbi.servicedesc;

import javax.jbi.servicedesc.ServiceEndpoint;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import org.apache.servicemix.jbi.util.DOMUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointReferenceBuilder {
    
    public static final String JBI_NAMESPACE = "http://java.sun.com/jbi/end-point-reference";
    public static final String XMLNS_NAMESPACE = "http://www.w3.org/2000/xmlns/";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointReferenceBuilder.class);

    private EndpointReferenceBuilder() {
    }
    
    public static DocumentFragment getReference(ServiceEndpoint endpoint) {
        try {
            Document doc = DOMUtil.newDocument();
            DocumentFragment fragment = doc.createDocumentFragment();
            Element epr = doc.createElementNS(JBI_NAMESPACE, "jbi:end-point-reference");
            epr.setAttributeNS(XMLNS_NAMESPACE, "xmlns:sns", endpoint.getServiceName().getNamespaceURI());
            epr.setAttributeNS(JBI_NAMESPACE, "jbi:service-name", "sns:" + endpoint.getServiceName().getLocalPart());
            epr.setAttributeNS(JBI_NAMESPACE, "jbi:end-point-name", endpoint.getEndpointName());
            fragment.appendChild(epr);
            return fragment;
        } catch (Exception e) {
            LOGGER.warn("Unable to create reference for ServiceEndpoint {}", endpoint, e);
            return null;
        }
    }
    
}
