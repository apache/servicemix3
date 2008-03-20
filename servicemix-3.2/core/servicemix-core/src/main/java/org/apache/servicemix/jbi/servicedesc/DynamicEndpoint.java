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
import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

import org.apache.servicemix.jbi.framework.ComponentNameSpace;

/**
 * Dynamic endpoints are used to route exchanges using endpoint references.
 */
public class DynamicEndpoint extends AbstractServiceEndpoint {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = -9084647509619730734L;
    
    private final QName serviceName;
    private final String endpointName;
    private final transient DocumentFragment epr;
    
    public DynamicEndpoint(ComponentNameSpace componentName,
                           ServiceEndpoint endpoint,
                           DocumentFragment epr) {
        super(componentName);
        this.serviceName = endpoint.getServiceName();
        this.endpointName = endpoint.getEndpointName();
        this.epr = epr;
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getAsReference(javax.xml.namespace.QName)
     */
    public DocumentFragment getAsReference(QName operationName) {
        return epr;
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getEndpointName()
     */
    public String getEndpointName() {
        return endpointName;
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getInterfaces()
     */
    public QName[] getInterfaces() {
        return null;
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getServiceName()
     */
    public QName getServiceName() {
        return serviceName;
    }

    protected String getClassifier() {
        return "dynamic";
    }

}
