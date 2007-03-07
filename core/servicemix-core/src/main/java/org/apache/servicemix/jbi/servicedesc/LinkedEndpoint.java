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

import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;

/**
 * Linked endpoints are defined by SA deployment.
 * They act as proxies for real endpoints.
 */
public class LinkedEndpoint extends AbstractServiceEndpoint {

    /**
     * Generated serial version UID
     */
    private static final long serialVersionUID = 4615848436197469611L;
    
    private final QName fromService;
    private final String fromEndpoint;
    private final QName toService;
    private final String toEndpoint;
    private final String linkType;
    
    public LinkedEndpoint(QName fromService,
                          String fromEndpoint,
                          QName toService,
                          String toEndpoint,
                          String linkType) {
        super(null);
        this.fromService = fromService;
        this.fromEndpoint = fromEndpoint;
        this.toService = toService;
        this.toEndpoint = toEndpoint;
        this.linkType = linkType;
    }
    
    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getAsReference(javax.xml.namespace.QName)
     */
    public DocumentFragment getAsReference(QName operationName) {
        return EndpointReferenceBuilder.getReference(this);
    }

    /* (non-Javadoc)
     * @see javax.jbi.servicedesc.ServiceEndpoint#getEndpointName()
     */
    public String getEndpointName() {
        return this.fromEndpoint;
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
        return this.fromService;
    }

    /**
     * Get the link type.
     * @return the link type
     */
    public String getLinkType() {
        return linkType;
    }

    /**
     * Get the destination endpoint.
     * @return the destination endpoint
     */
    public String getToEndpoint() {
        return toEndpoint;
    }

    /**
     * Get the destination service.
     * @return the destination service
     */
    public QName getToService() {
        return toService;
    }

    protected String getClassifier() {
        return "linked";
    }

}
