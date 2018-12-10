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
package org.apache.servicemix.jbi.servicedesc;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.parsers.DocumentBuilderFactory;

public class EndpointReferenceBuilder {

    public static final String JBI_NAMESPACE = "http://java.sun.com/xml/ns/jbi";
    
    public static DocumentFragment getReference(ServiceEndpoint endpoint) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            DocumentFragment fragment = doc.createDocumentFragment();
            Element epr = doc.createElementNS(JBI_NAMESPACE, "jbi:end-point-reference");
            epr.setAttribute("xmlns:jbi", JBI_NAMESPACE);
            epr.setAttribute("xmlns:sns", endpoint.getServiceName().getNamespaceURI());
            epr.setAttributeNS(JBI_NAMESPACE, "jbi:service-name", "sns:" + endpoint.getServiceName().getLocalPart());
            epr.setAttributeNS(JBI_NAMESPACE, "jbi:end-point-name", endpoint.getEndpointName());
            fragment.appendChild(epr);
            return fragment;
        } catch (Exception e) {
            return null;
        }
    }
    
}
