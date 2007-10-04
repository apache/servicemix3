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
package org.apache.servicemix.jbi.runtime.impl;

import org.apache.servicemix.api.NMR;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Document;

import javax.jbi.component.ComponentContext;
import javax.jbi.component.Component;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.jbi.JBIException;
import javax.jbi.management.MBeanNames;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessagingException;
import javax.xml.namespace.QName;
import javax.management.MBeanServer;
import javax.naming.InitialContext;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: gnodet
 * Date: Oct 4, 2007
 * Time: 10:36:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class ComponentContextImpl implements ComponentContext {

    private NMR nmr;
    private Component component;
    private Map<String,?> properties;

    public ComponentContextImpl(NMR nmr, Component component, Map<String,?> properties) {
        this.nmr = nmr;
        this.component = component;
        this.properties = properties;
    }

    public ServiceEndpoint activateEndpoint(QName serviceName, String endpointName) throws JBIException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deactivateEndpoint(ServiceEndpoint endpoint) throws JBIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerExternalEndpoint(ServiceEndpoint externalEndpoint) throws JBIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deregisterExternalEndpoint(ServiceEndpoint externalEndpoint) throws JBIException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getComponentName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public DeliveryChannel getDeliveryChannel() throws MessagingException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEndpoint getEndpoint(QName service, String name) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Document getEndpointDescriptor(ServiceEndpoint endpoint) throws JBIException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEndpoint[] getEndpoints(QName interfaceName) {
        return new ServiceEndpoint[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEndpoint[] getEndpointsForService(QName serviceName) {
        return new ServiceEndpoint[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEndpoint[] getExternalEndpoints(QName interfaceName) {
        return new ServiceEndpoint[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceEndpoint[] getExternalEndpointsForService(QName serviceName) {
        return new ServiceEndpoint[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getInstallRoot() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Logger getLogger(String suffix, String resourceBundleName) throws MissingResourceException, JBIException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public MBeanNames getMBeanNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public MBeanServer getMBeanServer() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InitialContext getNamingContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object getTransactionManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getWorkspaceRoot() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
