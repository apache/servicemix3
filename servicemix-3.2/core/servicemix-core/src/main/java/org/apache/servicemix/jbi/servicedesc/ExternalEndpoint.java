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
 * External endpoints are wrapper for endpoints registered
 * by {@link javax.jbi.component.ComponentContext#registerExternalEndpoint(ServiceEndpoint)}.
 * These endpoints can not be used to address message exchanges.
 * 
 * TODO: this class should be serializable
 */
public class ExternalEndpoint extends AbstractServiceEndpoint {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 4257588916448457889L;
    
    protected final ServiceEndpoint se;
    
    public ExternalEndpoint(ComponentNameSpace cns, ServiceEndpoint se) {
        super(cns);
        this.se = se;
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getAsReference(javax.xml.namespace.QName)
     */
    public DocumentFragment getAsReference(QName operationName) {
        return se.getAsReference(operationName);
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getEndpointName()
     */
    public String getEndpointName() {
        return se.getEndpointName();
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getInterfaces()
     */
    public QName[] getInterfaces() {
        return se.getInterfaces();
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getServiceName()
     */
    public QName getServiceName() {
        return se.getServiceName();
    }

    protected String getClassifier() {
        return "external";
    }

}
