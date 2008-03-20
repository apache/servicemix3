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
package org.apache.servicemix.jbi.installation;

import javax.jbi.JBIException;
import javax.jbi.component.Component;
import javax.jbi.component.ComponentContext;
import javax.jbi.component.ComponentLifeCycle;
import javax.jbi.component.ServiceUnitManager;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.management.ObjectName;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

public class DummyComponent implements Component, ComponentLifeCycle, ServiceUnitManager {

    private String result;
    private boolean exception;
    
    public DummyComponent() {
    }

    public ComponentLifeCycle getLifeCycle() {
        return this;
    }

    public ServiceUnitManager getServiceUnitManager() {
        return this;
    }

    public Document getServiceDescription(ServiceEndpoint endpoint) {
        return null;
    }

    public boolean isExchangeWithConsumerOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return false;
    }

    public boolean isExchangeWithProviderOkay(ServiceEndpoint endpoint, MessageExchange exchange) {
        return false;
    }

    public ServiceEndpoint resolveEndpointReference(DocumentFragment epr) {
        return null;
    }

    public ObjectName getExtensionMBeanName() {
        return null;
    }

    public void init(ComponentContext context) throws JBIException {
    }

    public void shutDown() throws JBIException {
    }

    public void start() throws JBIException {
    }

    public void stop() throws JBIException {
    }

    public String deploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        if (exception) {
            throw new DeploymentException(result);
        }
        return result;
    }

    public void init(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
    }

    public void start(String serviceUnitName) throws DeploymentException {
    }

    public void stop(String serviceUnitName) throws DeploymentException {
    }

    public void shutDown(String serviceUnitName) throws DeploymentException {
    }

    public String undeploy(String serviceUnitName, String serviceUnitRootPath) throws DeploymentException {
        if (exception) {
            throw new DeploymentException(result);
        }
        return result;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isException() {
        return exception;
    }

    public void setException(boolean exception) {
        this.exception = exception;
    }

}
