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
package org.apache.servicemix.components.wsif;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jbi.messaging.MessageExchange;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.servicemix.jbi.NoSuchOperationException;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFService;

/**
 * Maintains a Map of the available operations for the binding.
 * 
 * @version $Revision$
 */
public class WSIFOperationMap {

    private WSIFService service;
    private Map operationMap = new HashMap();
    private WSIFOperationInfo defaultOperation;

    public WSIFOperationMap(WSIFService service) {
        this.service = service;
    }

    /**
     * Returns the operation information for the current message exchange.
     *
     * @param exchange the current message exchange
     * @return the operation information
     * @throws NoSuchOperationException if the operation is not available
     */
    public WSIFOperationInfo getOperationForExchange(MessageExchange exchange) throws NoSuchOperationException {
        QName operationName = exchange.getOperation();
        WSIFOperationInfo operationInfo = getOperation(operationName);
        if (operationInfo == null) {
            throw new NoSuchOperationException(operationName);
        }
        return operationInfo;
    }

    /**
     * Returns the operation information for the given QName
     *
     * @param operationName is the name of the operation
     * @return the operation information or null if it is not available
     */
    public WSIFOperationInfo getOperation(QName operationName) {
        if (operationName == null) {
            // lets try a default operation
            return defaultOperation;
        }
        return (WSIFOperationInfo) operationMap.get(operationName);
    }

    public int getOperationCount() {
        return operationMap.values().size();
    }

    /**
     * Returns the operation for the given name
     *
     * @param operationName is the name of the operation
     * @return the operation instance or null if it is not available
     */
    public WSIFOperationInfo getOperation(String operationName) {
        return (WSIFOperationInfo) operationMap.get(operationName);
    }

    public void addBinding(Binding binding) throws WSIFException {
        List list = binding.getBindingOperations();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            BindingOperation bindingOperation = (BindingOperation) iter.next();
            addBindingOperation(binding, bindingOperation);
        }
    }

    protected void addBindingOperation(Binding binding, BindingOperation bindingOperation) throws WSIFException {
        Operation operation = bindingOperation.getOperation();
        String name = operation.getName();
        WSIFOperationInfo info = new WSIFOperationInfo(service.getPort(), name);
        operationMap.put(name, info);
        operationMap.put(new QName(name), info);
        if (defaultOperation == null) {
            defaultOperation = info;
        }
    }

}
