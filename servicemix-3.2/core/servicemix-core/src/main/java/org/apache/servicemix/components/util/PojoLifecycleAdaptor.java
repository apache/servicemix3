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
package org.apache.servicemix.components.util;

import javax.jbi.JBIException;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import org.springframework.beans.factory.DisposableBean;

/**
 * Adapts a POJO to a {@link ComponentLifeCycle} without performing any activation
 *
 * @version $Revision$
 */
public class PojoLifecycleAdaptor implements ComponentLifeCycle {

    private Object pojo;
    private QName service;
    private String endpoint;
    private ComponentContext context;
    private ObjectName extensionMBeanName;

    public PojoLifecycleAdaptor(Object pojo, QName service, String endpoint) {
        this.pojo = pojo;
        this.service = service;
        this.endpoint = endpoint;
    }

    public ObjectName getExtensionMBeanName() {
        return extensionMBeanName;
    }

    public void init(ComponentContext ctx) throws JBIException {
        this.context = ctx;
        if (service != null && endpoint != null) {
            ctx.activateEndpoint(service, endpoint);
        }
    }


    public void shutDown() throws JBIException {
        if (pojo instanceof DisposableBean) {
            DisposableBean disposableBean = (DisposableBean) pojo;
            try {
                disposableBean.destroy();
            } catch (Exception e) {
                throw new JBIException(e);
            }
        }
    }

    public void start() throws JBIException {
    }

    public void stop() throws JBIException {
    }

    // Properties
    //-------------------------------------------------------------------------
    public Object getPojo() {
        return pojo;
    }

    public void setExtensionMBeanName(ObjectName extensionMBeanName) {
        this.extensionMBeanName = extensionMBeanName;
    }

    public ComponentContext getContext() {
        return context;
    }
}
